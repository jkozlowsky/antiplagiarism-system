package pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories;

import pl.lodz.p.it.ssbd2026.ssbd01.entities.ReportLevelDict;

import java.util.Optional;

public interface RaportLevelTestRepository extends BaseTestRepository<ReportLevelDict> {
    Optional<ReportLevelDict> findByName(String name);
}
