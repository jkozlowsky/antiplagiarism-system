package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccessLevelAlreadyAssignedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccessLevelDictNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccountNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccessLevelDictRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccessLevelRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;

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
    private final AccessLevelRepository accessLevelRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mokTransactionManager")
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

    /**
     * Nadaje poziom dostępu do konta na podstawie UUID konta i nazwy poziomu dostępu.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
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

        if (accessLevelRepository.findByAccessLevelDictAndAccount(accessLevelDict, account).isPresent()) {
            throw new AccessLevelAlreadyAssignedException();
        }

        AccessLevel newAccessLevel = createAccessLevel(accessLevelName);

        newAccessLevel.setAccessLevelDict(accessLevelDict);
        newAccessLevel.setAccount(account);

        account.getAccessLevelList().add(newAccessLevel);
        accountRepository.saveAndFlush(account);
    }

    private static @NonNull AccessLevel createAccessLevel(String accessLevelName) {
        return switch (accessLevelName) {
            case AccountConsts.ROLE_ADMIN -> new Admin();
            case AccountConsts.ROLE_STUDENT -> new Student();
            case AccountConsts.ROLE_TEACHER -> new Teacher();
            default -> throw new AccessLevelDictNotFoundException();
        };
    }
}