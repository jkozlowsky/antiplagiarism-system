package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import pl.lodz.p.it.ssbd2026.ssbd01.common.utils.PostCommitTransactionManager;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.GiteaRepoDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.SubjectDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.SubjectAssignment;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Teacher;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RaportLevelDict;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccountNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel.AccessLevelNotAssignedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.raportLevel.RaportLevelDictNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Report;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectAccessDeniedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectAssignmentRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.TeacherRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RaportLevelRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;

import java.util.ArrayList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


@RequiredArgsConstructor
@Service
@MethodCallLogged
@TransactionLogged
@Slf4j
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
@Retryable(retryFor = {JpaSystemException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final VersionHashService versionHashService;
    private final PostCommitTransactionManager postCommitTransactionManager;
    private final GiteaService giteaService;
    private final RestClient giteaClient;
    private final SubjectAssignmentRepository subjectAssignmentRepository;
    private final TeacherRepository teacherRepository;
    private final RaportLevelRepository raportLevelRepository;

    public void syncSubjectWithGitea(UUID subjectId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void getAllSubjects() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    public void getSubjectDetails(UUID subjectId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @MethodCallLogged
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mppTransactionManager")
    public void createSubject(SubjectDTO subjectDto, String creatorLogin) {

        Teacher teacher = teacherRepository.findActiveTeacherByLogin(creatorLogin)
                .orElseThrow(AccessLevelNotAssignedException::new);

        RaportLevelDict defaultRaportLevel = raportLevelRepository.findByName("FULL_VIEW")
                .orElseThrow(RaportLevelDictNotFoundException::new);

        Subject subject = new Subject();
        subject.setName(subjectDto.name());
        subject.setOrganizationName(subjectDto.organizationName());
        subject.setEdition(subjectDto.edition());
        subject.setSubjectDescription(subjectDto.subjectDescription());
        subject.setGiteaURL(subjectDto.giteaURL());
        subject.setArchived(false);

        RulePreset defaultPreset = new RulePreset();
        defaultPreset.setRaportLevel(defaultRaportLevel);
        defaultPreset.setStudentTicketCount(3);
        defaultPreset.setMinimumTokensMatch(10);
        defaultPreset.setEnableNormalization(true);
        defaultPreset.setIsPreset(false);
        defaultPreset.setSubject(subject);
        defaultPreset.setScheduleList(new ArrayList<>());

        subject.setRulePreset(defaultPreset);

        subjectRepository.saveAndFlush(subject);

        SubjectAssignment assignment = new SubjectAssignment();
        assignment.setSubject(subject);
        assignment.setTeacher(teacher);
        assignment.setHasEditPermissions(true);

        subjectAssignmentRepository.saveAndFlush(assignment);
    }

    public void updateSubject(UUID subjectId, Object subjectDto) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @MethodCallLogged
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mppTransactionManager")
    public void deleteSubject(UUID subjectId, String versionHash, String currentUsername, boolean deleteGiteaOrg) {

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(SubjectNotFoundException::new);

        if (!versionHash.equals(versionHashService.hashObjectVersion(subjectId, subject.getVersion()))) {
            throw new ConcurrentUpdateException();
        }

        boolean hasPermission = subject.getSubjectAssignmentList().stream()
                .anyMatch(assignment ->
                        assignment.getTeacher().getAccount().getLogin().equals(currentUsername) &&
                                Boolean.TRUE.equals(assignment.getHasEditPermissions())
                );

        if (!hasPermission) {
            throw new SubjectAccessDeniedException();
        }

        List<String> reportPaths = subject.getReportList().stream()
                .map(Report::getPathToReport)
                .filter(Objects::nonNull)
                .toList();

        if (deleteGiteaOrg) {
            try {
                giteaService.deleteGiteaOrganization(subject.getName());
            } catch (Exception e) {
                log.error("Nie udało się usunąć przedmiotu z serwera Gitea: {}", subject.getName(), e);
                throw new RuntimeException("Błąd podczas usuwania przedmiotu z Gitea", e);
            }
        } else {
            log.info("Pominięto usuwanie organizacji na serwerze Gitea dla przedmiotu: {}", subject.getName());
        }

        subjectRepository.delete(subject);
        subjectRepository.flush();

        postCommitTransactionManager.register(() -> {
            for (String path : reportPaths) {
                try {
                    Files.deleteIfExists(Paths.get(path));
                    log.info("Plik raportu został trwale usunięty z dysku: {}", path);
                } catch (IOException e) {
                    log.error("Nie udało się usunąć fizycznego pliku z dysku po usunięciu przedmiotu. Ścieżka: {}", path, e);
                }
            }
        });
    }
    
    public void calculateAggregatedValues(UUID subjectId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    public void changeSubjectManager(UUID subjectId, UUID newManagerId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}