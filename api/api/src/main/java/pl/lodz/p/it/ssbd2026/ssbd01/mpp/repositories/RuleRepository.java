package pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;

import java.util.List;

@JaversSpringDataAuditable
@Repository
@Transactional(propagation = Propagation.MANDATORY, transactionManager = "mppTransactionManager")
public interface RuleRepository extends AbstractRepository<RulePreset> {

    List<RulePreset> findAllByIsPreset(Boolean isPreset);
}
