package pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.OneTimeTokenEnt;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.OneTimeTokenType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Component
@Transactional(propagation = Propagation.MANDATORY, transactionManager = "mokTransactionManager")
public interface OneTimeTokenRepository extends AbstractRepository<OneTimeTokenEnt> {
    Optional<OneTimeTokenEnt> findByTokenValueAndTokenTypeAndIsUsedIsFalseAndExpiresAtAfter(String token, OneTimeTokenType tokenType, LocalDateTime now);

    //Blokujemy wpisy które znajdziemy żeby wyeliminować wyścig związany z jednoczesnym wczytaniem i tworzeniem tokenu.
    //Nie do końca wiem czy jest to potrzebne ale chyba nic złego nie robi ¯\_(ツ)_/¯
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<OneTimeTokenEnt> findByUsernameAndTokenTypeAndIsUsedIsFalse(String username, OneTimeTokenType tokenType);

    void deleteByExpiresAtBefore(LocalDateTime expiresAt);
}
