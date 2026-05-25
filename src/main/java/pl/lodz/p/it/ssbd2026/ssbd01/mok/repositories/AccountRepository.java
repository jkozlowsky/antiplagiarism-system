package pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories;

import jakarta.persistence.QueryHint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts.EMAIL_LENGTH;
import static pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts.LOGIN_LENGTH;


@Repository
@Transactional(propagation = Propagation.MANDATORY, transactionManager = "mokTransactionManager")
public interface AccountRepository extends AbstractRepository<Account> {
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true, transactionManager = "mokTransactionManager")
    Optional<Account> findByActivationToken(String activationToken);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true, transactionManager = "mokTransactionManager")
    List<Account> findAllByIsVerifiedFalseAndSecondEmailSentFalseAndCreationDateBefore(LocalDateTime date);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true, transactionManager = "mokTransactionManager")
    List<Account> findAllByIsVerifiedFalseAndCreationDateBefore(LocalDateTime date);

    @EntityGraph(attributePaths = {"createdBy", "modifiedBy", "accessLevelList"})
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true, transactionManager = "mokTransactionManager")
    Optional<Account> findByLogin(@NotBlank @Size(max = LOGIN_LENGTH) String login);

    @EntityGraph(attributePaths = {"createdBy", "modifiedBy", "accessLevelList"})
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true, transactionManager = "mokTransactionManager")
    Optional<Account> findByEmail(@Email @Size(max = EMAIL_LENGTH) String email);

    @EntityGraph(attributePaths = {"createdBy", "modifiedBy", "accessLevelList"})
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true, transactionManager = "mokTransactionManager")
    Optional<Account> findById(UUID id);

    @EntityGraph(attributePaths = {"accessLevelList", "accessLevelList.accessLevelDict", "createdBy", "modifiedBy"})
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true, transactionManager = "mokTransactionManager")
    Page<Account> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"accessLevelList", "accessLevelList.accessLevelDict", "createdBy", "modifiedBy"})
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true, transactionManager = "mokTransactionManager")
    Page<Account> findByLoginContainingIgnoreCaseOrEmailContainingIgnoreCaseOrNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(String login, String email, String name, String surname, Pageable pageable);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true, transactionManager = "mokTransactionManager")
    @EntityGraph(attributePaths = {"accessLevelList", "accessLevelList.accessLevelDict"})
    Page<Account> findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(String name, String surname, Pageable pageable);
//    List<Account> findByLoginContainingIgnoreCaseOrEmailContainingIgnoreCase(String login, String email);

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true, transactionManager = "mokTransactionManager")
    List<Account> findByIsBlockedTrueAndFailedAttemptsGreaterThanEqualAndLastLoginFailureDateTimeBefore(Integer failedAttempts, LocalDateTime date);

    @QueryHints(@QueryHint(name = "org.hibernate.flushMode", value = "COMMIT"))
    @Query("SELECT a FROM Account a WHERE a.login = :login")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
    Optional<Account> findAuditorByLogin(@Param("login") String login);

    @Query("SELECT a FROM Account a WHERE a.isBlocked = false AND " +
            "((a.lastLoginSuccessDateTime IS NOT NULL AND a.lastLoginSuccessDateTime < :lastTimeLogged) OR " +
            "(a.lastLoginSuccessDateTime IS NULL AND a.creationDate < :lastTimeLogged))")
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true, transactionManager = "mokTransactionManager")
    List<Account> findInactiveAccountsToBlock(@Param("lastTimeLogged") LocalDateTime lastTimeLogged);
}

