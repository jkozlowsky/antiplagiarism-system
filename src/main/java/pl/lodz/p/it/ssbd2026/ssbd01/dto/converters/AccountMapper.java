package pl.lodz.p.it.ssbd2026.ssbd01.dto.converters;

import org.mapstruct.*;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AccountDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AccountWithAccessLevelsDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.RegisterAccountDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.projections.AccountListProjectionDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;

@Mapper(componentModel = "spring", uses = {AccessLevelMapper.class})
public interface AccountMapper {

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "versionHash", source = "versionHash")
    @Mapping(source = "account.createdBy.login", target = "createdBy")
    @Mapping(source = "account.modifiedBy.login", target = "modifiedBy")
    AccountDTO toAccountDTO(Account account, String versionHash);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "versionHash", ignore = true)
    @Mapping(source = "createdBy.login", target = "createdBy")
    @Mapping(source = "modifiedBy.login", target = "modifiedBy")
    AccountDTO toAccountDTONoHash(Account account);

    @Mapping(target = "account", expression = "java(toAccountDTO(account, versionHash))")
    @Mapping(target = "accessLevels", source = "account.accessLevelList")
    AccountWithAccessLevelsDTO toAccountWithAccessLevelsAndVersionHashDTO(Account account, String versionHash);

    @Mapping(target = "account", source = ".")
    @Mapping(target = "accessLevels", source = "account.accessLevelList")
    AccountWithAccessLevelsDTO toAccountWithAccessLevelsDTO(Account account);

    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    Account toEntity(RegisterAccountDTO dto);

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    Account toUpdateAccount(AccountDTO accountDTO);

    @Mapping(target = "id", source = "proj.id")
    @Mapping(target = "login", source = "proj.login")
    @Mapping(target = "name", source = "proj.name")
    @Mapping(target = "surname", source = "proj.surname")
    @Mapping(target = "email", source = "proj.email")
    @Mapping(target = "lastLoginSuccessDateTime", source = "proj.lastLoginSuccessDateTime")
    @Mapping(target = "versionHash", source = "versionHash")
    AccountDTO projectionToAccountDTO(AccountListProjectionDTO proj, String versionHash);

    @BeanMapping(ignoreByDefault = true, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "name"),
            @Mapping(target = "surname"),
    })
    public void updateExistingAccount(Account accountUpdate, @MappingTarget Account account);
}