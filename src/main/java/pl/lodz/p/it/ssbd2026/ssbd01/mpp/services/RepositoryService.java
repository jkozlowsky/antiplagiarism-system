package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.RepositoryMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.StudentRepositoryDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.projections.RepoStudentProjectionDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.projections.RepositorySimpleProjectionDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.repository.RepositoryWithStudentDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.repository.StudentDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.subject.SubjectStudentStatsDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Report;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Repository;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.GiteaRepositoryRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.ReportsRepository;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@MethodCallLogged
@TransactionLogged
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
@Retryable(retryFor = {JpaSystemException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
public class RepositoryService {

    private final GiteaRepositoryRepository giteaRepositoryRepository;
    private final ReportsRepository reportsRepository;
    private final RepositoryMapper repositoryMapper;

    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
    public List<RepositoryWithStudentDTO> getRepositories(UUID subjectId) {

        List<RepositorySimpleProjectionDTO> repos = giteaRepositoryRepository.findProjectedRepositoriesBySubjectId(subjectId);

        if (repos.isEmpty()) {
            return Collections.emptyList();
        }

        List<RepoStudentProjectionDTO> rawStudents = giteaRepositoryRepository.findStudentsForSubjectRepositories(subjectId);

        Map<UUID, List<StudentDTO>> studentsByRepoId = rawStudents.stream()
                .collect(Collectors.groupingBy(
                        RepoStudentProjectionDTO::repositoryId,
                        Collectors.mapping(
                                proj -> new StudentDTO(proj.name(), proj.surname()),
                                Collectors.toList()
                        )
                ));

        return repos.stream().map(repo -> {
            List<StudentDTO> assignedStudents = studentsByRepoId.getOrDefault(repo.id(), new ArrayList<>());

            return repositoryMapper.projectionToWithStudentDto(repo, assignedStudents);
        }).toList();
    }
//    public List<Repository> getRepositories(UUID subjectId) {
//        return giteaRepositoryRepository.findBySubjectId(subjectId);
//    }

    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public List<SubjectStudentStatsDTO> getUserAccounts(String subjectName) {
        List<Repository> repos = giteaRepositoryRepository.findBySubject_Name(subjectName);
        List<SubjectStudentStatsDTO> results = new ArrayList<>();

        repos.forEach(repo -> {
            repo.getStudentList().forEach(student -> {
                Account account = student.getAccount();

                Double averagePlagiarismRatio = null;

                Optional<Report> latestReport = reportsRepository.findFirstByInitiatingRepositoryIdOrderByCreatedAtDesc(repo.getId());

                if (latestReport.isPresent()) {
                    averagePlagiarismRatio = latestReport.get().getAverageSimilarity();
                }

                results.add(new SubjectStudentStatsDTO(
                        student.getId(),
                        account.getName(),
                        account.getSurname(),
                        repo.getId(),
                        repo.getRepositoryName(),
                        averagePlagiarismRatio
                ));
            });
        });

        return results;
    }

    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT)")
    public List<StudentRepositoryDTO> getStudentRepositories(String login) {
        return giteaRepositoryRepository
                .findByStudentList_Account_Login(login)
                .stream()
                .map(repo -> {
                    int used = (int) reportsRepository.countReportsByInitiatedId(repo.getId());

                    int max = repo.getSubject().getRulePreset() != null
                            ? repo.getSubject().getRulePreset().getStudentTicketCount()
                            : 0;

                    return new StudentRepositoryDTO(
                            repo.getId(),
                            repo.getRepositoryName(),
                            repo.getSubject().getName(),
                            used,
                            max
                    );
                })
                .toList();
    }
}