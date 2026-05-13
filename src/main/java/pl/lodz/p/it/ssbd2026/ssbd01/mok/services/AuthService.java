package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.common.utils.AuthenticationTokenValueGenerator;
import pl.lodz.p.it.ssbd2026.ssbd01.common.utils.PostCommitTransactionManager;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.OneTimeTokenConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AuthResponseDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.AccessLevel;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.OneTimeTokenEnt;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.OneTimeTokenType;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.*;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.LanguageDictRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.OneTimeTokenRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@MethodCallLogged
@Slf4j
public class AuthService {

    @Value("${google.client.id}")
    private String googleClientId;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthService jwtAuthService;
    private final EmailService emailService;
    private final PostCommitTransactionManager postCommitTransactionManager;
    private final AuthenticationTokenValueGenerator tokenValueGenerator;
    private final OneTimeTokenRepository tokenRepository;
    private final LanguageDictRepository languageDictRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public AuthResponseDTO authenticateWithGoogle(String idTokenString, String ipAddress) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                throw new InvalidCredentialsProvidedException();
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();

            Account account = accountRepository.findByEmail(email)
                    .orElseThrow(AccountNotFoundException::new);

            if (account.getIsBlocked()) {
                throw new AccountIsBlockedException();
            }

            List<String> roles = account.getAccessLevelList().stream()
                    .filter(AccessLevel::getActive)
                    .map(al -> al.getAccessLevelDict().getAccessLevelName())
                    .toList();

            account.setFailedAttempts(0);
            account.setLastSuccessfulLoginIp(ipAddress);
            account.setLastLoginSuccessDateTime(LocalDateTime.now());
            accountRepository.saveAndFlush(account);
            log.info("User session started | User: {} | Logical address (IP): {}", account.getLogin(), ipAddress);

            String token = jwtAuthService.generateToken(account.getLogin(), roles);
            String refreshToken = jwtAuthService.generateRefreshToken(account.getLogin(), roles);

            return new AuthResponseDTO(token, refreshToken);

        } catch (Exception e) {
            throw new InvalidCredentialsProvidedException();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager", noRollbackFor = InvalidCredentialsProvidedException.class)
    @Retryable(retryFor = ConcurrentUpdateException.class, backoff = @Backoff(delayExpression = "${app.retry.backoff}"))
    public void authenticate(String login, String password, String ipAddress) {
        Account account = accountRepository.findByLogin(login).orElseThrow(InvalidCredentialsProvidedException::new);

        if (account.getIsBlocked()) {
            throw new AccountIsBlockedException();
        }

        if (!account.getIsVerified()) {
            throw new AccountIsNotVerifiedException();
        }

        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            handleFailedLogin(account, ipAddress);
            throw new InvalidCredentialsProvidedException();
        }
        generate2FactorCode(account);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW,
            transactionManager = "mokTransactionManager",
            noRollbackFor = {OneTimeTokenInvalidException.class, AccountNotFoundException.class})
    public AuthResponseDTO verify2FactorCode(String token, String login, String ipAddress, String langHeader) {

        String hashedToken = hashToken(token);

        Optional<OneTimeTokenEnt> tokenOpt = tokenRepository
                .findByTokenValueAndUsernameAndTokenTypeAndIsUsedIsFalseAndExpiresAtAfter(
                        hashedToken, login, OneTimeTokenType.CODE_2FA, LocalDateTime.now());

        if (tokenOpt.isEmpty()) {
            Account account = accountRepository.findByLogin(login)
                    .orElseThrow(OneTimeTokenInvalidException::new);
            handleFailedLogin(account, ipAddress);
            throw new OneTimeTokenInvalidException();
        }

        OneTimeTokenEnt tokenEntity = tokenOpt.get();
        Account account = accountRepository.findByLogin(tokenEntity.getUsername())
                .orElseThrow(AccountNotFoundException::new);

        tokenEntity.setUsed(true);
        tokenRepository.saveAndFlush(tokenEntity);

        return handleSuccessfulLogin(account, ipAddress, langHeader);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void resend2FactorCode(String login) {
        Account account = accountRepository.findByLogin(login)
                .orElseThrow(InvalidCredentialsProvidedException::new);

        if (account.getIsBlocked()) {
            throw new AccountIsBlockedException();
        }

        if (!account.getIsVerified()) {
            throw new AccountIsNotVerifiedException();
        }

        generate2FactorCode(account);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void generate2FactorCode(Account account) {
        List<OneTimeTokenEnt> activeTokens = tokenRepository
                .findByUsernameAndTokenTypeAndIsUsedIsFalse(account.getLogin(), OneTimeTokenType.CODE_2FA);

        if (!activeTokens.isEmpty()) {
            OneTimeTokenEnt oldToken = activeTokens.getFirst();
            if (oldToken.getExpiresAt() != null) {
                LocalDateTime createdAt = oldToken.getExpiresAt().minusMinutes(OneTimeTokenConsts.EXPIRATION_ONE_TIME_TOKEN_MINUTES);
                LocalDateTime canResend = createdAt.plusMinutes(OneTimeTokenConsts.RESEND_TIMEOUT_2FA_IN_MINUTES);

                if (LocalDateTime.now().isBefore(canResend)) {
                    throw new Resend2FATokenException();
                }
            }
            oldToken.setUsed(true);
            tokenRepository.save(oldToken);
        }

        OneTimeTokenEnt tokenEnt = new OneTimeTokenEnt();
        String twoFactorCode = tokenValueGenerator.generate2FAToken();
        postCommitTransactionManager.register(() -> emailService.send2FAuthEmail(twoFactorCode, account.getEmail(), account.getLanguageVersion().getLanguage()));

        tokenEnt.setTokenValue(hashToken(twoFactorCode));
        tokenEnt.setUsername(account.getLogin());
        tokenEnt.setExpiresAt(LocalDateTime.now().plusMinutes(OneTimeTokenConsts.EXPIRATION_ONE_TIME_TOKEN_MINUTES));
        tokenEnt.setTokenType(OneTimeTokenType.CODE_2FA);
        tokenEnt.setUsed(false);

        tokenRepository.saveAndFlush(tokenEnt);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance(OneTimeTokenConsts.HASHING_ALGORITHM);
            byte[] encodedHash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            return getStringFromByteArray(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new HashEncryptionException("Algorithm " + OneTimeTokenConsts.HASHING_ALGORITHM + " does not exist", e);
        }
    }

    private static String getStringFromByteArray(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private void handleFailedLogin(Account account, String ipAddress) {
        int newFailures = (account.getFailedAttempts() != null ? account.getFailedAttempts() : 0) + 1;
        account.setFailedAttempts(newFailures);
        account.setLastFailedLoginIp(ipAddress);
        account.setLastLoginFailureDateTime(LocalDateTime.now());

        if (newFailures >= 3) {
            account.setIsBlocked(true);
            postCommitTransactionManager.register(() -> emailService.sendAutoBlockEmail(account.getEmail(), account.getLogin(), account.getLanguageVersion().getLanguage()));
        }

        accountRepository.saveAndFlush(account);
    }

    private AuthResponseDTO handleSuccessfulLogin(Account account, String ipAddress, String langHeader) {
        account.setFailedAttempts(0);
        account.setLastSuccessfulLoginIp(ipAddress);
        account.setLastLoginSuccessDateTime(LocalDateTime.now());
        String langCode = (langHeader != null && langHeader.length() >= 2) ? langHeader.substring(0, 2).toLowerCase() : "en";
        languageDictRepository.findByLanguage(langCode)
                .ifPresent(account::setLanguageVersion);
        accountRepository.saveAndFlush(account);

        List<String> roles = account.getAccessLevelList().stream().filter(AccessLevel::getActive).map(al -> al.getAccessLevelDict().getAccessLevelName()).toList();

        if (roles.contains(AccountConsts.ROLE_ADMIN)) {
            postCommitTransactionManager.register(() -> emailService.sendAdminLoginNotification(account.getEmail(), account.getLogin(), ipAddress, account.getLanguageVersion().getLanguage()));
        }
        String token = jwtAuthService.generateToken(account.getLogin(), roles);
        String refreshToken = jwtAuthService.generateRefreshToken(account.getLogin(), roles);
        log.info("User session started | User: {} | Logical address (IP): {}", account.getLogin(), ipAddress);
        return new AuthResponseDTO(token, refreshToken);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    @Retryable(retryFor = ConcurrentUpdateException.class, backoff = @Backoff(delayExpression = "${app.retry.backoff}"))
    public AuthResponseDTO refreshToken(String refreshToken, String ipAddress) {
        if (!jwtAuthService.isTokenValid(refreshToken)) {
            throw new InvalidCredentialsProvidedException();
        }

        String login = jwtAuthService.extractUsername(refreshToken);

        Account account = accountRepository.findByLogin(login).orElseThrow(InvalidCredentialsProvidedException::new);

        if (account.getIsBlocked()) {
            throw new AccountIsBlockedException();
        }

        List<String> roles = account.getAccessLevelList().stream().filter(AccessLevel::getActive).map(al -> al.getAccessLevelDict().getAccessLevelName()).toList();

        account.setLastSuccessfulLoginIp(ipAddress);
        accountRepository.saveAndFlush(account);

        String newToken = jwtAuthService.generateToken(account.getLogin(), roles);
        String newRefreshToken = jwtAuthService.generateRefreshToken(account.getLogin(), roles);

        return new AuthResponseDTO(newToken, newRefreshToken);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
    @Retryable(retryFor = ConcurrentUpdateException.class, backoff = @Backoff(delayExpression = "${app.retry.backoff}"))
    public void authenticateOnlyWithEmail(String email, String ipAddress) {
        Account account = accountRepository.findByEmail(email).orElseThrow(AccountNotFoundException::new);
        if (account.getIsBlocked()) {
            throw new AccountIsBlockedException();
        } else if (!account.getIsVerified()) {
            throw new AccountIsNotVerifiedException();
        }
        generateEmailOnlyAuthCode(account);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager",
            noRollbackFor = {OneTimeTokenInvalidException.class, AccountNotFoundException.class})
    public AuthResponseDTO verifyEmailOnlyAuthCode(String token, String email, String ipAddress, String langHeader) {
        Account account = accountRepository.findByEmail(email).orElseThrow(AccountNotFoundException::new);
        String hashedToken = hashToken(token);
        Optional<OneTimeTokenEnt> tokenOpt = tokenRepository
                .findByTokenValueAndUsernameAndTokenTypeAndIsUsedIsFalseAndExpiresAtAfter(
                        hashedToken, account.getLogin(), OneTimeTokenType.EMAIL_ONLY_AUTH, LocalDateTime.now());
        if (tokenOpt.isEmpty()) {
            handleFailedLogin(account, ipAddress);
            throw new OneTimeTokenInvalidException();
        }
        OneTimeTokenEnt tokenEntity = tokenOpt.get();
        tokenEntity.setUsed(true);
        tokenRepository.saveAndFlush(tokenEntity);
        return handleSuccessfulLoginWithEmail(account, ipAddress, langHeader);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
    public void generateEmailOnlyAuthCode(Account account) {
        List<OneTimeTokenEnt> activeTokens = tokenRepository
                .findByUsernameAndTokenTypeAndIsUsedIsFalse(account.getLogin(), OneTimeTokenType.EMAIL_ONLY_AUTH);

        invalidateActiveToken(activeTokens);
        OneTimeTokenEnt tokenEnt = new OneTimeTokenEnt();
        String authCode = tokenValueGenerator.generateAuthWithEmailOnlyCode();
        postCommitTransactionManager.register(() ->
                emailService.sendEmailOnlyAuthCode(account.getEmail(), authCode, account.getLanguageVersion().getLanguage()));

        tokenEnt.setTokenValue(hashToken(authCode));
        tokenEnt.setUsername(account.getLogin());
        tokenEnt.setExpiresAt(LocalDateTime.now().plusMinutes(OneTimeTokenConsts.EXPIRATION_ONE_TIME_TOKEN_MINUTES));
        tokenEnt.setTokenType(OneTimeTokenType.EMAIL_ONLY_AUTH);
        tokenEnt.setUsed(false);
        tokenRepository.saveAndFlush(tokenEnt);
    }

    private AuthResponseDTO handleSuccessfulLoginWithEmail(Account account, String ipAddress, String langHeader) {
        account.setFailedAttempts(0);
        account.setLastSuccessfulLoginIp(ipAddress);
        account.setLastLoginSuccessDateTime(LocalDateTime.now());
        String langCode = (langHeader != null && langHeader.length() >= 2) ? langHeader.substring(0, 2).toLowerCase() : "en";
        languageDictRepository.findByLanguage(langCode).ifPresent(account::setLanguageVersion);
        accountRepository.saveAndFlush(account);

        List<String> roles = account.getAccessLevelList().stream()
                .filter(AccessLevel::getActive)
                .map(al -> al.getAccessLevelDict().getAccessLevelName())
                .toList();

        if (roles.contains(AccountConsts.ROLE_ADMIN)) {
            postCommitTransactionManager.register(() -> emailService.sendAdminLoginNotification(account.getEmail(), account.getLogin(), ipAddress, account.getLanguageVersion().getLanguage()));
        }

        String token = jwtAuthService.generateShortLivedToken(account.getLogin(), roles);
        String refreshToken = jwtAuthService.generateRefreshToken(account.getLogin(), roles);
        return new AuthResponseDTO(token, refreshToken);
    }

    private void invalidateActiveToken(List<OneTimeTokenEnt> activeTokens) {
        if (!activeTokens.isEmpty()) {
            OneTimeTokenEnt oldToken = activeTokens.getFirst();
            if (oldToken.getExpiresAt() != null) {
                LocalDateTime createdAt = oldToken.getExpiresAt().minusMinutes(OneTimeTokenConsts.EXPIRATION_ONE_TIME_TOKEN_MINUTES);
                LocalDateTime canResend = createdAt.plusMinutes(OneTimeTokenConsts.RESEND_TIMEOUT_2FA_IN_MINUTES);

                if (LocalDateTime.now().isBefore(canResend)) {
                    throw new Resend2FATokenException();
                }
            }
            oldToken.setUsed(true);
            tokenRepository.save(oldToken);
        }
    }
}