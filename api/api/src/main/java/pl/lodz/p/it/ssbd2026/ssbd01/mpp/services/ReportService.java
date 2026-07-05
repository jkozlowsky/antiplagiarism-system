package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import de.jplag.JPlagComparison;
import de.jplag.JPlagResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.*;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.projections.ReportListProjectionDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Report;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Repository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.*;
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
import java.util.Comparator;
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

    private static final String SUBMISSIONS_DIR = "repos/submissions/";
    private static final String REPORTS_DIR = "reports/";
    private static final String JPLAG_EXT = ".jplag";
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd_HH-mm-ss";

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

        List<GiteaRepoDTO> giteaRepoDTO = giteaService.getRepositoriesFromOrganization(config.organizationName());

        long successfulClonesCount = giteaRepoDTO.stream()
                .filter(repo -> !repo.template())
                .map(repo -> {
                    File targetDir = new File(SUBMISSIONS_DIR + config.organizationName() + "/" + tag + "/" + repo.name());
                    return GitService.cloneTag(repo.clone_url(), tag, targetDir, giteaToken);
                })
                .filter(success -> success)
                .count();

        if (successfulClonesCount == 0) {
            throw new GitCloneException();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT));
        String reportPath = REPORTS_DIR + config.organizationName() + "/" + tag + "/" + timestamp + JPLAG_EXT;

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

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true, transactionManager = "mppTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT)")
    public StudentReportDetailsDTO getStudentReportDetails(UUID reportId, String login) {
        Report report = reportsRepository.findById(reportId)
                .orElseThrow(ReportNotFoundException::new);

        return buildReportDetailsDTO(report, login);
    }

    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public Page<ReportListProjectionDTO> getAllReportsForTeacher(String login, Pageable pageable) {
        return reportsRepository.findProjectedReportsForAssignedTeacher(login, pageable);
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
            throw new DeleteReportException(e);
        }

        Subject subject = subjectRepository.findByReportId(reportId).orElseThrow(SubjectNotFoundException::new);

        reportsRepository.delete(report);

        subjectStatisticsService.recalculateAndUpdateStatistics(subject.getId());
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = false, transactionManager = "mppTransactionManager")
    @Retryable(retryFor = {JpaSystemException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public boolean generateReportBySchedule() {
        ScheduleConfigDTO config = reportDatabaseService.fetchNextScheduleToExecute();
        if (config == null) return false;
        try {
            Subject subject = subjectRepository.findById(config.subjectId()).orElseThrow(SubjectNotFoundException::new);
            if (subject.getArchived()) {
                throw new SubjectArchivedException();
            }

            List<GiteaRepoDTO> giteaRepoList = giteaService.getRepositoriesFromOrganization(config.organizationName());

            long successfulClonesCount = giteaRepoList.stream()
                    .filter(repo -> !repo.template())
                    .map(repo -> {
                        File targetDir = new File(SUBMISSIONS_DIR + config.organizationName() + "/" + config.giteaTag() + "/" + repo.name());
                        return GitService.cloneTag(repo.clone_url(), config.giteaTag(), targetDir, giteaToken);
                    })
                    .filter(success -> success)
                    .count();

            if (successfulClonesCount == 0) {
                throw new GitCloneException();
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT));
            String reportPath = REPORTS_DIR + config.organizationName() + "/" + config.giteaTag() + "/" + timestamp + JPLAG_EXT;

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

            subjectStatisticsService.recalculateAndUpdateStatistics(config.subjectId());

        } catch (Exception e) {
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

        int usedTickets = reportsRepository.countReportsByInitiatedId(repositoryId);

        if (usedTickets >= preset.getStudentTicketCount()) {
            throw new NoTicketsLeftException();
        }

        List<GiteaRepoDTO> giteaRepoDTO = giteaService.getRepositoriesFromOrganization(subject.getOrganizationName());

        giteaRepoDTO.stream().filter(repo -> !repo.template()).forEach(repo -> {
            File targetDir = new File(SUBMISSIONS_DIR + subject.getOrganizationName() + "/" + studentTag + "/" + repo.name());
            String tag = repo.name().equals(repository.getRepositoryName()) ? studentTag : taskTag;
            GitService.cloneTag(repo.clone_url(), tag, targetDir, giteaToken);
        });

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT));

        List<Repository> repositoryList = giteaRepositoryRepository.getAllBySubjectId(subject.getId());

        Report report = new Report();
        report.setSubject(subject);
        report.setTag(studentTag);
        report.setPathToReport(REPORTS_DIR + subject.getOrganizationName() + "/" + studentTag + "/" + timestamp + JPLAG_EXT);
        report.setScannedRepositories(0L);
        report.setIsStudentReport(true);
        report.setInitiatingRepository(repository);
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

        subjectRepository.saveAndFlush(subject);

        return buildReportDetailsDTO(report, login);
    }

    private StudentReportDetailsDTO buildReportDetailsDTO(Report report, String login) {
        validateStudentAccess(report, login);

        String raportLevel = report.getSubject().getRulePreset().getRaportLevel().getName();
        List<String> studentRepoNames = extractStudentRepositoryNames(report, login);

        List<FilteredComparisonDTO> filteredMatches = getFilteredMatches(report, studentRepoNames, raportLevel);

        return new StudentReportDetailsDTO(
                report.getId(),
                report.getTag(),
                report.getSubject().getName(),
                report.getCreatedAt(),
                filteredMatches,
                raportLevel
        );
    }

    private void validateStudentAccess(Report report, String login) {
        boolean hasAccess = report.getRepositoryList().stream()
                .flatMap(repo -> repo.getStudentList().stream())
                .anyMatch(student -> student.getAccount().getLogin().equals(login));

        if (!hasAccess) {
            throw new SubjectAccessDeniedException();
        }

        if (report.getSubject() == null
                || report.getSubject().getRulePreset() == null
                || report.getSubject().getRulePreset().getRaportLevel() == null) {
            throw new SubjectAccessDeniedException();
        }
    }


    private List<String> extractStudentRepositoryNames(Report report, String login) {
        return report.getRepositoryList().stream()
                .filter(repo -> repo.getStudentList().stream()
                        .anyMatch(s -> s.getAccount().getLogin().equals(login)))
                .map(Repository::getRepositoryName)
                .toList();
    }


    private List<FilteredComparisonDTO> getFilteredMatches(Report report, List<String> studentRepoNames, String raportLevel) {
        if ("NOTHING".equals(raportLevel)) {
            return new ArrayList<>();
        }

        List<TeacherComparisonDTO> allComparisons = JplagReportParser.parseFullTeacherReport(report.getPathToReport());

        List<FilteredComparisonDTO> matches = allComparisons.stream()
                .filter(c -> studentRepoNames.contains(c.firstSubmission()) || studentRepoNames.contains(c.secondSubmission()))
                .map(c -> mapToFilteredComparisonDTO(c, raportLevel))
                .sorted(Comparator.comparing(
                        (FilteredComparisonDTO c) -> c.getStudentSimilarity() != null ? c.getStudentSimilarity() : 0.0
                ).reversed())
                .toList();

        if ("ONLY_HIGHEST_PERCENT".equals(raportLevel) && !matches.isEmpty()) {
            return List.of(matches.get(0));
        }

        return matches;
    }

    private FilteredComparisonDTO mapToFilteredComparisonDTO(TeacherComparisonDTO c, String raportLevel) {
        Double studentSimilarity = null;
        Double avgSim = null;

        if ("ONLY_HIGHEST_PERCENT".equals(raportLevel)) {
            studentSimilarity = c.maxSimilarity();
        } else if ("ONLY_PERCENTAGES".equals(raportLevel)) {
            studentSimilarity = c.maxSimilarity();
            avgSim = c.averageSimilarity();
        }

        return new FilteredComparisonDTO("ANONYMIZED", studentSimilarity, null, avgSim);
    }
}