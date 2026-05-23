package pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.SubjectAssignment;

@Repository
@Transactional(propagation = Propagation.MANDATORY, transactionManager = "mppTransactionManager")
public interface SubjectAssignmentRepository extends AbstractRepository<SubjectAssignment> {
    boolean existsBySubjectAndTeacherAccountLoginAndHasEditPermissionsTrue(Subject subject, String login);
}
