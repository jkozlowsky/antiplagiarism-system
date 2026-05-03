package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.OneTimeTokenConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.*;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel.AccessLevelAlreadyAssignedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel.AccessLevelAlreadyRevokedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel.AccessLevelDictNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel.AccessLevelNotAssignedException;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccessLevelDictRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.OneTimeTokenRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@MethodCallLogged
@TransactionLogged
@Transactional(
        propagation = Propagation.REQUIRES_NEW,
        readOnly = true,
        transactionManager = "mokTransactionManager"
)
@Retryable(
        retryFor = {JpaSystemException.class},
        backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
        maxAttemptsExpression = "${app.retry.maxattempts}"
)
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccessLevelDictRepository accessLevelDictRepository;
    private final EmailService emailService;
    private final OneTimeTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mokTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Retryable(
            retryFor = {JpaSystemException.class, ConcurrentUpdateException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}"
    )
    public Account getAccountWithAccessLevels(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(AccountNotFoundException::new);

        account.getAccessLevelList().forEach(accessLevel -> {
            if (accessLevel.getAccessLevelDict() != null) {
                accessLevel.getAccessLevelDict().getAccessLevelName();
            }
        });

        return account;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Retryable(
            retryFor = {JpaSystemException.class, ConcurrentUpdateException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}"
    )
    public void grantAccessLevel(UUID uuid, String accessLevelName, Integer sourceVersion) {
        Account account = accountRepository.findById(uuid)
                .orElseThrow(AccountNotFoundException::new);

        if (!account.getVersion().equals(sourceVersion)) {
            throw new ConcurrentUpdateException();
        }

        AccessLevelDict accessLevelDict = accessLevelDictRepository.findByAccessLevelName(accessLevelName)
                .orElseThrow(AccessLevelDictNotFoundException::new);

        var existingAccessLevelOpt = account.getAccessLevelList().stream()
                .filter(al -> al.getAccessLevelDict() != null &&
                        al.getAccessLevelDict().getId().equals(accessLevelDict.getId()))
                .findFirst();

        if (existingAccessLevelOpt.isPresent()) {
            AccessLevel existingAccessLevel = existingAccessLevelOpt.get();
            if (existingAccessLevel.getActive()) {
                throw new AccessLevelAlreadyAssignedException();
            } else {
                existingAccessLevel.setActive(true);
                accountRepository.saveAndFlush(account);
                return;
            }
        }

        AccessLevel newAccessLevel = switch (accessLevelName) {
            case AccountConsts.ROLE_ADMIN -> new Admin();
            case AccountConsts.ROLE_STUDENT -> new Student();
            case AccountConsts.ROLE_TEACHER -> new Teacher();
            default -> throw new AccessLevelDictNotFoundException();
        };

        newAccessLevel.setAccessLevelDict(accessLevelDict);
        newAccessLevel.setAccount(account);
        newAccessLevel.setActive(true);

        account.getAccessLevelList().add(newAccessLevel);
        accountRepository.saveAndFlush(account);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Retryable(
            retryFor = {JpaSystemException.class, ConcurrentUpdateException.class},
            backoff = @Backoff(delayExpression = "${app.retry.backoff}"),
            maxAttemptsExpression = "${app.retry.maxattempts}"
    )
    public void revokeAccessLevel(UUID uuid, String accessLevelName, Integer sourceVersion) {
        Account account = accountRepository.findById(uuid)
                .orElseThrow(AccountNotFoundException::new);

        if (!account.getVersion().equals(sourceVersion)) {
            throw new ConcurrentUpdateException();
        }

        AccessLevelDict accessLevelDict = accessLevelDictRepository.findByAccessLevelName(accessLevelName)
                .orElseThrow(AccessLevelDictNotFoundException::new);

        AccessLevel accessLevelToRevoke = account.getAccessLevelList().stream()
                .filter(al -> al.getAccessLevelDict() != null &&
                        al.getAccessLevelDict().getId().equals(accessLevelDict.getId()))
                .findFirst()
                .orElseThrow(AccessLevelNotAssignedException::new);

        if (!accessLevelToRevoke.getActive()) {
            throw new AccessLevelAlreadyRevokedException();
        }

        accessLevelToRevoke.setActive(false);

        accountRepository.saveAndFlush(account);
    }

    @MethodCallLogged
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Retryable(retryFor = ConcurrentUpdateException.class)
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void blockUserAccount(UUID id) {
        Account account = accountRepository.findById(id).orElseThrow(AccountNotFound::new);

        if (account.getIsBlocked()) {
            throw new AccountIsBlockedException();
        }

        account.setIsBlocked(true);
        accountRepository.saveAndFlush(account);
        emailService.sendAccountIsBlockedInfo(account.getEmail(), account.getLogin());
    }

    @MethodCallLogged
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Retryable(retryFor = ConcurrentUpdateException.class)
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void unblockUserAccount(UUID id) {
        Account account = accountRepository.findById(id).orElseThrow(AccountNotFound::new);

        if (!account.getIsBlocked()) {
            throw new AccountIsNotBlockedException();
        }

        account.setIsBlocked(false);
        accountRepository.saveAndFlush(account);
        emailService.sendAccountIsUnblockedInfo(account.getEmail(), account.getLogin());
    }

    @MethodCallLogged
    @Retryable(retryFor = ConcurrentUpdateException.class)
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void createUrlOwnEmailChange(String login) {
        Account account = accountRepository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException());

        String generateUrl = UUID.randomUUID().toString();
        OneTimeTokenEnt token = new OneTimeTokenEnt();

        token.setTokenValue(generateUrl);
        token.setUsername(login);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(OneTimeTokenConsts.EXPIRATION_TIME_MINUTES));
        token.setUsed(false);

        tokenRepository.saveAndFlush(token);

        String changeUrl = frontendUrl + "/change-email?token=" + generateUrl;
        emailService.sendUrlToChangeOwnEmail(account.getEmail(), account.getLogin(), changeUrl);
    }

    @MethodCallLogged
    @Retryable(retryFor = ConcurrentUpdateException.class)
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void confirmOwnEmailChange(String token, String password, String newEmail) {

        OneTimeTokenEnt tokenEntity = tokenRepository
                .findByTokenValueAndIsUsedIsFalseAndExpiresAtAfter(token, LocalDateTime.now())
                .orElseThrow(() -> new OneTimeTokenInvalidException("Token is invalid"));

        Account account = accountRepository.findByLogin(tokenEntity.getUsername())
                .orElseThrow(() -> new AccountNotFoundException());

        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            throw new InvalidCredentialsProvidedException();
        }

        account.setEmail(newEmail);
        tokenEntity.setUsed(true);

        accountRepository.saveAndFlush(account);
        tokenRepository.saveAndFlush(tokenEntity);
    }
}