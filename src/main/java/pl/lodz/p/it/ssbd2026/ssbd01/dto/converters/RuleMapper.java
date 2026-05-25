package pl.lodz.p.it.ssbd2026.ssbd01.dto.converters;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.RulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;

@Mapper(componentModel = "spring")
public interface RuleMapper {
    @Mapping(target = "raportLevelName", source = "rulePreset.raportLevel.name")
    RulePresetDTO toRuleDTO(RulePreset rulePreset);
}