package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import de.jplag.JPlagComparison;
import de.jplag.JPlagResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.FilteredComparisonDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.GiteaRepoDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.StudentReportDetailsDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Report;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Repository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectAccessDeniedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.reportObjects.TopComparison;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.utils.GitService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.GiteaRepositoryRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.ReportsRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.utils.GitService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.utils.JPlagService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.utils.JplagReportParser;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

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

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public Report generateReportOnDemand(UUID subjectId, String tag) {

        Subject subject = subjectRepository.findById(subjectId).orElseThrow(SubjectNotFoundException::new);
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
    public Page<Report> getAllReports(UUID subjectId, Pageable pageable) {
        return reportsRepository.findAllBySubjectId(subjectId, pageable);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT)")
    public List<Report> getStudentOwnReports(String login) {
        return reportsRepository.findVisibleReportsForStudent(login);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT)")
    public StudentReportDetailsDTO getStudentReportDetails(UUID reportId, String login) {
        Report report = reportsRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Raport nie znaleziony")); // ReportNotFoundException

        boolean hasAccess = report.getRepositoryList().stream()
                .flatMap(repo -> repo.getStudentList().stream())
                .anyMatch(student -> student.getAccount().getLogin().equals(login));

        if (!hasAccess) {
            throw new SubjectAccessDeniedException();
        }

        String raportLevel = report.getSubject().getRulePreset().getRaportLevel().getName();

        if ("NOTHING".equals(raportLevel)) {
            throw new SubjectAccessDeniedException();
        }

        List<String> studentRepoNames = report.getRepositoryList().stream()
                .filter(repo -> repo.getStudentList().stream()
                        .anyMatch(s -> s.getAccount().getLogin().equals(login)))
                .map(Repository::getRepositoryName)
                .toList();

        List<TopComparison> allComparisons = JplagReportParser.readJplagArchive(report.getPathToReport());

        List<FilteredComparisonDTO> filteredMatches = allComparisons.stream()
                .filter(c -> studentRepoNames.contains(c.firstSubmission()) || studentRepoNames.contains(c.secondSubmission()))
                .map(c -> {
                    String anonymizedMatchedWith = "Repozytorium Innego Studenta";

                    Double maxSim = null, avgSim = null, longest = null;

                    if ("ONLY_HIGHEST_PERCENT".equals(raportLevel)) {
                        maxSim = c.similarities().max();
                    } else if ("ONLY_PERCENTAGES".equals(raportLevel)) {
                        maxSim = c.similarities().max();
                        avgSim = c.similarities().avg();
                    } else if ("FULL_VIEW".equals(raportLevel)) {
                        maxSim = c.similarities().max();
                        avgSim = c.similarities().avg();
                        longest = c.similarities().longestMatch();
                    }

                    return new FilteredComparisonDTO(anonymizedMatchedWith, maxSim, avgSim, longest);
                })
                .sorted((c1, c2) -> {
                    double sim1 = c1.maxSimilarity() != null ? c1.maxSimilarity() : 0.0;
                    double sim2 = c2.maxSimilarity() != null ? c2.maxSimilarity() : 0.0;
                    return Double.compare(sim2, sim1);
                })
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


    public void getReportDetails(UUID reportId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void deleteReport(UUID reportId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void generateReportBySchedule() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}