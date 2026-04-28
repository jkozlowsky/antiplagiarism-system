package pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories;


import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;

import java.util.List;
import java.util.Optional;

@Transactional(propagation = Propagation.MANDATORY, transactionManager = "mokTransactionManager")
public interface UserRepository extends AbstractRepository<Account> {
    Optional<Account> findByEmail(String email);
}
