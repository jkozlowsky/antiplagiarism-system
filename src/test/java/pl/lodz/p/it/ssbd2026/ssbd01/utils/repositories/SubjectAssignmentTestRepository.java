package pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories;

import pl.lodz.p.it.ssbd2026.ssbd01.entities.SubjectAssignment;

import java.util.List;

public interface SubjectAssignmentTestRepository extends BaseTestRepository<SubjectAssignment> {
    List<SubjectAssignment> findByTeacherAccountLogin(String login);
}
