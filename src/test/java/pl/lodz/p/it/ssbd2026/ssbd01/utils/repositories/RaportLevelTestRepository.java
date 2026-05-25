package pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories;

import pl.lodz.p.it.ssbd2026.ssbd01.entities.RaportLevelDict;

import java.util.Optional;

public interface RaportLevelTestRepository extends BaseTestRepository<RaportLevelDict> {
    Optional<RaportLevelDict> findByName(String name);
}
