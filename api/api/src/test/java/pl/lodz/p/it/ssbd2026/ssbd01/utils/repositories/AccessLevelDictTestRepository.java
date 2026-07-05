package pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories;

import pl.lodz.p.it.ssbd2026.ssbd01.entities.AccessLevelDict;
import java.util.Optional;

public interface AccessLevelDictTestRepository extends BaseTestRepository<AccessLevelDict> {
    Optional<AccessLevelDict> findByAccessLevelName(String name);
}