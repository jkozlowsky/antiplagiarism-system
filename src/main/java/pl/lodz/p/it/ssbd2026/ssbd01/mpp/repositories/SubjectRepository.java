package pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories;


import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(propagation = Propagation.MANDATORY, transactionManager = "mppTransactionManager")
public interface SubjectRepository extends AbstractRepository<Subject> {

    @Query("SELECT COUNT(s) > 0 FROM Subject s JOIN s.repositoryList r JOIN r.studentList st WHERE s.id = :subjectId AND st.account.login = :studentLogin")
    boolean isStudentAssignedToSubject(
            @Param("subjectId") UUID subjectId,
            @Param("studentLogin") String studentLogin
    );

    @Query("SELECT COUNT(s) > 0 FROM Subject s JOIN s.subjectAssignmentList sa JOIN sa.teacher t WHERE s.id = :subjectId AND t.account.login = :teacherLogin")
    boolean isTeacherAssignedToSubject(
            @Param("subjectId") UUID subjectId,
            @Param("teacherLogin") String teacherLogin
    );

    @Query("SELECT DISTINCT s FROM Subject s JOIN s.repositoryList r JOIN r.studentList st WHERE st.account.login = :login")
    List<Subject> findAllByStudentLogin(@Param("login") String login);

    boolean existsByOrganizationNameIgnoreCase(String organizationName);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("SELECT s FROM Subject s WHERE s.id = :id")
    Optional<Subject> findByIdForceIncrement(@Param("id") UUID id);
}
