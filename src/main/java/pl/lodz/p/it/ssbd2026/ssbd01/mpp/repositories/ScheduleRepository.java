package pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Schedule;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(propagation = Propagation.MANDATORY, transactionManager = "mppTransactionManager")
public interface ScheduleRepository extends AbstractRepository<Schedule> {
    @Query(value = "select s from Schedule as s join s.rulePreset rp join rp.subject sub where sub.id = :subjectId")
    List<Schedule> findAllBySubjectId(@Param("subjectId") UUID subjectId);
}
