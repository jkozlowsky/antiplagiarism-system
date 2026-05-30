package pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Teacher;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY, transactionManager = "mppTransactionManager")
public interface TeacherRepository extends AbstractRepository<Teacher> {

    @Query("SELECT t FROM Teacher t WHERE t.account.login = :login AND t.active = true")
    Optional<Teacher> findActiveTeacherByLogin(@Param("login") String login);

    List<Teacher> findByAccountLoginContainingIgnoreCaseOrAccountEmailContainingIgnoreCase(String login, String email);

}