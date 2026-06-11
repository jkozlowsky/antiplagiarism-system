package pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.projections.ReportListProjectionDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Report;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@JaversSpringDataAuditable
@Repository
@Transactional(propagation = Propagation.MANDATORY, transactionManager = "mppTransactionManager")
public interface ReportsRepository extends AbstractRepository<Report> {
    @Query("SELECT AVG(r.averageSimilarity) FROM Report r WHERE r.subject.id = :subjectId AND r.averageSimilarity IS NOT NULL AND r.isStudentReport = FALSE")
    Double calculateAggregatedAverageForSubject(@Param("subjectId") UUID subjectId);

    @EntityGraph(attributePaths = {"subject", "createdBy"})
    Page<Report> findAllBySubjectIdAndIsStudentReportFalse(UUID subjectId, Pageable pageable);

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
            "WHERE sa.teacher.account.login = :login " +
            "AND r.isStudentReport = false ")
    Page<Report> findAllReportsForAssignedTeacherByLogin(@Param("login") String login, Pageable pageable);

    @Query("""
                SELECT COUNT(r)
                FROM Report r
                WHERE r.initiatingRepository.id = :repositoryId
                  AND r.isStudentReport = true
            """)
    int countReportsByInitiatedId(@Param("repositoryId") UUID repositoryId);

    @Query("""
                SELECT new pl.lodz.p.it.ssbd2026.ssbd01.dto.projections.ReportListProjectionDTO(
                    r.id, r.tag, r.averageSimilarity, r.subject.name, r.createdAt, r.scannedRepositories
                )
                FROM Report r
                JOIN SubjectAssignment sa ON r.subject.id = sa.subject.id
                WHERE sa.teacher.account.login = :login
                AND r.isStudentReport = false
            """)
    Page<ReportListProjectionDTO> findProjectedReportsForAssignedTeacher(@Param("login") String login, Pageable pageable);

    Optional<Report> findFirstByInitiatingRepositoryIdOrderByCreatedAtDesc(UUID repositoryId);
}
