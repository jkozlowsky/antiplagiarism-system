package pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories;

import jakarta.persistence.LockModeType;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.projections.RepoStudentProjectionDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.projections.RepositorySimpleProjectionDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@JaversSpringDataAuditable
@Repository
@Transactional(propagation = Propagation.MANDATORY, transactionManager = "mppTransactionManager")
public interface GiteaRepositoryRepository extends AbstractRepository<pl.lodz.p.it.ssbd2026.ssbd01.entities.Repository> {
    List<pl.lodz.p.it.ssbd2026.ssbd01.entities.Repository> getAllBySubjectId(UUID subjectId);

    @EntityGraph(attributePaths = {"studentList", "studentList.account"})
    List<pl.lodz.p.it.ssbd2026.ssbd01.entities.Repository> findBySubjectId(UUID subjectId);

    List<pl.lodz.p.it.ssbd2026.ssbd01.entities.Repository> findBySubject_Name(String subjectName);

    List<pl.lodz.p.it.ssbd2026.ssbd01.entities.Repository> findByStudentList_Account_Login(String login);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Repository r WHERE r.id = :id")
    Optional<pl.lodz.p.it.ssbd2026.ssbd01.entities.Repository> findByIdWithLock(@Param("id") UUID id);

    @Query("""  
            SELECT new pl.lodz.p.it.ssbd2026.ssbd01.dto.projections.RepositorySimpleProjectionDTO(r.id, r.repositoryName, COUNT(rep)) FROM Repository r LEFT JOIN Report rep ON rep.initiatingRepository = r AND rep.isStudentReport = true WHERE r.subject.id = :subjectId GROUP BY r.id, r.repositoryName""")
    List<RepositorySimpleProjectionDTO> findProjectedRepositoriesBySubjectId(@Param("subjectId") UUID subjectId);

    @Query("""
SELECT new pl.lodz.p.it.ssbd2026.ssbd01.dto.projections.RepoStudentProjectionDTO(r.id, s.id, s.account.login, s.account.name, s.account.surname) FROM Repository r JOIN r.studentList s WHERE r.subject.id = :subjectId""")
    List<RepoStudentProjectionDTO> findStudentsForSubjectRepositories(@Param("subjectId") UUID subjectId);
}
