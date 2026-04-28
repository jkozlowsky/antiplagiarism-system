package pl.lodz.p.it.ssbd2026.ssbd01.dto.converters;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AccountDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AccountWithAccessLevelsDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;

@Mapper(componentModel = "spring", uses = {AccessLevelMapper.class})
public interface AccountMapper {

    AccountDTO toAccountDTO(Account account);

    @Mapping(target = "account", source = ".")
    @Mapping(target = "accessLevels", source = "accessLevelList")
    AccountWithAccessLevelsDTO toAccountWithAccessLevelsDTO(Account account);

}