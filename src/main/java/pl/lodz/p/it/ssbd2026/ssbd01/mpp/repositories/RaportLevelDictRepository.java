package pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories;

import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RaportLevelDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;

public interface RaportLevelDictRepository extends AbstractRepository<RaportLevelDict> {
    RaportLevelDict findByName(String name);
}
