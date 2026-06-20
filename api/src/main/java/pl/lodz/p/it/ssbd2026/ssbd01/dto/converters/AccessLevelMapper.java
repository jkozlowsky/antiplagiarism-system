package pl.lodz.p.it.ssbd2026.ssbd01.dto.converters;

import org.mapstruct.Mapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AccessLevelDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.AccessLevel;

@Mapper(componentModel = "spring")
public interface AccessLevelMapper {
    AccessLevelDTO toAccessLevelDTO(AccessLevel accessLevel);
}