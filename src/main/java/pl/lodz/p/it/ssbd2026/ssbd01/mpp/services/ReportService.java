package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import de.jplag.JPlagComparison;
import de.jplag.JPlagResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.*;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.GitCloneException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ReportNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectAccessDeniedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ownReport.NoTicketsLeftException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ownReport.RepositoryNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.subject.SubjectArchivedException;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.GiteaRepositoryRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.ReportsRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.ScheduleRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.utils.GitService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.utils.JPlagService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.utils.JplagReportParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
@MethodCallLogged
@TransactionLogged
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
@Retryable(retryFor = {JpaSystemException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
public class ReportService {

    private final SubjectRepository subjectRepository;
    private final GiteaService giteaService;
    private final JPlagService jPlagService;
    private final GiteaRepositoryRepository giteaRepositoryRepository;
    private final ReportsRepository reportsRepository;
    private final ScheduleRepository scheduleRepository;
    private final SubjectStatisticsService subjectStatisticsService;
    private final ReportDatabaseService reportDatabaseService;

    @Value("${gitea.token}")
    private String giteaToken;

    @Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = false, transactionManager = "mppTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public Report generateReportOnDemand(UUID subjectId, String tag, String login) {
        SubjectConfigDTO config = reportDatabaseService.verifyAccessAndGetConfig(subjectId, login);

        Subject subject = subjectRepository.findById(subjectId).orElseThrow(SubjectNotFoundException::new);
        if (subject.getArchived()) {
            throw new SubjectArchivedException();
        }

        List<GiteaRepoDTO> giteaRepoDTO = giteaService.getRepositoriesFromOrganization(config.organizationName());


        long successfulClonesCount = giteaRepoDTO.stream()
                .filter(repo -> !repo.template())
                .map(repo -> {
                    File targetDir = new File("repos/submissions/" + config.organizationName() + "/" + tag + "/" + repo.name());
                    return GitService.cloneTag(repo.clone_url(), tag, targetDir, giteaToken);
                })
                .filter(success -> success)
                .count();

        if (successfulClonesCount == 0) {
            throw new GitCloneException();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String reportPath = "reports/" + config.organizationName() + "/" + tag + "/" + timestamp + ".jplag";

        JPlagResult result = jPlagService.runPlagiarismScanOnOrganisation(
                config.organizationName(),
                tag,
                config.minimumTokensMatch(),
                config.enableNormalization(),
                reportPath
        );

        List<JPlagComparison> comparisons = result.getAllComparisons();

        Double avg = comparisons.stream()
                .mapToDouble(JPlagComparison::similarity)
                .average()
                .orElse(0.0);

        Report savedReport = reportDatabaseService.saveGeneratedReport(
                subjectId, tag, successfulClonesCount, reportPath, avg
        );
        subjectStatisticsService.recalculateAndUpdateStatistics(subjectId);

        return savedReport;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public Page<Report> getAllReportsForSubject(UUID subjectId, Pageable pageable) {
        return reportsRepository.findAllBySubjectIdAndIsStudentReportFalse(subjectId, pageable);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT)")
    public List<ReportDTO> getStudentOwnReports(String login) {
        List<Report> reports = reportsRepository.findVisibleReportsForStudent(login);

        return reports.stream()
                .map(r -> new ReportDTO(
                        r.getId(),
                        r.getTag(),
                        r.getAverageSimilarity(),
                        r.getCreatedAt(),
                        r.getSubject().getCreatedBy().getLogin(),
                        r.getSubject().getName(),
                        r.getScannedRepositories()
                ))
                .toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT)")
    public StudentReportDetailsDTO getStudentReportDetails(UUID reportId, String login) {
        Report report = reportsRepository.findById(reportId)
                .orElseThrow(ReportNotFoundException::new);

        boolean hasAccess = report.getRepositoryList().stream()
                .flatMap(repo -> repo.getStudentList().stream())
                .anyMatch(student -> student.getAccount().getLogin().equals(login));

        if (!hasAccess) {
            throw new SubjectAccessDeniedException();
        }

        if (report.getSubject() == null || report.getSubject().getRulePreset() == null || report.getSubject().getRulePreset().getRaportLevel() == null) {
            throw new SubjectAccessDeniedException();
        }

        String raportLevel = report.getSubject().getRulePreset().getRaportLevel().getName();

        List<String> studentRepoNames = report.getRepositoryList().stream()
                .filter(repo -> repo.getStudentList().stream()
                        .anyMatch(s -> s.getAccount().getLogin().equals(login)))
                .map(Repository::getRepositoryName)
                .toList();

        List<FilteredComparisonDTO> filteredMatches = new ArrayList<>();

        if (!"NOTHING".equals(raportLevel)) {
            List<TeacherComparisonDTO> allComparisons = JplagReportParser.parseFullTeacherReport(report.getPathToReport());

            filteredMatches = allComparisons.stream()
                    .filter(c -> studentRepoNames.contains(c.firstSubmission()) || studentRepoNames.contains(c.secondSubmission()))
                    .map(c -> {
                        Double studentSimilarity = null;
                        Double otherSimilarity = null;
                        Double avgSim = null;

                        boolean isFirst = studentRepoNames.contains(c.firstSubmission());

                        if ("ONLY_HIGHEST_PERCENT".equals(raportLevel)) {
                            studentSimilarity = c.maxSimilarity();
                        } else if ("ONLY_PERCENTAGES".equals(raportLevel)) {
                            studentSimilarity = c.maxSimilarity();
                            avgSim = c.averageSimilarity();
                        }
                        return new FilteredComparisonDTO("ANONYMIZED", studentSimilarity, otherSimilarity, avgSim);
                    })
                    .sorted((c1, c2) -> Double.compare(
                            c2.getStudentSimilarity() != null ? c2.getStudentSimilarity() : 0.0,
                            c1.getStudentSimilarity() != null ? c1.getStudentSimilarity() : 0.0
                    ))
                    .toList();

            if ("ONLY_HIGHEST_PERCENT".equals(raportLevel) && !filteredMatches.isEmpty()) {
                filteredMatches = List.of(filteredMatches.get(0));
            }
        }
            return new StudentReportDetailsDTO(
                    report.getId(),
                    report.getTag(),
                    report.getSubject().getName(),
                    report.getCreatedAt(),
                    filteredMatches,
                    raportLevel
            );
    }

    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public Page<Report> getAllReportsForTeacher(String login, Pageable pageable) {
        return reportsRepository.findAllReportsForAssignedTeacherByLogin(login, pageable);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public TeacherReportDetailsDTO getTeacherReportDetails(UUID reportId, String login) {

        Report report = reportsRepository.findById(reportId)
                .orElseThrow(ReportNotFoundException::new);

        boolean isCreator = report.getSubject().getCreatedBy().getLogin().equals(login);
        boolean isAssigned = report.getSubject().getSubjectAssignmentList().stream()
                .anyMatch(sa -> sa.getTeacher().getAccount().getLogin().equals(login));

        if (!isCreator && !isAssigned) {
            throw new SubjectAccessDeniedException();
        }

        List<TeacherComparisonDTO> parsedComparisons = JplagReportParser.parseFullTeacherReport(report.getPathToReport());

        List<TeacherComparisonDTO> sortedComparisons = parsedComparisons.stream()
                .sorted((c1, c2) -> Double.compare(
                        c2.maxSimilarity() != null ? c2.maxSimilarity() : 0.0,
                        c1.maxSimilarity() != null ? c1.maxSimilarity() : 0.0
                ))
                .toList();

        return new TeacherReportDetailsDTO(
                report.getId(),
                report.getTag(),
                report.getSubject().getName(),
                report.getCreatedAt(),
                report.getAverageSimilarity(),
                sortedComparisons
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public void deleteReport(UUID reportId) {
        Report report = reportsRepository.findById(reportId).orElseThrow(ReportNotFoundException::new);
        Path filePath = Path.of(report.getPathToReport());

        if (report.getSubject().getArchived()) {
            throw new SubjectArchivedException();
        }

        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (!deleted) {
                log.warn("File was not found on disk, but proceeding to delete DB record: {}", filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not delete the report file physically", e);
        }

        reportsRepository.delete(report);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = false, transactionManager = "mppTransactionManager")
    @Retryable(retryFor = {JpaSystemException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public boolean generateReportBySchedule() {
        ScheduleConfigDTO config = reportDatabaseService.fetchNextScheduleToExecute();
        if (config == null) return false;

//        List<Schedule> schedules = scheduleRepository.findSchedulesToExecute(PageRequest.of(0, 1));
//        if (schedules.isEmpty()) return false;

//        Schedule schedule = schedules.getFirst();
        try {
            Subject subject = subjectRepository.findById(config.subjectId()).orElseThrow(SubjectNotFoundException::new);
            if (subject.getArchived()) {
                throw new SubjectArchivedException();
            }
//            RulePreset settings = schedule.getRulePreset();
//            Subject subject = settings.getSubject();
//            String giteaTag = schedule.getTag();

            List<GiteaRepoDTO> giteaRepoList = giteaService.getRepositoriesFromOrganization(config.organizationName());
//            giteaRepoList.stream().filter(repo -> !repo.template()).forEach(repo -> {
//                File targetDir = new File("repos/submissions/" + subject.getOrganizationName() + "/" + giteaTag + "/" + repo.name());
//                GitService.cloneTag(repo.clone_url(), giteaTag, targetDir, giteaToken);
//            });

            long successfulClonesCount = giteaRepoList.stream()
                    .filter(repo -> !repo.template())
                    .map(repo -> {
                        File targetDir = new File("repos/submissions/" + config.organizationName() + "/" + config.giteaTag() + "/" + repo.name());
                        return GitService.cloneTag(repo.clone_url(), config.giteaTag(), targetDir, giteaToken);
                    })
                    .filter(success -> success)
                    .count();

            if (successfulClonesCount == 0) {
                throw new GitCloneException();
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String reportPath = "reports/" + config.organizationName() + "/" + config.giteaTag() + "/" + timestamp + ".jplag";

//            List<Repository> repositoryList = giteaRepositoryRepository.getAllBySubjectId(subject.getId());

//            Report report = new Report();
//            report.setSubject(subject);
//            report.setTag(giteaTag);
//            report.setScannedRepositories(successfulClonesCount);
//            report.setIsStudentReport(false);
//            report.setPathToReport("reports/" + subject.getOrganizationName() + "/" + giteaTag + "/" + timestamp + ".jplag");
//            report.setRepositoryList(repositoryList);
            JPlagResult result = jPlagService.runPlagiarismScanOnOrganisation(
                    config.organizationName(),
                    config.giteaTag(),
                    config.minimumTokensMatch(),
                    config.enableNormalization(),
                    reportPath
            );

            Double avg = result.getAllComparisons().stream()
                    .mapToDouble(JPlagComparison::similarity)
                    .average()
                    .orElse(0.0);

            reportDatabaseService.saveScheduleSuccess(config, successfulClonesCount, reportPath, avg);

//            report.setAverageSimilarity(avg);

//            reportsRepository.saveAndFlush(report);

//            Double newSubjectAggregate = reportsRepository.calculateAggregatedAverageForSubject(subject.getId());
//            if (subject.getSubjectStatistics() == null){
//                SubjectStatistics subjectStatistics = new SubjectStatistics();
//                subjectStatistics.setAverageSimilarity(newSubjectAggregate);
//                subjectStatistics.setSubject(subject);
//                subject.setSubjectStatistics(subjectStatistics);
//            }else{
//                subject.getSubjectStatistics().setAverageSimilarity(newSubjectAggregate);
//            }
//            subjectRepository.saveAndFlush(subject);
            subjectStatisticsService.recalculateAndUpdateStatistics(config.subjectId());

//            schedule.setExecuted(true);
//            scheduleRepository.saveAndFlush(schedule);
        } catch (Exception e) {
//            schedule.setExecuted(true);
//            schedule.setHasFailed(true);
//            scheduleRepository.saveAndFlush(schedule);
            reportDatabaseService.saveScheduleFailure(config.scheduleId());
        }
        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT)")
    public StudentReportDetailsDTO generateStudentReport(UUID repositoryId, String studentTag, String taskTag, String login) {
        Repository repository = giteaRepositoryRepository.findByIdWithLock(repositoryId)
                .orElseThrow(RepositoryNotFoundException::new);

        boolean isStudentInThisRepository = repository.getStudentList().stream()
                .anyMatch(s -> s.getAccount().getLogin().equals(login));

        if (!isStudentInThisRepository) {
            throw new SubjectAccessDeniedException();
        }

        Subject subject = repository.getSubject();
        if (subject.getArchived()) {
            throw new SubjectArchivedException();
        }
        RulePreset preset = subject.getRulePreset();

        if (preset == null || "NOTHING".equals(preset.getRaportLevel().getName())) {
            throw new SubjectAccessDeniedException();
        }

        int usedTickets = reportsRepository.countReportsByIdAndCreatedByLogin(repositoryId, login);

        if (usedTickets >= preset.getStudentTicketCount()) {
            throw new NoTicketsLeftException();
        }

        List<GiteaRepoDTO> giteaRepoDTO = giteaService.getRepositoriesFromOrganization(subject.getOrganizationName());

        giteaRepoDTO.stream().filter(repo -> !repo.template()).forEach(repo -> {
            File targetDir = new File("repos/submissions/" + subject.getOrganizationName() + "/" + studentTag + "/" + repo.name());
            String tag = repo.name().equals(repository.getRepositoryName()) ? studentTag : taskTag;
            GitService.cloneTag(repo.clone_url(), tag, targetDir, giteaToken);
        });

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        List<Repository> repositoryList = giteaRepositoryRepository.getAllBySubjectId(subject.getId());

        Report report = new Report();
        report.setSubject(subject);
        report.setTag(studentTag);
        report.setPathToReport("reports/" + subject.getOrganizationName() + "/" + studentTag + "/" + timestamp + ".jplag");
        report.setScannedRepositories(0L);
        report.setIsStudentReport(true);
        report.setRepositoryList(repositoryList);

        JPlagResult result = jPlagService.runPlagiarismScanOnOrganisation(
                subject.getOrganizationName(),
                studentTag,
                preset.getMinimumTokensMatch(),
                preset.getEnableNormalization(),
                report.getPathToReport()
        );

        List<JPlagComparison> comparisons = result.getAllComparisons();

        Double avg = comparisons.stream()
                .mapToDouble(JPlagComparison::similarity)
                .average()
                .orElse(0.0);
        report.setAverageSimilarity(avg);

        reportsRepository.saveAndFlush(report);

//        Double aggValue = reportsRepository.calculateAggregatedAverageForSubject(subject.getId());
//        subject.getSubjectStatistics().setAverageSimilarity(aggValue);
        subjectRepository.saveAndFlush(subject);

        return getStudentReportDetails(report.getId(), login);
    }
}