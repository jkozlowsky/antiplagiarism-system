package pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Report;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(propagation = Propagation.MANDATORY, transactionManager = "mppTransactionManager")
public interface ReportsRepository extends AbstractRepository<Report> {
    @Query("SELECT AVG(r.averageSimilarity) FROM Report r WHERE r.subject.id = :subjectId AND r.averageSimilarity IS NOT NULL")
    Double calculateAggregatedAverageForSubject(@Param("subjectId") UUID subjectId);

    @EntityGraph(attributePaths = {"subject"})
    Page<Report> findAllBySubjectId(UUID subjectId, Pageable pageable);

    @Query("SELECT DISTINCT r FROM Report r " +
            "JOIN FETCH r.subject " +
            "JOIN r.repositoryList repo " +
            "JOIN repo.studentList s " +
            "WHERE s.account.login = :login " +
            "AND r.subject.rulePreset.raportLevel.name != 'NOTHING'")
    List<Report> findVisibleReportsForStudent(@Param("login") String login);

    @EntityGraph(attributePaths = {"subject", "createdBy"})
    @Query("SELECT r FROM Report r " +
            "JOIN SubjectAssignment sa ON r.subject.id = sa.subject.id " +
            "WHERE sa.teacher.account.login = :login")
    Page<Report> findAllReportsForAssignedTeacherByLogin(@Param("login") String login, Pageable pageable);
}
