package pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.EmailRevertTokenEnt;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailRevertTokenRepository extends JpaRepository<EmailRevertTokenEnt, UUID> {
    Optional<EmailRevertTokenEnt> findByTokenValue(String tokenValue);
}