package pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.SubjectAssignment;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(propagation = Propagation.MANDATORY, readOnly = true, transactionManager = "mppTransactionManager")
public interface SubjectAssignmentRepository extends AbstractRepository<SubjectAssignment> {
    boolean existsBySubjectAndTeacherAccountLoginAndHasEditPermissionsTrue(Subject subject, String login);
    @Query(value = "select sa from SubjectAssignment sa where sa.teacher.account.login = :teacherAccountLogin and sa.subject.id = :subjectId")
    List<SubjectAssignment> findByTeacherLoginAndSubjectId(
            @Param("teacherAccountLogin") String teacherAccountLogin,
            @Param("subjectId") UUID subjectId
    );
}
