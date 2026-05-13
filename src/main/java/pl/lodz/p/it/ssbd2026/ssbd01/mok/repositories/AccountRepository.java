package pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
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
    Optional<Account> findByActivationToken(String activationToken);
    List<Account> findAllByIsVerifiedFalseAndSecondEmailSentFalseAndCreationDateBefore(LocalDateTime date);
    List<Account> findAllByIsVerifiedFalseAndCreationDateBefore(LocalDateTime date);

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY)
    Optional<Account> findByLogin(@NotBlank @Size(max = LOGIN_LENGTH) String login);

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY)
    Optional<Account> findByEmail(@Email @Size(max = EMAIL_LENGTH) String email);

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY)
    Optional<Account> findById(UUID id);

    @EntityGraph(attributePaths = {"accessLevelList", "accessLevelList.accessLevelDict"})
    @Transactional(readOnly = true, propagation = Propagation.MANDATORY)
    Page<Account> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"accessLevelList", "accessLevelList.accessLevelDict"})
    @Transactional(readOnly = true, propagation = Propagation.MANDATORY)
    Page<Account> findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(String name, String surname, Pageable pageable);
    List<Account> findByLoginContainingIgnoreCaseOrEmailContainingIgnoreCase(String login, String email);

    UUID id(UUID id);

    List<Account> findByIsBlockedTrueAndFailedAttemptsGreaterThanEqualAndLastLoginFailureDateTimeBefore(Integer failedAttempts, LocalDateTime date);

    @Query("SELECT a FROM Account a WHERE a.isBlocked = false AND " +
            "((a.lastLoginSuccessDateTime IS NOT NULL AND a.lastLoginSuccessDateTime < :lastTimeLogged) OR " +
            "(a.lastLoginSuccessDateTime IS NULL AND a.creationDate < :lastTimeLogged))")
    List<Account> findInactiveAccountsToBlock(@Param("lastTimeLogged") LocalDateTime lastTimeLogged);
}

