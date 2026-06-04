package pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories;

import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.ReportLevelDict;

import java.util.Optional;

public interface RaportLevelDictRepository extends AbstractRepository<ReportLevelDict> {
    Optional<ReportLevelDict> findByName(String name);

}
