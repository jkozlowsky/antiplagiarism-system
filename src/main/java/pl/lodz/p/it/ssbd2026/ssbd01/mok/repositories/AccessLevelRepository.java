package pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.projections.AccountRoleProjectionDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.AccessLevel;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.AccessLevelDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@JaversSpringDataAuditable
@Repository
@Transactional(propagation = Propagation.MANDATORY, transactionManager = "mokTransactionManager")
public interface AccessLevelRepository extends AbstractRepository<AccessLevel> {
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true, transactionManager = "mokTransactionManager")
    Optional<AccessLevel> findByAccessLevelDictAndAccount(AccessLevelDict accessLevelDict, Account account);

    @Query("""  
    SELECT new pl.lodz.p.it.ssbd2026.ssbd01.dto.projections.AccountRoleProjectionDTO(        al.account.id, al.id, al.accessLevelDict.accessLevelName, al.active, al.version    )    FROM AccessLevel al   
    WHERE al.account.id IN :accountIds  
""")
    List<AccountRoleProjectionDTO> findRolesByAccountIds(@Param("accountIds") List<UUID> accountIds);
}
