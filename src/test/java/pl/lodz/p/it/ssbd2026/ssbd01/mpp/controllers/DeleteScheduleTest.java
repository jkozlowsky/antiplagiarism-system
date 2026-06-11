package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.specification.FilterableRequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RaportLevelRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RuleRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.ScheduleRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.CommonFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class DeleteScheduleTest extends BaseTestSetup {

    @Autowired
    private JwtAuthService jwtAuthService;

    @Autowired
    private RequestFunctions requestFunctions;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private RaportLevelRepository raportLevelRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private VersionHashService versionHashService;

    @Autowired
    private PlatformTransactionManager mppTransactionManager;

    private String teacherPermToken;
    private String teacherNoPermToken;
    private String studentToken;

    private UUID authorizedScheduleId;

    private final CommonFunctions commonFunctions = new CommonFunctions();

    @BeforeEach
    public void setupDataAndAuth() {
        teacherPermToken = jwtAuthService.generateToken("teacher_perm", List.of(AccountConsts.ROLE_TEACHER), false);
        teacherNoPermToken = jwtAuthService.generateToken("teacher_noperm", List.of(AccountConsts.ROLE_TEACHER), false);
        studentToken = jwtAuthService.generateToken("student", List.of(AccountConsts.ROLE_STUDENT), false);

        TransactionTemplate transactionTemplate = new TransactionTemplate(mppTransactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        transactionTemplate.execute(status -> {
            ReportLevelDict existingLevel = raportLevelRepository.findByName("ONLY_PERCENTAGES")
                    .orElseThrow(() -> new IllegalStateException("Brak poziomu FULL_VIEW w bazie testowej"));

            Subject subjectWithPerm = new Subject();
            String uniqueSuffix = commonFunctions.generateRandomStringOfLength(8);

            subjectWithPerm.setName("Przedmiot " + uniqueSuffix);
            subjectWithPerm.setOrganizationName("Org " + uniqueSuffix);
            subjectWithPerm.setEdition("26/27_" + commonFunctions.generateRandomStringOfLength(3));
            subjectWithPerm.setGiteaURL("http://localhost:3000/repo_" + uniqueSuffix);
            subjectWithPerm.setArchived(false);

            subjectWithPerm = subjectRepository.save(subjectWithPerm);

            RulePreset rulePreset1 = new RulePreset();
            rulePreset1.setRaportLevel(existingLevel);
            rulePreset1.setSubject(subjectWithPerm);
            rulePreset1.setStudentTicketCount(5);
            rulePreset1.setMinimumTokensMatch(10);
            rulePreset1.setEnableNormalization(false);
            rulePreset1.setIsPreset(false);
            rulePreset1 = ruleRepository.saveAndFlush(rulePreset1);

            Schedule schedule1 = new Schedule();
            schedule1.setScheduleDateTime(LocalDateTime.now().plusDays(2));
            schedule1.setTag("LAB_" + commonFunctions.generateRandomStringOfLength(4));
            schedule1.setRulePreset(rulePreset1);
            schedule1.setModifiableUntil(LocalDateTime.now().plusDays(2));

            authorizedScheduleId = scheduleRepository.save(schedule1).getId();

            return null;
        });
    }

    private String getValidVersionHash(UUID scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalStateException("Nie znaleziono harmonogramu do wygenerowania hasha"));
        return versionHashService.hashObjectVersion(scheduleId, schedule.getVersion());
    }

    @Test
    @DisplayName("Powinno zwrócić błąd, gdy nauczyciel nie posiada przypisanych praw edycji")
    public void shouldReturn403WhenTeacherHasNoEditPermissions() {
        Headers headers = new Headers(
                new Header("Authorization", "Bearer " + teacherNoPermToken),
                new Header("If-Match", getValidVersionHash(authorizedScheduleId))
        );

        requestFunctions.sendDeleteRequest(
                403,
                "/api/schedules/" + authorizedScheduleId,
                headers
        );
    }

    @Test
    @DisplayName("Powinno zwrócić błąd, gdy próbę podejmuje student")
    public void shouldReturn403WhenUserIsStudent() {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");

        Headers headers = new Headers(
                new Header("Authorization", "Bearer " + studentToken),
                new Header("If-Match", "dummy-hash")
        );

        requestFunctions.sendDeleteRequest(
                403,
                "/api/schedules/" + authorizedScheduleId,
                headers
        );
    }

    @Test
    @DisplayName("Powinno zwrócić błąd, gdy zapytanie jest nieautoryzowane")
    public void shouldReturn401WhenNoTokenProvided() {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");

        Headers headers = new Headers(
                new Header("If-Match", "dummy-hash")
        );

        requestFunctions.sendDeleteRequest(
                401,
                "/api/schedules/" + authorizedScheduleId,
                headers
        );
    }

    @Test
    @DisplayName("Powinno zwrócić błąd, gdy harmonogram o podanym ID nie istnieje")
    public void shouldReturn404WhenScheduleDoesNotExist() {
        UUID randomId = UUID.randomUUID();

        Headers headers = new Headers(
                new Header("Authorization", "Bearer " + teacherPermToken),
                new Header("If-Match", "dummy-hash")
        );

        requestFunctions.sendDeleteRequest(
                404,
                "/api/schedules/" + randomId,
                headers
        );
    }

    @Test
    @DisplayName("Powinno zwrócić błąd 409, gdy nagłówek wersji nie zgadza się ze stanem w bazie")
    public void shouldReturn409WhenVersionMismatch() {
        Headers headers = new Headers(
                new Header("Authorization", "Bearer " + teacherPermToken),
                new Header("If-Match", "invalid-version-hash")
        );

        requestFunctions.sendDeleteRequest(
                409,
                "/api/schedules/" + authorizedScheduleId,
                headers
        );
    }

    @Test
    @DisplayName("Powinno zwrócić błąd 400, gdy zapytanie nie zawiera nagłówka If-Match")
    public void shouldReturn400WhenIfMatchHeaderIsMissing() {
        Headers headers = new Headers(
                new Header("Authorization", "Bearer " + teacherPermToken)
        );

        requestFunctions.sendDeleteRequest(
                400,
                "/api/schedules/" + authorizedScheduleId,
                headers
        );
    }
}