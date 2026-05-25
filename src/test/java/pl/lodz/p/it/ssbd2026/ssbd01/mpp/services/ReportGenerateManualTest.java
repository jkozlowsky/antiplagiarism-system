package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.http.Headers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.lodz.p.it.ssbd2026.ssbd01.config.gitea.GiteaClientConfig;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.ReportDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Report;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RaportLevelRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReportGenerateManualTest extends BaseTestSetup {
    @Autowired
    ReportService reportService;

    @Autowired
    SubjectRepository subjectRepository;

    @Autowired
    RequestFunctions requestFunctions;

    @Autowired
    GiteaClientConfig config;

    @Test
    @Disabled
    void testGenerateReport() throws IOException, InterruptedException {
        Subject subject = subjectRepository.findAll().get(0);

        String result = requestFunctions.sendPostRequest(
                "",
                200,
                "api/reports/demand/" + subject.getId().toString() + "?tag=v1",
                new Headers()
                );
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
        ReportDTO report = objectMapper.readValue(result, ReportDTO.class);

//        Report report = reportService.generateReportOnDemand(subject.getId(), "v1");
        File reportDir = new File(report.path_to_report());

        String reportPath = reportDir.getAbsolutePath();
        String jplagJarPath = "tools/jplag-6.3.0-jar-with-dependencies.jar";

        if (System.getenv("CI") == null) {
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", jplagJarPath, "--mode=VIEW", reportPath);
            pb.inheritIO().start().waitFor();
        }
//        File reportDir = new File("reports/report_" + testSubject);
//        assertTrue(reportDir.exists(), "JPlag report directory should have been created");
//
//         Check if index.html exists inside the report
//        File indexFile = new File(reportDir, "index.html");
//        assertTrue(indexFile.exists(), "JPlag report should contain an index.html");
    }
}
