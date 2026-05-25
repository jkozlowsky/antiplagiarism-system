package pl.lodz.p.it.ssbd2026.ssbd01.mpp.mpp13;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.*;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MPP13_DeleteRulePreset extends BaseTestSetup {

    @Autowired
    private AccountTestRepository accountTestRepository;

    @Autowired
    private SubjectTestRepository subjectTestRepository;

    @Autowired
    private TeacherTestRepository teacherTestRepository;

    @Autowired
    private SubjectAssignmentTestRepository subjectAssignmentTestRepository;

    @Autowired
    private RuleTestRepository ruleTestRepository;

    @Autowired
    private ScheduleTestRepository scheduleTestRepository;

    @Autowired
    private AccessLevelDictTestRepository accessLevelDictTestRepository;

    @Autowired
    private VersionHashService versionHashService;

    @Autowired
    private RaportLevelTestRepository raportLevelTestRepository;

    @Autowired
    @Qualifier("mppTransactionManager")
    private PlatformTransactionManager transactionManager;

    private UUID rulePresetId;
    private String validEtag;
    private UUID scheduleId;

    @BeforeEach
    public void setupData() {
        var txTemplate = new TransactionTemplate(transactionManager);
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
            subject.setName("Przedmiot do testów reguł");
            subject.setOrganizationName("Org Testowa");
            subject.setEdition("2026L");
            subject.setGiteaURL("http://gitea.local/org/repo");
            subject.setArchived(false);
            subjectTestRepository.save(subject);

            SubjectAssignment assignment = new SubjectAssignment();
            assignment.setTeacher(teacher);
            assignment.setSubject(subject);
            assignment.setHasEditPermissions(true);
            subjectAssignmentTestRepository.save(assignment);

            RulePreset preset = new RulePreset();
            preset.setSubject(subject);
            preset.setIsPreset(false);
            preset.setStudentTicketCount(10);
            preset.setMinimumTokensMatch(20);
            preset.setEnableNormalization(true);

            var iterator = raportLevelTestRepository.findAll().iterator();
            if (!iterator.hasNext()) {
                throw new IllegalStateException("Brak słowników poziomu raportowania (RaportLevelDict) w bazie!");
            }
            preset.setRaportLevel(iterator.next());

            ruleTestRepository.save(preset);
            rulePresetId = preset.getId();

            Schedule schedule = new Schedule();
            schedule.setRulePreset(preset);
            schedule.setScheduleDateTime(LocalDateTime.now().plusDays(1));
            schedule.setTag("v1.0.0");
            scheduleTestRepository.save(schedule);
            scheduleId = schedule.getId();

            validEtag = versionHashService.hashObjectVersion(rulePresetId, preset.getVersion());
        });
    }

    @Test
    @DisplayName("POS01: Powinien poprawnie usunąć zestaw reguł oraz kaskadowo powiązane harmonogramy (kod 204)")
    public void POS01_DeleteRulePreset_SuccessAndCascadeDelete() {
        Headers requestHeaders = new Headers(
                new Header("Content-Type", "application/json"),
                new Header("If-Match", validEtag)
        );

        io.restassured.RestAssured.given()
                .headers(requestHeaders)
                .delete("/api/rules/" + rulePresetId)
                .then()
                .statusCode(204);

        var txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.executeWithoutResult(status -> {
            assertTrue(ruleTestRepository.findById(rulePresetId).isEmpty(),
                    "Zestaw reguł powinien zostać trwale usunięty z bazy.");
            assertTrue(scheduleTestRepository.findById(scheduleId).isEmpty(),
                    "Harmonogram powiązany z zestawem reguł powinien zostać kaskadowo usunięty.");
        });
    }

    @Test
    @DisplayName("NEG01: Powinien odrzucić próbę usunięcia ze względu na błędny ETag (Conflict 409)")
    public void NEG01_DeleteRulePreset_ConcurrentUpdateException() {
        Headers requestHeaders = new Headers(
                new Header("Content-Type", "application/json"),
                new Header("If-Match", "niepoprawny-hash-wersji-12345")
        );

        io.restassured.RestAssured.given()
                .headers(requestHeaders)
                .delete("/api/rules/" + rulePresetId)
                .then()
                .statusCode(409);

        var txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.executeWithoutResult(status -> {
            assertTrue(ruleTestRepository.findById(rulePresetId).isPresent(),
                    "Zestaw reguł nie powinien zostać usunięty z powodu błędu blokady optymistycznej.");
            assertTrue(scheduleTestRepository.findById(scheduleId).isPresent(),
                    "Harmonogram powinien pozostać nienaruszony.");
        });
    }
}