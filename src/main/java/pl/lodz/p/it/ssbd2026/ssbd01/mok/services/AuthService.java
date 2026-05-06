package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.common.utils.PostCommitTransactionManager;
import pl.lodz.p.it.ssbd2026.ssbd01.common.utils.TwoFactorAuthenticationTokenGenerator;
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
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.OneTimeTokenRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@MethodCallLogged
public class AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthService jwtAuthService;
    private final EmailService emailService;
    private final PostCommitTransactionManager postCommitTransactionManager;
    private final TwoFactorAuthenticationTokenGenerator tokenValueGenerator;
    private final OneTimeTokenRepository tokenRepository;

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

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public AuthResponseDTO verify2FactorCode(String token, String ipAddress) {

        String hashedToken = hashToken(token);

        OneTimeTokenEnt tokenEntity = tokenRepository
                .findByTokenValueAndTokenTypeAndIsUsedIsFalseAndExpiresAtAfter(hashedToken, OneTimeTokenType.CODE_2FA, LocalDateTime.now())
                .orElseThrow(() -> new OneTimeTokenInvalidException("Token is invalid"));

        Account account = accountRepository.findByLogin(tokenEntity.getUsername())
                .orElseThrow(AccountNotFoundException::new);

        tokenEntity.setUsed(true);
        tokenRepository.saveAndFlush(tokenEntity);

        return handleSuccessfulLogin(account, ipAddress);
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

        OneTimeTokenEnt tokenEnt;

        if (!activeTokens.isEmpty()) {
            tokenEnt = activeTokens.getFirst();

            if (tokenEnt.getExpiresAt() != null) {
                LocalDateTime createdAt = tokenEnt.getExpiresAt().minusMinutes(OneTimeTokenConsts.EXPIRATION_ONE_TIME_TOKEN_MINUTES);

                LocalDateTime canResend = createdAt.plusMinutes(OneTimeTokenConsts.RESEND_TIMEOUT_2FA_IN_MINUTES);

                if (LocalDateTime.now().isBefore(canResend)) {
                    throw new Resend2FATokenException();
                }
            }
        } else {
            tokenEnt = new OneTimeTokenEnt();
        }

        String twoFactorCode = tokenValueGenerator.generate2FAToken();
        postCommitTransactionManager.register(() -> emailService.send2FAuthEmail(twoFactorCode, account.getEmail()));

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
            postCommitTransactionManager.register(() -> emailService.sendAutoBlockEmail(account.getEmail(), account.getLogin()));
        }

        accountRepository.saveAndFlush(account);
    }

    private AuthResponseDTO handleSuccessfulLogin(Account account, String ipAddress) {
        account.setFailedAttempts(0);
        account.setLastSuccessfulLoginIp(ipAddress);
        account.setLastLoginSuccessDateTime(LocalDateTime.now());
        accountRepository.saveAndFlush(account);

        List<String> roles = account.getAccessLevelList().stream().filter(AccessLevel::getActive).map(al -> al.getAccessLevelDict().getAccessLevelName()).toList();

        if (roles.contains(AccountConsts.ROLE_ADMIN)) {
            postCommitTransactionManager.register(() -> emailService.sendAdminLoginNotification(account.getEmail(), account.getLogin(), ipAddress));
        }
        String token = jwtAuthService.generateToken(account.getLogin(), roles);
        String refreshToken = jwtAuthService.generateRefreshToken(account.getLogin(), roles);
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
}