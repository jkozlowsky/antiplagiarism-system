package pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.projections.ScheduleProjectionListDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Schedule;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@JaversSpringDataAuditable
@Repository
@Transactional(propagation = Propagation.MANDATORY, transactionManager = "mppTransactionManager")
public interface ScheduleRepository extends AbstractRepository<Schedule> {
    interface SubjectNextScan {
        UUID getSubjectId();
        LocalDateTime getNextScanTime();
    }

    @Query(value = "select s from Schedule as s join s.rulePreset rp join rp.subject sub where sub.id = :subjectId")
    List<Schedule> findAllBySubjectId(@Param("subjectId") UUID subjectId);

    @Lock(LockModeType.PESSIMISTIC_WRITE) // Na wypadek wielu instancji aplikacji; przy Pgpool wymaga weryfikacji zachowania
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")) // -2 = skip locked
    @Query(value = "select s from Schedule s where s.scheduleDateTime <= CURRENT_TIMESTAMP and s.isExecuted = false")
    List<Schedule> findSchedulesToExecute(Pageable pageable);


    @Query("SELECT sub.id AS subjectId, MIN(s.scheduleDateTime) AS nextScanTime " +
            "FROM Schedule s JOIN s.rulePreset rp JOIN rp.subject sub " +
            "WHERE sub.id IN :subjectIds AND s.isExecuted = false AND s.scheduleDateTime > CURRENT_TIMESTAMP " +
            "GROUP BY sub.id")
    List<SubjectNextScan> findNextScanTimesForSubjects(@Param("subjectIds") List<UUID> subjectIds);

    @Query("SELECT MIN(s.scheduleDateTime) FROM Schedule s JOIN s.rulePreset rp JOIN rp.subject sub WHERE sub.id = :subjectId AND s.isExecuted = false AND s.scheduleDateTime > CURRENT_TIMESTAMP")
    Optional<LocalDateTime> findNextScanTimeBySubjectId(@Param("subjectId") UUID subjectId);

    @Query("""  
    SELECT new pl.lodz.p.it.ssbd2026.ssbd01.dto.projections.ScheduleProjectionListDTO(s.id, s.scheduleDateTime, s.tag, s.version, s.isExecuted, s.hasFailed) FROM Schedule s JOIN s.rulePreset rp JOIN rp.subject sub WHERE sub.id = :subjectId""")
    List<ScheduleProjectionListDTO> findAllProjectedBySubjectId(@Param("subjectId") UUID subjectId);

}
