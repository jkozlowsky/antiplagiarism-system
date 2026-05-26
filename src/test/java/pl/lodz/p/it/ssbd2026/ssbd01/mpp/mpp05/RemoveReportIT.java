package pl.lodz.p.it.ssbd2026.ssbd01.mpp.mpp05;

import io.restassured.http.Headers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Report;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.RestSetupInit;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.ReportTestRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.SubjectTestRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RemoveReportIT extends BaseTestSetup {

    @Autowired
    private RequestFunctions requestFunctions;

    @Autowired
    private ReportTestRepository reportTestRepository;

    @Autowired
    private SubjectTestRepository subjectTestRepository;

    @Autowired
    private RestSetupInit restSetup;

    private Path testFilePath;

    @BeforeEach
    public void setupTest() throws IOException {

        Path testDirectory = Path.of("reports/test_integration/");
        Files.createDirectories(testDirectory);
        testFilePath = testDirectory.resolve("dummy_report.jplag");

        if (!Files.exists(testFilePath)) {
            Files.createFile(testFilePath);
        }
    }

    @AfterEach
    public void cleanupFiles() throws IOException {
        Files.deleteIfExists(testFilePath);
    }

    @Test
    void shouldSuccessfullyDeleteReportViaHttp() {
        Subject subject = new Subject();
        subject.setOrganizationName("IntegrationTestOrg");
        subject.setArchived(false);
        subject.setName("test");
        subject.setEdition("26");
        subject.setGiteaURL("gitea.url");
        subjectTestRepository.saveAndFlush(subject);

        Report report = new Report();
        report.setSubject(subject);
        report.setTag("tag");
        report.setAverageSimilarity(25D);
        report.setPathToReport(testFilePath.toString());
        reportTestRepository.saveAndFlush(report);

        assertTrue(Files.exists(testFilePath), "File should exist before deletion");
        assertTrue(reportTestRepository.findById(report.getId()).isPresent(), "DB record should exist before deletion");

        String url = "/api/reports/" + report.getId();

        requestFunctions.sendDeleteRequest(200, url, new Headers());

        boolean dbRecordExists = reportTestRepository.findById(report.getId()).isPresent();
        assertFalse(dbRecordExists, "Report should have been removed from the database");

        boolean fileExists = Files.exists(testFilePath);
        assertFalse(fileExists, "Physical file should have been deleted from the disk");
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentReport() {
        String url = "/api/reports/" + java.util.UUID.randomUUID();

        requestFunctions.sendDeleteRequest(404, url, new Headers());
    }
}