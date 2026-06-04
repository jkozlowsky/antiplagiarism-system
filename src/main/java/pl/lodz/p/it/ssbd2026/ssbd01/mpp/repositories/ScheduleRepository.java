package pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
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

    //todo zweryfikować czy to ma sens i działa przy >1 instancjach, bo przy Pgpool możliwe, że będzie problem
    @Lock(LockModeType.PESSIMISTIC_WRITE) //To na wypadek, gdy będziemy mieli więcej niż jedną instancję aplikacji
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")) // -2 = skip locked
    @Query(value = "select s from Schedule s where s.scheduleDateTime <= CURRENT_TIMESTAMP and not exists (select r from Report r where r.subject = s.rulePreset.subject and r.tag = s.tag)")
    List<Schedule> findSchedulesToExecute();
}
