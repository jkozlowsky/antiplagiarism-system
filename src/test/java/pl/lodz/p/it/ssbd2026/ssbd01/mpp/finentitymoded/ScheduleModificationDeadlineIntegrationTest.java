package pl.lodz.p.it.ssbd2026.ssbd01.mpp.finentitymoded;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.specification.FilterableRequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.schedule.UpdateScheduleDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Schedule;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.SubjectAssignment;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.RulePresetTestRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.ScheduleTestRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.SubjectAssignmentTestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScheduleModificationDeadlineIntegrationTest extends BaseTestSetup {
    @Autowired
    private RequestFunctions requestFunctions;
    @Autowired
    private JwtAuthService jwtAuthService;
    @Autowired
    private VersionHashService versionHashService;
    @Autowired
    private ScheduleTestRepository scheduleTestRepository;
    @Autowired
    private RulePresetTestRepository rulePresetTestRepository;
    @Autowired
    private SubjectAssignmentTestRepository subjectAssignmentTestRepository;
    @Autowired
    @Qualifier("mppTransactionManager")
    private PlatformTransactionManager mppTransactionManager;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String TEST_TEACHER_LOGIN = "test_teacher";

    private Headers teacherHeaders;
    private Headers otherTeacherHeaders;
    private Headers unauthHeaders;

    private UUID lockedScheduleId;
    private UUID openScheduleId;

    @BeforeEach
    void setUpScheduleDeadlineTest() {
        String token = jwtAuthService.generateToken(TEST_TEACHER_LOGIN, List.of(AccountConsts.ROLE_TEACHER), false);
        teacherHeaders = new Headers(new Header("Authorization", "Bearer " + token));

        String otherToken = jwtAuthService.generateToken("mkarbowańczyk", List.of(AccountConsts.ROLE_TEACHER), false);
        otherTeacherHeaders = new Headers(new Header("Authorization", "Bearer " + otherToken));

        unauthHeaders = new Headers();

        TransactionTemplate tx = new TransactionTemplate(mppTransactionManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        tx.execute(_ -> {
            SubjectAssignment assignment = subjectAssignmentTestRepository
                    .findByTeacherAccountLogin(TEST_TEACHER_LOGIN).getFirst();
            Subject subject = assignment.getSubject();
            RulePreset rulePreset = rulePresetTestRepository.findBySubjectId(subject.getId()).getFirst();

            Schedule locked = new Schedule();
            locked.setScheduleDateTime(LocalDateTime.now().plusDays(3));
            locked.setTag("locked-tag");
            locked.setRulePreset(rulePreset);
            locked.setModifiableUntil(LocalDateTime.now().minusMinutes(1));
            lockedScheduleId = scheduleTestRepository.saveAndFlush(locked).getId();

            Schedule open = new Schedule();
            open.setScheduleDateTime(LocalDateTime.now().plusDays(5));
            open.setTag("open-tag");
            open.setRulePreset(rulePreset);
            open.setModifiableUntil(LocalDateTime.now().plusDays(5));
            openScheduleId = scheduleTestRepository.saveAndFlush(open).getId();

            return null;
        });
    }

    private String versionHash(UUID scheduleId) {
        Schedule s = scheduleTestRepository.findById(scheduleId).orElseThrow();
        return versionHashService.hashObjectVersion(scheduleId, s.getVersion());
    }

    private Headers withIfMatch(Headers base, String hash) {
        List<Header> headerList = new ArrayList<>(base.asList());
        headerList.add(new Header("If-Match", hash));
        return new Headers(headerList);
    }

    @Test
    void POS01_updateSchedule_whenModifiableUntilInFuture_shouldReturn204() throws Exception {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");

        UpdateScheduleDTO dto = new UpdateScheduleDTO();
        dto.setScheduleDateTime(LocalDateTime.now().plusDays(7));
        dto.setTag("new-open-tag");
        dto.setVersionHash(versionHash(openScheduleId));

        requestFunctions.sendPutRequest(
                objectMapper.writeValueAsString(dto),
                HttpStatus.NO_CONTENT.value(),
                "/api/schedules/" + openScheduleId,
                teacherHeaders
        );
    }

    @Test
    void NEG01_updateSchedule_whenModifiableUntilInPast_shouldReturn409() throws Exception {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");

        UpdateScheduleDTO dto = new UpdateScheduleDTO();
        dto.setScheduleDateTime(LocalDateTime.now().plusDays(7));
        dto.setTag("should-fail");
        dto.setVersionHash(versionHash(lockedScheduleId));

        requestFunctions.sendPutRequest(
                objectMapper.writeValueAsString(dto),
                HttpStatus.CONFLICT.value(),
                "/api/schedules/" + lockedScheduleId,
                teacherHeaders
        );
    }

    @Test
    void NEG02_updateSchedule_whenNoPermission_shouldReturn403() throws Exception {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");

        UpdateScheduleDTO dto = new UpdateScheduleDTO();
        dto.setScheduleDateTime(LocalDateTime.now().plusDays(7));
        dto.setTag("noperm-tag");
        dto.setVersionHash(versionHash(openScheduleId));

        requestFunctions.sendPutRequest(
                objectMapper.writeValueAsString(dto),
                HttpStatus.FORBIDDEN.value(),
                "/api/schedules/" + openScheduleId,
                otherTeacherHeaders
        );
    }

    @Test
    void NEG03_updateSchedule_whenUnauthenticated_shouldReturn401() throws Exception {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");

        UpdateScheduleDTO dto = new UpdateScheduleDTO();
        dto.setScheduleDateTime(LocalDateTime.now().plusDays(7));
        dto.setTag("unauth-tag");
        dto.setVersionHash("any");

        requestFunctions.sendPutRequest(
                objectMapper.writeValueAsString(dto),
                HttpStatus.UNAUTHORIZED.value(),
                "/api/schedules/" + openScheduleId,
                unauthHeaders
        );
    }

    @Test
    void NEG04_updateSchedule_whenScheduleNotFound_shouldReturn404() throws Exception {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");

        UpdateScheduleDTO dto = new UpdateScheduleDTO();
        dto.setScheduleDateTime(LocalDateTime.now().plusDays(7));
        dto.setTag("notfound-tag");
        dto.setVersionHash("any");

        requestFunctions.sendPutRequest(
                objectMapper.writeValueAsString(dto),
                HttpStatus.NOT_FOUND.value(),
                "/api/schedules/" + UUID.randomUUID(),
                teacherHeaders
        );
    }

    @Test
    void POS02_deleteSchedule_whenModifiableUntilInFuture_shouldReturn204() {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");

        requestFunctions.sendDeleteRequest(
                HttpStatus.NO_CONTENT.value(),
                "/api/schedules/" + openScheduleId,
                withIfMatch(teacherHeaders, versionHash(openScheduleId))
        );
    }

    @Test
    void NEG05_deleteSchedule_whenModifiableUntilInPast_shouldReturn409() {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");

        requestFunctions.sendDeleteRequest(
                HttpStatus.CONFLICT.value(),
                "/api/schedules/" + lockedScheduleId,
                withIfMatch(teacherHeaders, versionHash(lockedScheduleId))
        );
    }

}