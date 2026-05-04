package pl.lodz.p.it.ssbd2026.ssbd01.dto.converters;

import org.mapstruct.*;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AccountDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AccountWithAccessLevelsDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.RegisterAccountDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;

@Mapper(componentModel = "spring", uses = {AccessLevelMapper.class})
public interface AccountMapper {


    @Mapping(target = "password", ignore = true)
    @Mapping(target = "versionHash", source = "versionHash")
    AccountDTO toAccountDTO(Account account, String versionHash);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "versionHash", ignore = true)
    AccountDTO toAccountDTONoHash(Account account);

    @Mapping(target = "account", expression = "java(toAccountDTO(account, versionHash))")
    @Mapping(target = "accessLevels", source = "account.accessLevelList")
    AccountWithAccessLevelsDTO toAccountWithAccessLevelsAndVersionHashDTO(Account account, String versionHash);

    @Mapping(target = "account", source = ".")
    @Mapping(target = "accessLevels", source = "account.accessLevelList")
    AccountWithAccessLevelsDTO toAccountWithAccessLevelsDTO(Account account);

    @Mapping(target = "name", source = "firstName")
    @Mapping(target = "surname", source = "lastName")
    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    Account toEntity(RegisterAccountDTO dto);

    Account toUpdateAccount(AccountDTO accountDTO);
    @BeanMapping(ignoreByDefault = true, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "name"),
            @Mapping(target = "surname"),
    })
    public void updateExistingAccount(Account accountUpdate, @MappingTarget Account account);
}