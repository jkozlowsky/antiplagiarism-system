package pl.lodz.p.it.ssbd2026.ssbd01.mpp.mpp04;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.CommonFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.Matchers.*;

public class MPP04_ViewSingleReportTest extends BaseTestSetup {

    @Autowired
    private JwtAuthService jwtAuthService;

    private CommonFunctions commonFunctions = new CommonFunctions();

    @Autowired
    private ReportTestRepository reportTestRepository;

    @Autowired
    private SubjectTestRepository subjectTestRepository;

    @Autowired
    @Qualifier("adminTransactionManager")
    private PlatformTransactionManager transactionManager;

    @PersistenceContext
    private EntityManager em;

    private String mainTeacherToken;
    private String unassignedTeacherToken;
    private UUID reportId;
    private Path dummyReportPath;

    @BeforeEach
    public void setup() throws Exception {
        dummyReportPath = Files.createTempFile("dummy_report_", ".jplag");
        createDummyJplagArchive(dummyReportPath.toFile());

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);

        txTemplate.executeWithoutResult(status -> {
            Account mainTeacherAccount = em.createQuery("SELECT a FROM Account a WHERE a.login = 'test_teacher'", Account.class).getSingleResult();
            Account unassignedTeacherAccount = em.createQuery("SELECT a FROM Account a WHERE a.login = 'test_admin'", Account.class).getSingleResult();

            String mainTeacherLogin = mainTeacherAccount.getLogin();
            String unassignedTeacherLogin = unassignedTeacherAccount.getLogin();

            mainTeacherToken = jwtAuthService.generateToken(mainTeacherLogin, List.of(AccountConsts.ROLE_TEACHER), false);
            unassignedTeacherToken = jwtAuthService.generateToken(unassignedTeacherLogin, List.of(AccountConsts.ROLE_TEACHER), false);

            Subject subject = new Subject();
            subject.setName("Programowanie " + commonFunctions.generateRandomStringOfLength(4));
            subject.setOrganizationName("Org_" + commonFunctions.generateRandomStringOfLength(4));
            subject.setEdition("2026");
            subject.setGiteaURL("http://localhost:3000/org");
            subject.setArchived(false);

            ReflectionTestUtils.setField(subject, "createdBy", mainTeacherAccount);
            subjectTestRepository.saveAndFlush(subject);

            Report report = new Report();
            report.setSubject(subject);
            report.setTag("v1.0");
            report.setPathToReport(dummyReportPath.toString());
            report.setScannedRepositories(0L);
            report.setAverageSimilarity(45.5);
            reportTestRepository.saveAndFlush(report);

            reportId = report.getId();
        });
    }

    @AfterEach
    public void cleanup() {
        if (dummyReportPath != null) {
            dummyReportPath.toFile().delete();
        }
    }

    private void createDummyJplagArchive(File zipFile) throws Exception {
        try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipFile))) {
            ZipEntry entry = new ZipEntry("topComparisons.json");
            zout.putNextEntry(entry);
            String jsonContent = "[\n" +
                    "  {\n" +
                    "    \"firstSubmission\": \"kowalski_repo\",\n" +
                    "    \"secondSubmission\": \"nowak_repo\",\n" +
                    "    \"similarities\": { \"AVG\": 50.0, \"MAX\": 85.5, \"MAXIMUM_LENGTH\": 100, \"LONGEST_MATCH\": 200 }\n" +
                    "  }\n" +
                    "]";
            zout.write(jsonContent.getBytes());
            zout.closeEntry();
        }
    }

    @Test
    @DisplayName("POS01: Pobranie szczegółów raportu przez Głównego Prowadzącego - pełne dane bez anonimizacji")
    public void POS01_GetSingleReportDetails_MainTeacher() {
        RestAssured.given()
                .auth().oauth2(mainTeacherToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/reports/" + reportId)
                .then()
                .statusCode(200)
                .body("id", equalTo(reportId.toString()))
                .body("averageSimilarity", equalTo(45.5f))
                .body("comparisons", hasSize(1))
                .body("comparisons[0].firstSubmission", equalTo("kowalski_repo"))
                .body("comparisons[0].maxSimilarity", equalTo(85.5f));
    }

    @Test
    @DisplayName("NEG01: Prowadzący nieprzypisany do przedmiotu otrzymuje 403 Forbidden")
    public void NEG01_AccessDeniedForUnassignedTeacher() {
        RestAssured.given()
                .auth().oauth2(unassignedTeacherToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/reports/" + reportId)
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("NEG02: Próba pobrania nieistniejącego raportu - 404 Not Found")
    public void NEG02_ReportNotFound() {
        RestAssured.given()
                .auth().oauth2(mainTeacherToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/reports/" + UUID.randomUUID())
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Przepływ alternatywny 3a: Plik został usunięty z serwera (500 Internal Server Error)")
    public void NEG03_ReportFileMissingOrCorrupted() {
        dummyReportPath.toFile().delete();

        RestAssured.given()
                .auth().oauth2(mainTeacherToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/reports/" + reportId)
                .then()
                .statusCode(500);
    }
}