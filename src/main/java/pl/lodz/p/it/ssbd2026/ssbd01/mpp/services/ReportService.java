package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import de.jplag.JPlag;
import de.jplag.JPlagResult;
import de.jplag.exceptions.ExitException;
import de.jplag.highlightextraction.FrequencyAnalysisOptions;
import de.jplag.java.JavaLanguage;
import de.jplag.options.JPlagOptions;
import de.jplag.reporting.reportobject.ReportObjectFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.GiteaRepoDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.TransactionLogged;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@MethodCallLogged
@TransactionLogged
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
@Retryable(retryFor = {JpaSystemException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
public class ReportService  {

    private final RestClient giteaClient;

    public void generateReportOnDemand(UUID subjectId, String tag) {

        List<GiteaRepoDTO> giteaRepoDTO = giteaClient
                .get()
                .uri("/orgs/PAS/repos")
                .retrieve()
                .body(new ParameterizedTypeReference<List<GiteaRepoDTO>>(){});

        //using jgit get a version from tag
        giteaRepoDTO.stream().filter(repo -> !repo.template()).forEach(repo -> {
            File targetDir = new File("repos/submissions/" + repo.full_name());
            GitService.cloneTag(repo.clone_url(), tag, targetDir);
        });

        JavaLanguage language = new JavaLanguage();
        language.getOptions();
//        File baseCode = new File("repos/base_template/");
        Set<File> submissionDirectories = Set.of(new File("repos/submissions/PAS"));
        JPlagOptions options = new JPlagOptions(language, submissionDirectories, Set.of())
                .withMinimumTokenMatch(20)
                .withNormalize(true); //.withBaseCodeSubmissionDirectory(baseCode);

        try {
            JPlagResult result = JPlag.run(options);
            ReportObjectFactory reportObjectFactory = new ReportObjectFactory(new File("reports/report_A.jplag"));
            reportObjectFactory.createAndSaveReport(result);
            System.out.println(result.toString());
        } catch (ExitException | FileNotFoundException e) {
            System.out.println("error " + e);
            // error handling here
        }

//        throw new UnsupportedOperationException("Not implemented yet");
    }

    
    public void getAllReports(UUID subjectId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    
    public void getReportDetails(UUID reportId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    
    public void deleteReport(UUID reportId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    
    public void getStudentOwnReports(UUID studentId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    
    public void generateReportBySchedule() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}