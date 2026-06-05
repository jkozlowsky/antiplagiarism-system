package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import de.jplag.JPlagComparison;
import de.jplag.JPlagResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.*;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ReportFileException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ReportNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectAccessDeniedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ownReport.NoTicketsLeftException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ownReport.RepositoryNotFoundException;
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

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public Report generateReportOnDemand(UUID subjectId, String tag, String login) {

        Subject subject = subjectRepository.findById(subjectId).orElseThrow(SubjectNotFoundException::new);
        if(!subjectRepository.isTeacherAssignedToSubject(subjectId, login)){
            throw new SubjectAccessDeniedException();
        }
        List<GiteaRepoDTO> giteaRepoDTO = giteaService.getRepositoriesFromOrganization(subject.getOrganizationName());

        giteaRepoDTO.stream().filter(repo -> !repo.template()).forEach(repo -> {
            File targetDir = new File("repos/submissions/" + subject.getOrganizationName() + "/" + tag + "/" + repo.name());
            GitService.cloneTag(repo.clone_url(), tag, targetDir);
        });

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        List<Repository> repositoryList = giteaRepositoryRepository.getAllBySubjectId(subjectId);
        Report report = new Report();
        report.setSubject(subject);
        report.setTag(tag);
        report.setPathToReport("reports/" + subject.getOrganizationName() + "/" + tag + "/" + timestamp + ".jplag");
        report.setRepositoryList(repositoryList);

        RulePreset options = subject.getRulePreset();

        JPlagResult result = jPlagService.runPlagiarismScanOnOrganisation(
                subject.getOrganizationName(),
                tag,
                options.getMinimumTokensMatch(),
                options.getEnableNormalization(),
                report.getPathToReport()
        );

        List<JPlagComparison> comparisons = result.getAllComparisons();

        Double avg = comparisons.stream()
                .mapToDouble(JPlagComparison::similarity)
                .average()
                .orElse(0.0);
        report.setAverageSimilarity(avg);

        reportsRepository.saveAndFlush(report);

        Double newSubjectAggregate = reportsRepository.calculateAggregatedAverageForSubject(subjectId);
        subject.setAggregatedAverageSimilarity(newSubjectAggregate);
        subjectRepository.saveAndFlush(subject);

        return report;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public Page<Report> getAllReportsForSubject(UUID subjectId, Pageable pageable) {
        return reportsRepository.findAllBySubjectId(subjectId, pageable);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT)")
    public List<ReportDTO> getStudentOwnReports(String login) {
        List<Report> reports = reportsRepository.findVisibleReportsForStudent(login);

        return reports.stream()
                .filter(report -> {
                    if (report.getSubject() == null || report.getSubject().getRulePreset() == null || report.getSubject().getRulePreset().getRaportLevel() == null) {
                        return false;
                    }
                    String level = report.getSubject().getRulePreset().getRaportLevel().getName();
                    return !level.equals("HIDDEN");
                })
                .map(r -> new ReportDTO(
                        r.getId(),
                        r.getTag(),
                        r.getAverageSimilarity(),
                        r.getCreatedAt(),
                        r.getSubject().getCreatedBy().getLogin(),
                        r.getSubject().getName()
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

        if (raportLevel.equals("HIDDEN")) {
            throw new SubjectAccessDeniedException();
        }

        List<String> studentRepoNames = report.getRepositoryList().stream()
                .filter(repo -> repo.getStudentList().stream()
                        .anyMatch(s -> s.getAccount().getLogin().equals(login)))
                .map(Repository::getRepositoryName)
                .toList();

        List<TeacherComparisonDTO> allComparisons = JplagReportParser.parseFullTeacherReport(report.getPathToReport());

        List<FilteredComparisonDTO> filteredMatches = allComparisons.stream()
                .filter(c -> studentRepoNames.contains(c.firstSubmission()) || studentRepoNames.contains(c.secondSubmission()))
                .map(c -> {
                    String anonymizedMatchedWith = "Repozytorium Innego Studenta";
                    Double maxSim = null, avgSim = null, longest = null;
                    String studentFile = null, studentCode = null;
                    List<Integer> matchedLines = null;

                    boolean isFirst = studentRepoNames.contains(c.firstSubmission());

                    if ("ONLY_HIGHEST_PERCENT".equals(raportLevel)) {
                        maxSim = c.maxSimilarity();
                    } else if ("ONLY_PERCENTAGES".equals(raportLevel)) {
                        maxSim = c.maxSimilarity();
                        avgSim = c.averageSimilarity();
                    } else if ("FULL_VIEW".equals(raportLevel)) {
                        maxSim = c.maxSimilarity();
                        avgSim = c.averageSimilarity();
                        longest = c.longestMatch();
                        studentFile = isFirst ? c.fileA() : c.fileB();
                        studentCode = isFirst ? c.codeA() : c.codeB();
                        matchedLines = isFirst ? c.matchedLinesA() : c.matchedLinesB();
                    }

                    return new FilteredComparisonDTO(anonymizedMatchedWith, maxSim, avgSim, longest, studentFile, studentCode, matchedLines);
                })
                .sorted((c1, c2) -> Double.compare(
                        c2.maxSimilarity() != null ? c2.maxSimilarity() : 0.0,
                        c1.maxSimilarity() != null ? c1.maxSimilarity() : 0.0
                ))
                .toList();

        if ("ONLY_HIGHEST_PERCENT".equals(raportLevel) && !filteredMatches.isEmpty()) {
            filteredMatches = List.of(filteredMatches.get(0));
        }

        return new StudentReportDetailsDTO(
                report.getId(),
                report.getTag(),
                report.getSubject().getName(),
                report.getCreatedAt(),
                filteredMatches
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

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")
    @Retryable(retryFor = {JpaSystemException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    public boolean generateReportBySchedule() {
        List<Schedule> schedules = scheduleRepository.findSchedulesToExecute(PageRequest.of(0, 1));
        if (schedules.isEmpty()) return false;

        Schedule schedule = schedules.getFirst();
        try {
            RulePreset settings = schedule.getRulePreset();
            Subject subject = settings.getSubject();
            String giteaTag = schedule.getTag();

            List<GiteaRepoDTO> giteaRepoList = giteaService.getRepositoriesFromOrganization(subject.getOrganizationName());
            giteaRepoList.stream().filter(repo -> !repo.template()).forEach(repo -> {
                File targetDir = new File("repos/submissions/" + subject.getOrganizationName() + "/" + giteaTag + "/" + repo.name());
                GitService.cloneTag(repo.clone_url(), giteaTag, targetDir);
            });

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            List<Repository> repositoryList = giteaRepositoryRepository.getAllBySubjectId(subject.getId());

            Report report = new Report();
            report.setSubject(subject);
            report.setTag(giteaTag);
            report.setPathToReport("reports/" + subject.getOrganizationName() + "/" + giteaTag + "/" + timestamp + ".jplag");
            report.setRepositoryList(repositoryList);

            JPlagResult result = jPlagService.runPlagiarismScanOnOrganisation(
                    subject.getOrganizationName(),
                    giteaTag,
                    settings.getMinimumTokensMatch(),
                    settings.getEnableNormalization(),
                    report.getPathToReport()
            );

            Double avg = result.getAllComparisons().stream()
                    .mapToDouble(JPlagComparison::similarity)
                    .average()
                    .orElse(0.0);
            report.setAverageSimilarity(avg);

            reportsRepository.saveAndFlush(report);

            Double newSubjectAggregate = reportsRepository.calculateAggregatedAverageForSubject(subject.getId());
            subject.setAggregatedAverageSimilarity(newSubjectAggregate);
            subjectRepository.saveAndFlush(subject);

            schedule.setExecuted(true);
            scheduleRepository.saveAndFlush(schedule);
        } catch (Exception e) {
            schedule.setExecuted(true);
            schedule.setHasFailed(true);
            scheduleRepository.saveAndFlush(schedule);
        }
        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT)")
    public StudentReportDetailsDTO generateStudentReport(UUID repositoryId, String studentTag, String taskTag, String login) {
        Repository repository = giteaRepositoryRepository.findById(repositoryId)
                .orElseThrow(RepositoryNotFoundException::new);

        boolean isStudentInThisRepository = repository.getStudentList().stream()
                .anyMatch(s -> s.getAccount().getLogin().equals(login));
        if (!isStudentInThisRepository) {
            throw new SubjectAccessDeniedException();
        }

        Subject subject = repository.getSubject();
        RulePreset preset = subject.getRulePreset();

        if (preset == null || "HIDDEN".equals(preset.getRaportLevel().getName())) {
            throw new SubjectAccessDeniedException();
        }

        int usedTickets = repository.getTicketCount();

        if (usedTickets >= preset.getStudentTicketCount()) {
            throw new NoTicketsLeftException();
        }

        repository.setTicketCount(usedTickets + 1);
        giteaRepositoryRepository.saveAndFlush(repository);

        List<GiteaRepoDTO> giteaRepoDTO = giteaService.getRepositoriesFromOrganization(subject.getOrganizationName());

        giteaRepoDTO.stream().filter(repo -> !repo.template()).forEach(repo -> {
            File targetDir = new File("repos/submissions/" + subject.getOrganizationName() + "/" + studentTag + "/" + repo.name());
            String tag = repo.name().equals(repository.getRepositoryName()) ? studentTag : taskTag;
            GitService.cloneTag(repo.clone_url(), tag, targetDir);
        });

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        List<Repository> repositoryList = giteaRepositoryRepository.getAllBySubjectId(subject.getId());

        Report report = new Report();
        report.setSubject(subject);
        report.setTag(studentTag);
        report.setPathToReport("reports/" + subject.getOrganizationName() + "/" + studentTag + "/" + timestamp + ".jplag");
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

        Double aggValue = reportsRepository.calculateAggregatedAverageForSubject(subject.getId());
        subject.setAggregatedAverageSimilarity(aggValue);
        subjectRepository.saveAndFlush(subject);

        return getStudentReportDetails(report.getId(), login);
    }
}