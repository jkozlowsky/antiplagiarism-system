package pl.lodz.p.it.ssbd2026.ssbd01.mpp.mpp21;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.CommonFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.*;

import java.io.File;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;

public class MPP21_ViewOwnReportsTest extends BaseTestSetup {

    @Autowired
    private ReportTestRepository reportTestRepository;
    @Autowired
    private RaportLevelTestRepository raportLevelTestRepository;
    @Autowired
    private SubjectTestRepository subjectTestRepository;
    @Autowired
    private RulePresetTestRepository rulePresetTestRepository;
    @Autowired
    private AccountTestRepository accountTestRepository;
    @Autowired
    private AccessLevelTestRepository accessLevelTestRepository;
    @Autowired
    private RepositoryTestRepository repositoryTestRepository;
    @Autowired
    private LanguageDictTestRepository languageDictTestRepository;
    @Autowired
    private AccessLevelDictTestRepository accessLevelDictTestRepository;
    @Autowired
    private JwtAuthService jwtAuthService;

    private CommonFunctions commonFunctions = new CommonFunctions();

    @Autowired
    @Qualifier("adminTransactionManager")
    private PlatformTransactionManager transactionManager;

    private String studentLogin;
    private String studentToken;
    private UUID reportId;
    private File dummyReportFile;

    public static File createDummyJplagReport(String studentRepo, String otherRepo) throws Exception {
        File tempFile = File.createTempFile("dummy_report", ".zip");
        tempFile.deleteOnExit();

        String dummyJson = "[\n" +
                "  {\n" +
                "    \"firstSubmission\": \"" + studentRepo + "\",\n" +
                "    \"secondSubmission\": \"" + otherRepo + "\",\n" +
                "    \"similarities\": {\n" +
                "      \"MAX\": 85.5,\n" +
                "      \"AVG\": 40.2,\n" +
                "      \"LONGEST_MATCH\": 120.0\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"firstSubmission\": \"ThirdRepo\",\n" +
                "    \"secondSubmission\": \"FourthRepo\",\n" +
                "    \"similarities\": {\n" +
                "      \"MAX\": 90.0,\n" +
                "      \"AVG\": 50.0,\n" +
                "      \"LONGEST_MATCH\": 200.0\n" +
                "    }\n" +
                "  }\n" +
                "]";

        try (java.util.zip.ZipOutputStream out = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(tempFile))) {
            java.util.zip.ZipEntry e = new java.util.zip.ZipEntry("topComparisons.json");
            out.putNextEntry(e);
            byte[] data = dummyJson.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            out.write(data, 0, data.length);
            out.closeEntry();
        }
        return tempFile;
    }

    @BeforeEach
    public void setupData() throws Exception {
        ((io.restassured.specification.FilterableRequestSpecification) RestAssured.requestSpecification)
                .removeHeader("Authorization");

        studentLogin = "student_" + commonFunctions.generateRandomStringOfLength(5);
        studentToken = jwtAuthService.generateToken(studentLogin, List.of(AccountConsts.ROLE_STUDENT), false);

        dummyReportFile = createDummyJplagReport("Repo_" + studentLogin, "Repo_Innego_Studenta");

        var txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.executeWithoutResult(status -> {

            LanguageDict languageDict = languageDictTestRepository.findAll().iterator().hasNext()
                    ? languageDictTestRepository.findAll().iterator().next()
                    : null;
            if (languageDict == null) {
                languageDict = new LanguageDict();
                ReflectionTestUtils.setField(languageDict, "name", "PL");
                languageDict = languageDictTestRepository.saveAndFlush(languageDict);
            }

            Account account = new Account();
            account.setLogin(studentLogin);
            account.setPasswordHash("ZaawansowaneHaslo123!");
            account.setEmail(studentLogin + "@example.com");
            account.setName("Jan");
            account.setSurname("Kowalski");
            account.setLanguageVersion(languageDict);
            ReflectionTestUtils.setField(account, "isBlocked", false);
            ReflectionTestUtils.setField(account, "isVerified", true);
            accountTestRepository.saveAndFlush(account);

            AccessLevelDict studentLevelDict = accessLevelDictTestRepository.findAll().iterator().hasNext()
                    ? accessLevelDictTestRepository.findAll().iterator().next()
                    : null;
            if (studentLevelDict == null) {
                studentLevelDict = new AccessLevelDict();
                ReflectionTestUtils.setField(studentLevelDict, "name", AccountConsts.ROLE_STUDENT);
                studentLevelDict = accessLevelDictTestRepository.saveAndFlush(studentLevelDict);
            }

            Student student = new Student();
            student.setAccount(account);
            ReflectionTestUtils.setField(student, "accessLevelDict", studentLevelDict);
            student.setActive(true);
            accessLevelTestRepository.saveAndFlush(student);

            RaportLevelDict raportLevel = raportLevelTestRepository.findByName("FULL_VIEW")
                    .orElseGet(() -> {
                        RaportLevelDict dict = new RaportLevelDict();
                        ReflectionTestUtils.setField(dict, "name", "FULL_VIEW");
                        return raportLevelTestRepository.saveAndFlush(dict);
                    });

            Subject subject = new Subject();
            subject.setName("Przedmiot_" + commonFunctions.generateRandomStringOfLength(5));
            subject.setOrganizationName("Testowa Organizacja");
            subject.setEdition("2026/2027");
            subject.setGiteaURL("http://localhost:3000/test/repo");
            subject.setArchived(false);
            ReflectionTestUtils.setField(subject, "createdBy", account);
            subject = subjectTestRepository.saveAndFlush(subject);

            RulePreset rulePreset = new RulePreset();
            rulePreset.setRaportLevel(raportLevel);
            rulePreset.setStudentTicketCount(5);
            rulePreset.setMinimumTokensMatch(10);
            rulePreset.setEnableNormalization(true);
            rulePreset.setIsPreset(true);
            ReflectionTestUtils.setField(rulePreset, "subject", subject);
            rulePreset = rulePresetTestRepository.saveAndFlush(rulePreset);

            subject.setRulePreset(rulePreset);
            subjectTestRepository.saveAndFlush(subject);

            Repository repo = new Repository();
            repo.setRepositoryName("Repo_" + studentLogin);
            repo.setTicketCount(5);
            repo.setStudentList(List.of(student));
            repo.setSubject(subject);
            ReflectionTestUtils.setField(repo, "createdBy", account);
            repositoryTestRepository.saveAndFlush(repo);

            Report report = new Report();
            report.setSubject(subject);
            report.setRepositoryList(List.of(repo));
            report.setPathToReport(dummyReportFile.getAbsolutePath());
            report.setTag("v1");
            report.setAverageSimilarity(45.5);
            ReflectionTestUtils.setField(report, "createdBy", account);
            reportTestRepository.saveAndFlush(report);

            reportId = report.getId();
        });
    }

    @Test
    @DisplayName("Główny przypływ: Pobranie listy własnych raportów (200 OK)")
    public void POS01_GetOwnReportsList() {
        RestAssured.given()
                .header("Authorization", "Bearer " + studentToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/reports/my-reports")
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(1)))
                .body("[0].id", equalTo(reportId.toString()));
    }

    @Test
    @DisplayName("Główny przypływ: Pobranie szczegółów raportu (200 OK) z przefiltrowanymi i zanonimizowanymi danymi")
    public void POS02_GetOwnReportDetails() {
        RestAssured.given()
                .header("Authorization", "Bearer " + studentToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/reports/my-reports/" + reportId)
                .then()
                .statusCode(200)
                .body("matches", hasSize(1))
                .body("matches[0].matchedWith", equalTo("Repozytorium Innego Studenta"))
                .body("matches[0].maxSimilarity", equalTo(85.5f));
    }

    @Test
    @DisplayName("Przepływ alternatywny: Brak dostępu (403) przy próbie pobrania cudzego raportu")
    public void NEG01_AccessDeniedForOtherStudentsReport() {
        String otherStudentToken = jwtAuthService
                .generateToken("inny_student_" + commonFunctions.generateRandomStringOfLength(4),
                        List.of(AccountConsts.ROLE_STUDENT), false);

        RestAssured.given()
                .header("Authorization", "Bearer " + otherStudentToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/reports/my-reports/" + reportId)
                .then()
                .statusCode(403);
    }
}