package pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories;

import pl.lodz.p.it.ssbd2026.ssbd01.entities.Teacher;

import java.util.Optional;

public interface TeacherTestRepository extends BaseTestRepository<Teacher> {
    Optional<Teacher> findByAccount_Login(String login);
}
