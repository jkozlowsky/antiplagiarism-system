package pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(propagation = Propagation.MANDATORY, transactionManager = "mppTransactionManager")
public interface GiteaRepositoryRepository extends AbstractRepository<pl.lodz.p.it.ssbd2026.ssbd01.entities.Repository> {

    List<pl.lodz.p.it.ssbd2026.ssbd01.entities.Repository> findBySubjectId(UUID subjectId);
}
