package pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.projections.TeacherSearchProjectionDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Teacher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@JaversSpringDataAuditable
@Repository
@Transactional(propagation = Propagation.MANDATORY, transactionManager = "mppTransactionManager")
public interface TeacherRepository extends AbstractRepository<Teacher> {

    @Query("SELECT t FROM Teacher t WHERE t.account.login = :login AND t.active = true")
    Optional<Teacher> findActiveTeacherByLogin(@Param("login") String login);

    @Query("select t from Teacher t join SubjectAssignment sa on sa.teacher = t where sa.subject.id = :subjectId and sa.subjectRole = 'OWNER'")
    Optional<Teacher> findSubjectOwnerBySubjectId(@Param("subjectId") UUID subjectId);

    List<Teacher> findByAccountLoginContainingIgnoreCaseOrAccountEmailContainingIgnoreCase(String login, String email);

    @Query("SELECT t FROM Teacher t WHERE t.active = true AND (LOWER(t.account.login) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(t.account.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Teacher> findActiveTeachersByQuery(@Param("query") String query);

    @Query("""  
            SELECT new pl.lodz.p.it.ssbd2026.ssbd01.dto.projections.TeacherSearchProjectionDTO(t.id, t.account.login, t.account.email, t.account.name, t.account.surname) FROM Teacher t WHERE t.active = true AND (LOWER(t.account.login) LIKE LOWER(CONCAT('%', :query, '%'))  OR LOWER(t.account.email) LIKE LOWER(CONCAT('%', :query, '%')))""")
    List<TeacherSearchProjectionDTO> findProjectedActiveTeachersByQuery(@Param("query") String query);
}