package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.subject.SubjectStudentStatsDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Repository;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.GiteaRepositoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@MethodCallLogged
@TransactionLogged
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
@Retryable(retryFor = {JpaSystemException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
public class RepositoryService {

    private final GiteaRepositoryRepository giteaRepositoryRepository;

    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public List<Repository> getRepositories(UUID subjectId) {
        return giteaRepositoryRepository.findBySubjectId(subjectId);
    }

    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public List<SubjectStudentStatsDTO> getUserAccounts(String subjectName) {
        List<Repository> repos = giteaRepositoryRepository.findBySubject_Name(subjectName);
        List<SubjectStudentStatsDTO> results = new ArrayList<>();

        repos.forEach(repo -> {
            repo.getStudentList().forEach(student -> {
                Account account = student.getAccount();

                results.add(new SubjectStudentStatsDTO(
                        student.getId(),
                        account.getName(),
                        account.getSurname(),
                        repo.getId(),
                        repo.getRepositoryName(),
                        null // TODO: jak będę robił przypadek MPP.23 to tutaj będzie średni wskaźnik plagiatu dla każdego studenta
                ));
            });
        });

        return results;
    }

    public void checkOwnCode(UUID repositoryId, String tag) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}