package pl.lodz.p.it.ssbd2026.ssbd01.mpp.mpp9;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.GitService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MPP09_DeleteSubject extends BaseTestSetup {

    @Autowired
    private AccountTestRepository accountTestRepository;

    @Autowired
    private SubjectTestRepository subjectTestRepository;

    @Autowired
    private TeacherTestRepository teacherTestRepository;

    @Autowired
    private SubjectAssignmentTestRepository subjectAssignmentTestRepository;

    @Autowired
    private VersionHashService versionHashService;

    @Autowired
    private AccessLevelDictTestRepository accessLevelDictTestRepository;

    @MockitoBean
    private GitService gitService;

    @Autowired
    @Qualifier("adminTransactionManager")
    private org.springframework.transaction.PlatformTransactionManager adminTransactionManager;

    private UUID subjectId;
    private String versionHash;

    @BeforeEach
    public void setupData() {
        var txTemplate = new org.springframework.transaction.support.TransactionTemplate(adminTransactionManager);
        txTemplate.executeWithoutResult(status -> {

            Account adminAccount = accountTestRepository.findByLogin("admin").orElseThrow();

            Teacher teacher = new Teacher();
            teacher.setAccount(adminAccount);
            teacher.setActive(true);

            AccessLevelDict teacherDict = accessLevelDictTestRepository.findByAccessLevelName("TEACHER")
                    .orElseGet(() -> accessLevelDictTestRepository.findByAccessLevelName("ROLE_TEACHER")
                            .orElseThrow(() -> new IllegalStateException("Brak słownika poziomu dostępu dla nauczyciela w bazie!")));

            teacher.setAccessLevelDict(teacherDict);

            teacherTestRepository.save(teacher);

            Subject subject = new Subject();
            subject.setName("Programowanie Aplikacji Sieciowych");
            subject.setEdition("2026L");
            subject.setGiteaURL("http://gitea.example.com/PAS");
            subject.setArchived(false);
            subjectTestRepository.save(subject);

            SubjectAssignment assignment = new SubjectAssignment();
            assignment.setSubject(subject);
            assignment.setTeacher(teacher);
            assignment.setHasEditPermissions(true);
            subjectAssignmentTestRepository.save(assignment);

            subjectId = subject.getId();
            versionHash = versionHashService.hashObjectVersion(subjectId, subject.getVersion());
        });
    }

    @Test
    @DisplayName("POS01: Powinien poprawnie usunąć przedmiot i usunąć organizację z Gitea (flaga true)")
    public void POS01_deleteSubject_ShouldReturn204_AndCallGitea() {
        Headers headers = new Headers(
                new Header("Content-Type", "application/json"),
                new Header("If-Match", versionHash)
        );

        String url = "/api/subjects/" + subjectId + "?deleteGiteaOrg=true";

        io.restassured.RestAssured.given()
                .headers(headers)
                .delete(url)
                .then()
                .statusCode(204);

        var txTemplate = new org.springframework.transaction.support.TransactionTemplate(adminTransactionManager);
        txTemplate.executeWithoutResult(status -> {
            assertTrue(subjectTestRepository.findById(subjectId).isEmpty(), "Przedmiot powinien zostać kaskadowo usunięty");
        });

        verify(gitService, times(1)).deleteGiteaOrganization("Programowanie Aplikacji Sieciowych");
    }

    @Test
    @DisplayName("POS02: Powinien usunąć przedmiot, ale POMINĄĆ usuwanie z Gitea (flaga false)")
    public void POS02_deleteSubject_ShouldReturn204_AndSkipGitea() {
        Headers headers = new Headers(
                new Header("Content-Type", "application/json"),
                new Header("If-Match", versionHash)
        );

        String url = "/api/subjects/" + subjectId + "?deleteGiteaOrg=false";

        io.restassured.RestAssured.given()
                .headers(headers)
                .delete(url)
                .then()
                .statusCode(204);

        var txTemplate = new org.springframework.transaction.support.TransactionTemplate(adminTransactionManager);
        txTemplate.executeWithoutResult(status -> {
            assertTrue(subjectTestRepository.findById(subjectId).isEmpty(), "Przedmiot powinien zostać kaskadowo usunięty");
        });

        verify(gitService, never()).deleteGiteaOrganization(anyString());
    }

    @Test
    @DisplayName("NEG01: Powinien zwrócić błąd konfliktu HTTP 409 przy podaniu błędnego nagłówka If-Match")
    public void NEG01_deleteSubject_ShouldReturn409_WhenIfMatchIsInvalid() {
        Headers headers = new Headers(
                new Header("Content-Type", "application/json"),
                new Header("If-Match", "niepoprawny-hash-wersji-1234")
        );

        String url = "/api/subjects/" + subjectId + "?deleteGiteaOrg=true";

        io.restassured.RestAssured.given()
                .headers(headers)
                .delete(url)
                .then()
                .statusCode(409);

        var txTemplate = new org.springframework.transaction.support.TransactionTemplate(adminTransactionManager);
        txTemplate.executeWithoutResult(status -> {
            assertTrue(subjectTestRepository.findById(subjectId).isPresent(), "Przedmiot wciąż powinien znajdować się w bazie danych");
        });
    }
}