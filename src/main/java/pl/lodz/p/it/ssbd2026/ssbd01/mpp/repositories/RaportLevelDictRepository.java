package pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories;

import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RaportLevelDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;

import java.util.Optional;

public interface RaportLevelDictRepository extends AbstractRepository<RaportLevelDict> {
    Optional<RaportLevelDict> findByName(String name);

}
