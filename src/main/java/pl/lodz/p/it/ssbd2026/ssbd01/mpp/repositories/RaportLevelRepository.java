package pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories;

import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RaportLevelDict;

import java.util.Optional;

public interface RaportLevelRepository extends AbstractRepository<RaportLevelDict> {
    Optional<RaportLevelDict> findByName(String name);
}
