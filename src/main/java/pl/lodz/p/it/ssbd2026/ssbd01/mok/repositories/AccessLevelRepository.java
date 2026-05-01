package pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories;

import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.AccessLevel;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.AccessLevelDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;

import java.util.Optional;

public interface AccessLevelRepository extends AbstractRepository<AccessLevel> {
    Optional<AccessLevel> findByAccessLevelDictAndAccount(AccessLevelDict accessLevelDict, Account account);
}
