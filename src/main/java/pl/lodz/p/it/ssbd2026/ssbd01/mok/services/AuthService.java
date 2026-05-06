package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.common.utils.PostCommitTransactionManager;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AuthResponseDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.AccessLevel;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccountIsBlockedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.InvalidCredentialsProvidedException;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;

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

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager", noRollbackFor = InvalidCredentialsProvidedException.class)
    @Retryable(retryFor = ConcurrentUpdateException.class, backoff = @Backoff(delayExpression = "${app.retry.backoff}"))
    public AuthResponseDTO authenticate(String login, String password, String ipAddress) {
        Account account = accountRepository.findByLogin(login)
                .orElseThrow(InvalidCredentialsProvidedException::new);

        if (account.getIsBlocked()) {
            throw new AccountIsBlockedException();
        }

        if (!account.getIsVerified()) {
            throw new InvalidCredentialsProvidedException();
        }

        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            handleFailedLogin(account, ipAddress);
            throw new InvalidCredentialsProvidedException();
        }

        return handleSuccessfulLogin(account, ipAddress);
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

        List<String> roles = account.getAccessLevelList().stream()
                .filter(AccessLevel::getActive)
                .map(al -> al.getAccessLevelDict().getAccessLevelName())
                .toList();

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

        Account account = accountRepository.findByLogin(login)
                .orElseThrow(InvalidCredentialsProvidedException::new);

        if (account.getIsBlocked()) {
            throw new AccountIsBlockedException();
        }

        List<String> roles = account.getAccessLevelList().stream()
                .filter(AccessLevel::getActive)
                .map(al -> al.getAccessLevelDict().getAccessLevelName())
                .toList();

        account.setLastSuccessfulLoginIp(ipAddress);
        accountRepository.saveAndFlush(account);

        String newToken = jwtAuthService.generateToken(account.getLogin(), roles);
        String newRefreshToken = jwtAuthService.generateRefreshToken(account.getLogin(), roles);

        return new AuthResponseDTO(newToken, newRefreshToken);
    }
}