package pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories;

import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;

import java.util.List;
import java.util.UUID;

public interface RulePresetTestRepository extends BaseTestRepository<RulePreset> {
    List<RulePreset> findBySubjectId(UUID subjectId);
}
