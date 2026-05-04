package pl.lodz.p.it.ssbd2026.ssbd01.dto.converters;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AccountDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AccountWithAccessLevelsDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.RegisterAccountDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;

@Mapper(componentModel = "spring", uses = {AccessLevelMapper.class})
public interface AccountMapper {

    AccountDTO toAccountDTO(Account account);

    @Mapping(target = "account", source = ".")
    @Mapping(target = "accessLevels", source = "accessLevelList")
    AccountWithAccessLevelsDTO toAccountWithAccessLevelsDTO(Account account);

    @Mapping(target = "name", source = "firstName")
    @Mapping(target = "surname", source = "lastName")
    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    Account toEntity(RegisterAccountDTO dto);
}