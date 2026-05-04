package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.*;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccessLevelDictRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccessLevelRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.LanguageDictRepository;

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
    @Autowired
    private final AccessLevelDictRepository accessLevelDictRepository;
    private final AccessLevelRepository accessLevelRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LanguageDictRepository languageDictRepository;

    @Autowired
    private EmailService emailService;

    @Transactional(propagation =  Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void registerAccount(Account account) {
        account.setPasswordHash(passwordEncoder.encode(account.getPasswordHash()));

        account.setIsVerified(false);
        account.setIsBlocked(false);
        account.setActivationToken(UUID.randomUUID().toString());
        account.setCreationDate(LocalDateTime.now());
        account.setSecondEmailSent(false);
        account.setFailedAttempts(0);

        LanguageDict defaultLang = languageDictRepository.findByLanguage("pl")
                .orElseThrow(LanguageDictNotFoundException::new);
        account.setLanguageVersion(defaultLang);

        AccessLevelDict studentDict = accessLevelDictRepository.findByAccessLevelName(AccountConsts.ROLE_STUDENT)
                .orElseThrow(AccessLevelDictNotFoundException::new);

        Student studentRole = new Student();
        studentRole.setAccessLevelDict(studentDict);
        studentRole.setAccount(account);
        studentRole.setIsActive(true);

        account.getAccessLevelList().add(studentRole);

        accountRepository.saveAndFlush(account);
        emailService.sendActivationEmail(account.getEmail(), account.getActivationToken());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void confirmActivation(String token) {
        Account account = accountRepository.findByActivationToken(token)
                .orElseThrow(AccountActivationException::new);

        if (account.getCreationDate().plusHours(24).isBefore(LocalDateTime.now())) {
            throw new AccountActivationException("error.activation.token.expired");
        }

        account.setIsBlocked(false);
        account.setIsVerified(true);
        account.setActivationToken(null);
        accountRepository.saveAndFlush(account);
    }

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