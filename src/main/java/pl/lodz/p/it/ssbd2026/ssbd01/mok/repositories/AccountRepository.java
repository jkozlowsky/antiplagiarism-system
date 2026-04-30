package pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;

import java.util.Optional;
import java.util.UUID;

import static pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts.EMAIL_LENGTH;
import static pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts.LOGIN_LENGTH;

@Repository
@Transactional(propagation = Propagation.MANDATORY, transactionManager = "mokTransactionManager")
public interface AccountRepository extends AbstractRepository<Account> {

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY)
    Optional<Account> findByLogin(@NotBlank @Size(max = LOGIN_LENGTH) String login);

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY)
    Optional<Account> findByEmail(@Email @Size(max = EMAIL_LENGTH) String email);

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY)
    Optional<Account> findById(UUID id);
}
