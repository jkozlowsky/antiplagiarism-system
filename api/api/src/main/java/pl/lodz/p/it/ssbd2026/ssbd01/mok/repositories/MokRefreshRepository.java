package pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.AbstractEntity;

@Repository
@Transactional(propagation = Propagation.MANDATORY, transactionManager = "mokTransactionManager")
public class MokRefreshRepository {

    @PersistenceContext(unitName = "mokPersistenceUnit")
    private EntityManager em;

    public void refresh(AbstractEntity entity) {
        em.refresh(em.merge(entity));
    }
}
