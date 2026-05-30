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
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(propagation = Propagation.MANDATORY, readOnly = true, transactionManager = "mppTransactionManager")
public interface SubjectAssignmentRepository extends AbstractRepository<SubjectAssignment> {

    //nowy zamiennik na poprzednie existsBySubjectAndTeacherAccountLoginAndHasEditPermissionsTrue
    @Query("SELECT COUNT(sa) > 0 FROM SubjectAssignment sa WHERE sa.subject = :subject AND sa.teacher.account.login = :login AND sa.subjectRole IN ('OWNER', 'EDITOR')")
    boolean hasEditPermissions(@Param("subject") Subject subject, @Param("login") String login);

    @Query(value = "select sa from SubjectAssignment sa where sa.teacher.account.login = :teacherAccountLogin and sa.subject.id = :subjectId")
    Optional<SubjectAssignment> findByTeacherLoginAndSubjectId(
            @Param("teacherAccountLogin") String teacherAccountLogin,
            @Param("subjectId") UUID subjectId
    );

    @Query("SELECT sa FROM SubjectAssignment sa WHERE sa.teacher.account.login = :login")
    List<SubjectAssignment> findAllByTeacherLogin(@Param("login") String login);

}
