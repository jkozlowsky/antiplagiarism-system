package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.RulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.ScheduleConfigDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.SubjectConfigDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectAccessDeniedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.subject.SubjectArchivedException;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.GiteaRepositoryRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.ReportsRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.ScheduleRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@MethodCallLogged
@TransactionLogged
@Retryable(retryFor = {JpaSystemException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
public class ReportDatabaseService {
    private final SubjectRepository subjectRepository;
    private final GiteaRepositoryRepository giteaRepositoryRepository;
    private final ReportsRepository reportsRepository;
    private final ScheduleRepository scheduleRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
    public SubjectConfigDTO verifyAccessAndGetConfig(UUID subjectId, String login) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow(SubjectNotFoundException::new);
        if (!subjectRepository.isTeacherAssignedToSubject(subjectId, login)) {
            throw new SubjectAccessDeniedException();
        }
        if (subject.getArchived()) {
            throw new SubjectArchivedException();
        }

        RulePreset options = subject.getRulePreset();

        return new SubjectConfigDTO(
                subject.getOrganizationName(),
                options.getMinimumTokensMatch(),
                options.getEnableNormalization()
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")
    public Report saveGeneratedReport(UUID subjectId, String tag, long scannedCount, String reportPath, Double avgSimilarity) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow(SubjectNotFoundException::new);
        List<Repository> repositoryList = giteaRepositoryRepository.getAllBySubjectId(subjectId);

        Report report = new Report();
        report.setSubject(subject);
        report.setTag(tag);
        report.setIsStudentReport(false);
        report.setScannedRepositories(scannedCount);
        report.setPathToReport(reportPath);
        report.setRepositoryList(repositoryList);
        report.setAverageSimilarity(avgSimilarity);

        return reportsRepository.saveAndFlush(report);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")
    public ScheduleConfigDTO fetchNextScheduleToExecute() {
        List<Schedule> schedules = scheduleRepository.findSchedulesToExecute(PageRequest.of(0, 1));
        if (schedules.isEmpty()) return null;

        Schedule schedule = schedules.getFirst();
        RulePreset settings = schedule.getRulePreset();
        Subject subject = settings.getSubject();

        return new ScheduleConfigDTO(
                schedule.getId(),
                subject.getId(),
                subject.getOrganizationName(),
                schedule.getTag(),
                settings.getMinimumTokensMatch(),
                settings.getEnableNormalization()
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")
    public void saveScheduleSuccess(ScheduleConfigDTO config, long scannedCount, String reportPath, Double avgSimilarity) {
        Subject subject = subjectRepository.findById(config.subjectId()).orElseThrow(SubjectNotFoundException::new);
        List<Repository> repositoryList = giteaRepositoryRepository.getAllBySubjectId(config.subjectId());

        Report report = new Report();
        report.setSubject(subject);
        report.setTag(config.giteaTag());
        report.setScannedRepositories(scannedCount);
        report.setIsStudentReport(false);
        report.setPathToReport(reportPath);
        report.setRepositoryList(repositoryList);
        report.setAverageSimilarity(avgSimilarity);

        reportsRepository.saveAndFlush(report);

        Schedule schedule = scheduleRepository.findById(config.scheduleId()).orElseThrow();
        schedule.setExecuted(true);
        scheduleRepository.saveAndFlush(schedule);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")
    public void saveScheduleFailure(UUID scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow();
        schedule.setExecuted(true);
        schedule.setHasFailed(true);
        scheduleRepository.saveAndFlush(schedule);
    }
}