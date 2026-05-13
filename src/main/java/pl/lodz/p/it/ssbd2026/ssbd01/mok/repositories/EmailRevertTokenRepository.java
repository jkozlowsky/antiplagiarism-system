package pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.EmailRevertTokenEnt;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(propagation = Propagation.MANDATORY, transactionManager = "mokTransactionManager")
public interface EmailRevertTokenRepository extends JpaRepository<EmailRevertTokenEnt, UUID> {
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true, transactionManager = "mokTransactionManager")
    Optional<EmailRevertTokenEnt> findByTokenValue(String tokenValue);
}