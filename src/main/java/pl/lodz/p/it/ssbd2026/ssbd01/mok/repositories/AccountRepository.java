package pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories;


import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY, transactionManager = "mokTransactionManager")
public interface AccountRepository extends AbstractRepository<Account> {
    Optional<Account> findByEmail(String email);

    Optional<Account> findByLogin(String login);

    List<Account> findByLoginContainingIgnoreCaseOrEmailContainingIgnoreCase(String login, String email);
}
