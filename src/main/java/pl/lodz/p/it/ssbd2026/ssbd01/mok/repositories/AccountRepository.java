package pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories;

import org.springframework.stereotype.Repository;
import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends AbstractRepository<Account> {
    Optional<Account> findByActivationToken(String activationToken);
    List<Account> findAllByIsVerifiedFalseAndSecondEmailSentFalseAndCreationDateBefore(LocalDateTime date);
    List<Account> findAllByIsVerifiedFalseAndCreationDateBefore(LocalDateTime date);
}