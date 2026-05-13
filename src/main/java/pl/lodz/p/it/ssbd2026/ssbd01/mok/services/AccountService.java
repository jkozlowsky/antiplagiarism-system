package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.common.utils.PostCommitTransactionManager;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.OneTimeTokenConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AccountWithAccessLevelsDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.AccountMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.*;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel.AccessLevelAlreadyAssignedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel.AccessLevelAlreadyRevokedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel.AccessLevelDictNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel.AccessLevelNotAssignedException;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.*;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.PasswordHistory;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.PasswordAlreadyUsedInPastException;
import org.springframework.data.domain.Sort;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.PasswordHistory;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.PasswordAlreadyUsedInPastException;

import java.time.LocalDateTime;
import java.util.UUID;

import static pl.lodz.p.it.ssbd2026.ssbd01.entities.OneTimeTokenType.ACCOUNT_UNBLOCK;
import static pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts.*;

@RequiredArgsConstructor
@Service
@MethodCallLogged
@TransactionLogged
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mokTransactionManager")
@Retryable(retryFor = {JpaSystemException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccessLevelDictRepository accessLevelDictRepository;
    private final EmailService emailService;
    private final OneTimeTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final VersionHashService hashService;
    private final EmailRevertTokenRepository revertTokenRepository;
    private final VersionHashService versionHashService;
    private final MokRefreshRepository mokRefreshRepository;
    private final AccountMapper accountMapper;
    private final PostCommitTransactionManager postCommitTransactionManager;
    private final LanguageDictRepository languageDictRepository;
    @Value("${app.frontend.url}")
    private String frontendUrl;

    @MethodCallLogged
    @PreAuthorize("permitAll()")
    private void validateAndSetNewPassword(Account account, String newPassword) {
        if (passwordEncoder.matches(newPassword, account.getPasswordHash())) {
            throw new PasswordSameAsOldException();
        }

        for (PasswordHistory history : account.getPasswordHistoryList()) {
            if (passwordEncoder.matches(newPassword, history.getPasswordHash())) {
                throw new PasswordAlreadyUsedInPastException();
            }
        }

        String newHash = passwordEncoder.encode(newPassword);
        account.setPasswordHash(newHash);
        account.getPasswordHistoryList().add(new PasswordHistory(account, newHash));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    @MethodCallLogged
    @PreAuthorize("permitAll()")
    public void registerAccount(Account account) {
        account.setPasswordHash(passwordEncoder.encode(account.getPasswordHash()));
        account.getPasswordHistoryList().add(new PasswordHistory(account, account.getPasswordHash()));

        account.setIsVerified(false);
        account.setIsBlocked(false);
        account.setForcePasswordChange(true);
        account.setActivationToken(UUID.randomUUID().toString());
        account.setCreationDate(LocalDateTime.now());
        account.setSecondEmailSent(false);
        account.setFailedAttempts(0);

        LanguageDict defaultLang = languageDictRepository.findByLanguage("pl").orElseThrow(LanguageDictNotFoundException::new);
        account.setLanguageVersion(defaultLang);

        AccessLevelDict studentDict = accessLevelDictRepository.findByAccessLevelName(AccountConsts.ROLE_STUDENT).orElseThrow(AccessLevelDictNotFoundException::new);

        Student studentRole = new Student();
        studentRole.setAccessLevelDict(studentDict);
        studentRole.setAccount(account);
        studentRole.setActive(true);

        account.getAccessLevelList().add(studentRole);

        accountRepository.saveAndFlush(account);
        postCommitTransactionManager.register(() -> emailService.sendActivationEmail(account.getEmail(), account.getActivationToken(), defaultLang.getLanguage()));
    }

    @MethodCallLogged
    @PreAuthorize("permitAll()")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void confirmActivation(String token) {
        Account account = accountRepository.findByActivationToken(token).orElseThrow(AccountActivationException::new);

        if (account.getCreationDate().plusHours(24).isBefore(LocalDateTime.now())) {
            throw new AccountActivationException("error.activation.token.expired");
        }

        account.setIsBlocked(false);
        account.setIsVerified(true);
        account.setActivationToken(null);
        accountRepository.saveAndFlush(account);
    }

    @MethodCallLogged
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mokTransactionManager")
    public Account getAccountWithAccessLevels(UUID id) {
        Account account = accountRepository.findById(id).orElseThrow(AccountNotFoundException::new);
        if (account.getCreatedBy() != null) {
            Hibernate.initialize(account.getCreatedBy());
        }
        if (account.getModifiedBy() != null) {
            Hibernate.initialize(account.getModifiedBy());
        }
        account.getAccessLevelList().forEach(accessLevel -> {
            if (accessLevel.getAccessLevelDict() != null) {
                accessLevel.getAccessLevelDict().getAccessLevelName();
            }
        });

        return account;
    }

    @MethodCallLogged
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public Page<Account> getAllAccounts(Integer page, Integer size, String sortBy, Boolean sortDesc, String username) {
        Account admin = accountRepository.findByLogin(username)
                .orElseThrow(AccountNotFoundException::new);

        updatePreferencesIfProvided(admin, size, sortBy, sortDesc);

        Pageable pageable = createPageable(page, admin);
        return accountRepository.findAll(pageable);
    }

    @MethodCallLogged
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN) or #login == authentication.name")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mokTransactionManager")
    public Account getAccountByLogin(String login) {
        Account account = accountRepository.findByLogin(login).orElseThrow(AccountNotFoundException::new);

        if (account.getCreatedBy() != null) {
            Hibernate.initialize(account.getCreatedBy());
        }
        if (account.getModifiedBy() != null) {
            Hibernate.initialize(account.getModifiedBy());
        }
        account.getAccessLevelList().forEach(accessLevel -> {
            if (accessLevel.getAccessLevelDict() != null) {
                accessLevel.getAccessLevelDict().getAccessLevelName();
            }
        });

        return account;
    }

    @MethodCallLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mokTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public Page<Account> searchAccounts(String phrase, Integer page, Integer size, String sortBy, Boolean sortDesc, String username) {
        Account admin = accountRepository.findByLogin(username)
                .orElseThrow(AccountNotFoundException::new);

        updatePreferencesIfProvided(admin, size, sortBy, sortDesc);

        Pageable pageable = createPageable(page, admin);
        return accountRepository.findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(phrase, phrase, pageable);
    }

    @MethodCallLogged
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public AccountWithAccessLevelsDTO grantAccessLevel(UUID uuid, String accessLevelName, String versionHash) {
        Account account = accountRepository.findById(uuid).orElseThrow(AccountNotFoundException::new);
        mokRefreshRepository.refresh(account);

        if (!versionHash.equals(versionHashService.hashObjectVersion(uuid, account.getVersion()))) {
            throw new ConcurrentUpdateException();
        }

        AccessLevelDict accessLevelDict = accessLevelDictRepository.findByAccessLevelName(accessLevelName).orElseThrow(AccessLevelDictNotFoundException::new);

        var existingAccessLevelOpt = account.getAccessLevelList().stream().filter(al -> al.getAccessLevelDict() != null && al.getAccessLevelDict().getId().equals(accessLevelDict.getId())).findFirst();

        if (existingAccessLevelOpt.isPresent()) {
            AccessLevel existingAccessLevel = existingAccessLevelOpt.get();
            if (existingAccessLevel.getActive()) {
                throw new AccessLevelAlreadyAssignedException();
            } else {
                existingAccessLevel.setActive(true);
                Account saved = accountRepository.saveAndFlush(account);

                postCommitTransactionManager.register(() -> emailService.sendAccessLevelGrantedInfo(account.getEmail(), account.getLogin(), accessLevelName, account.getLanguageVersion().getLanguage()));
                return accountMapper.toAccountWithAccessLevelsAndVersionHashDTO(saved, versionHashService.hashObjectVersion(saved.getId(), saved.getVersion()));
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
        Account saved = accountRepository.saveAndFlush(account);

        postCommitTransactionManager.register(() -> emailService.sendAccessLevelGrantedInfo(account.getEmail(), account.getLogin(), accessLevelName, account.getLanguageVersion().getLanguage()));
        return accountMapper.toAccountWithAccessLevelsAndVersionHashDTO(saved, versionHashService.hashObjectVersion(saved.getId(), saved.getVersion()));
    }

    @MethodCallLogged
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void revokeAccessLevel(UUID uuid, String accessLevelName, String versionHash) {
        Account account = accountRepository.findById(uuid).orElseThrow(AccountNotFoundException::new);
        mokRefreshRepository.refresh(account);

        if (!versionHash.equals(versionHashService.hashObjectVersion(uuid, account.getVersion()))) {
            throw new ConcurrentUpdateException();
        }

        AccessLevelDict accessLevelDict = accessLevelDictRepository.findByAccessLevelName(accessLevelName).orElseThrow(AccessLevelDictNotFoundException::new);

        AccessLevel accessLevelToRevoke = account.getAccessLevelList().stream().filter(al -> al.getAccessLevelDict() != null && al.getAccessLevelDict().getId().equals(accessLevelDict.getId())).findFirst().orElseThrow(AccessLevelNotAssignedException::new);

        if (!accessLevelToRevoke.getActive()) {
            throw new AccessLevelAlreadyRevokedException();
        }

        accessLevelToRevoke.setActive(false);

        accountRepository.saveAndFlush(account);

        postCommitTransactionManager.register(() -> emailService.sendAccessLevelRevokedInfo(account.getEmail(), account.getLogin(), accessLevelName, account.getLanguageVersion().getLanguage()));
    }

    @MethodCallLogged
    @Retryable(retryFor = {JpaSystemException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
    public Account changeEmailByAdmin(String login, String newEmail, String frontendVersionHash) {
        Account account = accountRepository.findByLogin(login).orElseThrow(AccountNotFoundException::new);
        mokRefreshRepository.refresh(account);
        String expectedVersionHash = hashService.hashObjectVersion(account.getId(), account.getVersion());
        if (account.getCreatedBy() != null) {
            Hibernate.initialize(account.getCreatedBy());
        }
        if (account.getModifiedBy() != null) {
            Hibernate.initialize(account.getModifiedBy());
        }
        if (!expectedVersionHash.equals(frontendVersionHash)) {
            throw new ConcurrentUpdateException();
        }
        String oldEmail = account.getEmail();
        account.setEmail(newEmail);

        postCommitTransactionManager.register(() -> emailService.sendEmailAddressChangedByAdmin(oldEmail, account.getLanguageVersion().getLanguage()));
        postCommitTransactionManager.register(() -> emailService.sendEmailAddressChangedByAdmin(newEmail, account.getLanguageVersion().getLanguage()));

        return accountRepository.saveAndFlush(account);
    }

    @MethodCallLogged
    @Retryable(retryFor = {ConcurrentUpdateException.class}, backoff = @Backoff(delay = 1000))
    @PreAuthorize("isAuthenticated()")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mokTransactionManager")
    public void changeOwnPassword(String login, String oldPassword, String newPassword, String ifMatch) {

        Account account = accountRepository.findByLogin(login)
                .orElseThrow(AccountNotFoundException::new);
        mokRefreshRepository.refresh(account);
        if (!ifMatch.equals(versionHashService.hashObjectVersion(account.getId(), account.getVersion()))) {
            throw new ConcurrentUpdateException();
        }

        if (!passwordEncoder.matches(oldPassword, account.getPasswordHash())) {
            throw new InvalidOldPasswordException();
        }

        validateAndSetNewPassword(account, newPassword);
        account.setForcePasswordChange(false);
        accountRepository.saveAndFlush(account);
    }


    @MethodCallLogged
    @Retryable(retryFor = ConcurrentUpdateException.class)
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void blockUserAccount(UUID id, String versionHash) {
        Account account = accountRepository.findById(id).orElseThrow(AccountNotFoundException::new);

        mokRefreshRepository.refresh(account);
        if (!versionHash.equals(versionHashService.hashObjectVersion(id, account.getVersion()))) {
            throw new ConcurrentUpdateException();
        }

        if (account.getIsBlocked()) {
            throw new AccountIsBlockedException();
        }

        account.setIsBlocked(true);
        accountRepository.saveAndFlush(account);
        postCommitTransactionManager.register(() ->
                emailService.sendAccountIsBlockedInfo(account.getEmail(), account.getLogin(), account.getLanguageVersion().getLanguage()));
    }

    @MethodCallLogged
    @Retryable(retryFor = ConcurrentUpdateException.class)
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void unblockUserAccount(UUID id, String versionHash) {
        Account account = accountRepository.findById(id).orElseThrow(AccountNotFoundException::new);

        mokRefreshRepository.refresh(account);
        if (!versionHash.equals(versionHashService.hashObjectVersion(id, account.getVersion()))) {
            throw new ConcurrentUpdateException();
        }

        if (!account.getIsBlocked()) {
            throw new AccountIsNotBlockedException();
        }

        account.setIsBlocked(false);
        accountRepository.saveAndFlush(account);
        postCommitTransactionManager.register(() ->
                emailService.sendAccountIsUnblockedInfo(account.getEmail(), account.getLogin(), account.getLanguageVersion().getLanguage()));
    }

    @MethodCallLogged
    @Retryable(retryFor = ConcurrentUpdateException.class)
    @PreAuthorize("isAuthenticated()")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void createUrlOwnEmailChange(String login) {
        Account account = accountRepository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException());

//        mokRefreshRepository.refresh(account);
//        if (!versionHash.equals(versionHashService.hashObjectVersion(account.getId(), account.getVersion()))) {
//            throw new ConcurrentUpdateException();
//        }

        String generateUrl = UUID.randomUUID().toString();
        OneTimeTokenEnt token = new OneTimeTokenEnt();
        token.setTokenValue(generateUrl);
        token.setUsername(login);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(OneTimeTokenConsts.EXPIRATION_TIME_MINUTES));
        token.setTokenType(OneTimeTokenType.OWN_EMAIL_CHANGE);
        token.setIsUsed(false);

        tokenRepository.saveAndFlush(token);

        String changeUrl = frontendUrl + "/change-email?token=" + generateUrl;
        postCommitTransactionManager.register(() ->
                emailService.sendUrlToChangeOwnEmail(account.getEmail(), account.getLogin(), changeUrl, account.getLanguageVersion().getLanguage()));
    }

    @MethodCallLogged
    @Retryable(retryFor = ConcurrentUpdateException.class)
    @PreAuthorize("permitAll()")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void confirmOwnEmailChange(String token, String password, String newEmail) {

        OneTimeTokenEnt tokenEntity = tokenRepository
                .findByTokenValueAndTokenTypeAndIsUsedIsFalseAndExpiresAtAfter(token, OneTimeTokenType.OWN_EMAIL_CHANGE, LocalDateTime.now())
                .orElseThrow(OneTimeTokenInvalidException::new);

        Account account = accountRepository.findByLogin(tokenEntity.getUsername())
                .orElseThrow(AccountNotFoundException::new);

        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            throw new InvalidCredentialsProvidedException();
        }

        String oldEmail = account.getEmail();
        account.setEmail(newEmail);
        tokenEntity.setIsUsed(true);

        String revertUrlToken = UUID.randomUUID().toString();
        EmailRevertTokenEnt revertToken = new EmailRevertTokenEnt();
        revertToken.setTokenValue(revertUrlToken);
        revertToken.setUsername(account.getLogin());
        revertToken.setOldEmail(oldEmail);
        revertToken.setExpiresAt(LocalDateTime.now().plusMinutes(OneTimeTokenConsts.EXPIRATION_TIME_MINUTES));
        revertToken.setUsed(false);

        accountRepository.saveAndFlush(account);
        tokenRepository.saveAndFlush(tokenEntity);
        revertTokenRepository.saveAndFlush(revertToken);

        String revertUrl = frontendUrl + "/revert-email?token=" + revertUrlToken;
        postCommitTransactionManager.register(() ->
                emailService.sendComebackEmailAddressLink(oldEmail, account.getLogin(), newEmail, revertUrl, account.getLanguageVersion().getLanguage()));
    }

    @MethodCallLogged
    @Retryable(retryFor = ConcurrentUpdateException.class)
    @PreAuthorize("permitAll()")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void revertEmailChange(String token) {
        EmailRevertTokenEnt revertToken = revertTokenRepository.findByTokenValue(token)
                .orElseThrow(OneTimeTokenInvalidException::new);

        if (revertToken.isUsed() || revertToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new OneTimeTokenInvalidException();
        }

        Account account = accountRepository.findByLogin(revertToken.getUsername())
                .orElseThrow(AccountNotFoundException::new);

        account.setEmail(revertToken.getOldEmail());
        revertToken.setUsed(true);

        accountRepository.saveAndFlush(account);
        revertTokenRepository.saveAndFlush(revertToken);
    }

    @MethodCallLogged
    @Retryable(retryFor = ConcurrentUpdateException.class)
    @PreAuthorize("isAuthenticated()")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void resendUrlOwnEmailChange(String login) {
        tokenRepository.findByUsernameAndTokenTypeAndIsUsedIsFalse(login, OneTimeTokenType.OWN_EMAIL_CHANGE).forEach(token -> {
            token.setIsUsed(true);
            tokenRepository.save(token);
        });

        tokenRepository.flush();
        createUrlOwnEmailChange(login);
    }

    @MethodCallLogged
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public AccountWithAccessLevelsDTO updateAccount(UUID id, String versionHash, Account source) {
        Account target = accountRepository.findById(id).orElseThrow(AccountNotFoundException::new);
        mokRefreshRepository.refresh(target);

        if (!versionHash.equals(versionHashService.hashObjectVersion(id, target.getVersion()))) {
            throw new ConcurrentUpdateException();
        }

        accountMapper.updateExistingAccount(source, target);
        Account savedAccount = accountRepository.saveAndFlush(target);
        return accountMapper.toAccountWithAccessLevelsDTO(savedAccount);
    }

    @MethodCallLogged
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN) or #login == authentication.name")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public AccountWithAccessLevelsDTO updateAccountByLogin(String login, String versionHash, Account source) {
        Account target = accountRepository.findByLogin(login).orElseThrow(AccountNotFoundException::new);
        mokRefreshRepository.refresh(target);
        if (!versionHash.equals(versionHashService.hashObjectVersion(target.getId(), target.getVersion()))) {
            throw new ConcurrentUpdateException();
        }

        accountMapper.updateExistingAccount(source, target);
        Account savedAccount = accountRepository.saveAndFlush(target);
        return accountMapper.toAccountWithAccessLevelsDTO(savedAccount);
    }

    @MethodCallLogged
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Retryable(retryFor = ConcurrentUpdateException.class)
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void changeOtherPassword(String login, String newPassword, String ifMatch) {
        Account account = accountRepository.findByLogin(login).orElseThrow(AccountNotFoundException::new);

        mokRefreshRepository.refresh(account);
        if (!ifMatch.equals(versionHashService.hashObjectVersion(account.getId(), account.getVersion()))) {
            throw new ConcurrentUpdateException();
        }

        validateAndSetNewPassword(account, newPassword);
        account.setForcePasswordChange(true);
        accountRepository.saveAndFlush(account);

    }

    private void updatePreferencesIfProvided(Account admin, Integer size, String sortBy, Boolean sortDesc) {
        boolean updated = false;

        if (size != null && !size.equals(admin.getListPageSize())) {
            admin.setListPageSize(size);
            updated = true;
        }
        if (sortBy != null && !sortBy.equals(admin.getListSortBy())) {
            admin.setListSortBy(sortBy);
            updated = true;
        }
        if (sortDesc != null && !sortDesc.equals(admin.getListSortDesc())) {
            admin.setListSortDesc(sortDesc);
            updated = true;
        }

        if (updated) {
            accountRepository.saveAndFlush(admin);
        }
    }

    private Pageable createPageable(Integer page, Account admin) {
        int finalPage = (page != null) ? page : DEFAULT_PAGE;
        int finalSize = (admin.getListPageSize() != null) ? admin.getListPageSize() : DEFAULT_PAGE_SIZE;

        String sortByField = (admin.getListSortBy() != null) ? admin.getListSortBy() : DEFAULT_SORT_BY;
        if (SORT_ACCESS_LEVEL_ALIAS.equals(sortByField)) {
            sortByField = SORT_ACCESS_LEVEL_PATH;
        }

        boolean isDescending = Boolean.TRUE.equals(admin.getListSortDesc());
        Sort.Direction direction = isDescending ? Sort.Direction.DESC : Sort.Direction.ASC;

        return PageRequest.of(finalPage, finalSize, Sort.by(direction, sortByField));
    }

    @MethodCallLogged
    @Retryable(retryFor = ConcurrentUpdateException.class)
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    public void unblockAccountViaEmailToken(String token) {
        OneTimeTokenEnt tokenEntity = tokenRepository
                .findByTokenValueAndTokenTypeAndIsUsedIsFalseAndExpiresAtAfter(
                        token, ACCOUNT_UNBLOCK, LocalDateTime.now())
                .orElseThrow(OneTimeTokenInvalidException::new);

        Account account = accountRepository.findByLogin(tokenEntity.getUsername())
                .orElseThrow(AccountNotFoundException::new);

        if (!account.getIsBlocked()) {
            throw new AccountIsNotBlockedException();
        }

        account.setIsBlocked(false);
        account.setLastLoginSuccessDateTime(LocalDateTime.now());
        tokenEntity.setIsUsed(true);

        accountRepository.saveAndFlush(account);
        tokenRepository.saveAndFlush(tokenEntity);

        postCommitTransactionManager.register(() ->
                emailService.sendAccountIsUnblockedInfo(account.getEmail(), account.getLogin(), account.getLanguageVersion().getLanguage()));
    }
}