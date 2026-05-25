package pl.lodz.p.it.ssbd2026.ssbd01.mpp.mpp9;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

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
    @DisplayName("POS01: Powinien poprawnie usunąć przedmiot i zwrócić HTTP 204")
    public void POS01_deleteSubject_ShouldReturn204() {
        Headers headers = new Headers(
                new Header("Content-Type", "application/json"),
                new Header("If-Match", versionHash)
        );

        String url = "/api/subjects/" + subjectId;

        io.restassured.RestAssured.given()
                .headers(headers)
                .delete(url)
                .then()
                .statusCode(204);

        var txTemplate = new org.springframework.transaction.support.TransactionTemplate(adminTransactionManager);
        txTemplate.executeWithoutResult(status -> {
            assertTrue(subjectTestRepository.findById(subjectId).isEmpty(), "Przedmiot powinien zostać kaskadowo usunięty");
        });
    }

    @Test
    @DisplayName("NEG01: Powinien zwrócić błąd konfliktu HTTP 409 przy podaniu błędnego nagłówka If-Match")
    public void NEG01_deleteSubject_ShouldReturn409_WhenIfMatchIsInvalid() {
        Headers headers = new Headers(
                new Header("Content-Type", "application/json"),
                new Header("If-Match", "niepoprawny-hash-wersji-1234")
        );

        String url = "/api/subjects/" + subjectId;

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