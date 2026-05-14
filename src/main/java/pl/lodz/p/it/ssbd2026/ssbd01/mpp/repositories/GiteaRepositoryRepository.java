package pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;

@Repository
@Transactional(propagation = Propagation.MANDATORY, transactionManager = "mppTransactionManager")
public interface GiteaRepositoryRepository extends AbstractRepository<Repository> {
}
