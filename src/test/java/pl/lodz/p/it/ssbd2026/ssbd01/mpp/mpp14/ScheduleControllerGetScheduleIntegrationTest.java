package pl.lodz.p.it.ssbd2026.ssbd01.mpp.mpp14;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.specification.FilterableRequestSpecification;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.schedule.ScheduleDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Schedule;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.SubjectAssignment;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.RulePresetTestRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.ScheduleTestRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.SubjectAssignmentTestRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ScheduleControllerGetScheduleIntegrationTest extends BaseTestSetup {
    @Autowired
    private RequestFunctions requestFunctions;
    @Autowired
    private JwtAuthService jwtAuthService;
    @Autowired
    private ScheduleTestRepository scheduleTestRepository;
    @Autowired
    private RulePresetTestRepository rulePresetTestRepository;
    @Autowired
    @Qualifier("adminTransactionManager")
    private PlatformTransactionManager transactionManager;
    @Autowired
    private SubjectAssignmentTestRepository subjectAssignmentTestRepository;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()); // Bez tego modułu Jackson się wywala, bo nie obsługuje LocalDateTime z "urzędu".
    private Headers teacherHeaders;
    private Headers otherTeacherHeaders;
    private UUID subjectId;
    private UUID createdScheduleId;

    @BeforeAll
    public void setUpScheduleTest() {
        String token = jwtAuthService.generateToken("jkowalski", List.of(AccountConsts.ROLE_TEACHER), false);
        teacherHeaders = new Headers(new Header("Authorization", "Bearer " + token));

        String otherToken = jwtAuthService.generateToken("mkarbowańczyk", List.of(AccountConsts.ROLE_TEACHER), false);
        otherTeacherHeaders = new Headers(new Header("Authorization", "Bearer " + otherToken));

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.execute(_ -> {
            SubjectAssignment assignment = subjectAssignmentTestRepository.findByTeacherAccountLogin("jkowalski").getFirst();
            subjectId = assignment.getSubject().getId();

            RulePreset rulePreset = rulePresetTestRepository.findBySubjectId(subjectId).getFirst();

            Schedule schedule = new Schedule();
            schedule.setScheduleDateTime(LocalDateTime.now().plusDays(1));
            schedule.setTag("v1.0");
            schedule.setRulePreset(rulePreset);

            Schedule savedSchedule = scheduleTestRepository.save(schedule);
            createdScheduleId = savedSchedule.getId();
            return null;
        });
    }

    @AfterAll
    public void tearDownScheduleTest() {
        if (createdScheduleId != null) {
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            txTemplate.execute(_ -> {
                scheduleTestRepository.deleteById(createdScheduleId);
                return null;
            });
        }
    }

    @Test
    public void POS1_TeacherWithRequiredPermissions_ShouldGetSchedule() throws JsonProcessingException {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");
        String response = requestFunctions.sendGetRequest(
                HttpStatus.OK.value(), "/api/schedules/subject/" + subjectId, teacherHeaders);

        List<ScheduleDTO> schedules = Arrays.asList(objectMapper.readValue(response, ScheduleDTO[].class));
        assertAll(
                () -> assertNotNull(schedules),
                () -> assertNotEquals(0, schedules.size()),
                () -> assertFalse(schedules.isEmpty())
        );
    }

    @Test
    public void NEG1_TeacherWithoutPermissions_ShouldReturn403() {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");
        requestFunctions.sendGetRequest(
                HttpStatus.FORBIDDEN.value(), "/api/schedules/subject/" + subjectId, otherTeacherHeaders);
    }

    @Test
    public void NEG2_Unauthenticated_ShouldReturn401() {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");
        requestFunctions.sendGetRequest(
                HttpStatus.UNAUTHORIZED.value(), "/api/schedules/subject/" + subjectId, new Headers());
    }

}
