package pl.lodz.p.it.ssbd2026.ssbd01.mpp.mpp15;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.specification.FilterableRequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.schedule.ScheduleDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.SubjectAssignment;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ScheduleControllerCreateScheduleIntegrationTest extends BaseTestSetup {
    @Autowired
    private ScheduleTestRepository scheduleTestRepository;
    @Autowired
    private SubjectTestRepository subjectTestRepository;
    @Autowired
    private SubjectAssignmentTestRepository subjectAssignmentTestRepository;
    @Autowired
    private RequestFunctions requestFunctions;
    @Autowired
    private JwtAuthService jwtAuthService;
    @Autowired
    private AccountTestRepository accountTestRepository;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private Headers teacherWithEditHeaders;
    private Headers teacherWithoutEditHeaders;
    private UUID subjectId;
    private static final String TEST_TEACHER_LOGIN = "test_teacher";

    @BeforeAll
    public void setUpCreateScheduleTest() {
        String token = jwtAuthService.generateToken(TEST_TEACHER_LOGIN, List.of(AccountConsts.ROLE_TEACHER), false);
        teacherWithEditHeaders = new Headers(new Header("Authorization", "Bearer " + token));

        String otherToken = jwtAuthService.generateToken("mkarbowańczyk", List.of(AccountConsts.ROLE_TEACHER), false);
        teacherWithoutEditHeaders = new Headers(new Header("Authorization", "Bearer " + otherToken));

        SubjectAssignment assignment = subjectAssignmentTestRepository.findByTeacherAccountLogin(TEST_TEACHER_LOGIN).getFirst();
        subjectId = assignment.getSubject().getId();
    }

    @AfterEach
    public void tearDownCreateScheduleTest() {
        scheduleTestRepository.deleteAll(
                scheduleTestRepository.findAll().stream()
                        .filter(s -> "zad1".equals(s.getTag()))
                        .toList()
        );
    }

    @Test
    public void POS1_TeacherWithRequiredPermissions_ShouldCreateSchedule() throws JsonProcessingException {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");

        ScheduleDTO dto = new ScheduleDTO(null, LocalDateTime.now().plusDays(5), "zad1");

        String response = requestFunctions.sendPostRequest(
                objectMapper.writeValueAsString(dto),
                HttpStatus.CREATED.value(),
                "/api/schedules/subject/" + subjectId,
                teacherWithEditHeaders
        );

        ScheduleDTO createdSchedule = objectMapper.readValue(response, ScheduleDTO.class);
        assertAll(
                () -> assertNotNull(createdSchedule),
                () -> assertNotNull(createdSchedule.id()),
                () -> assertEquals("zad1", createdSchedule.tag())
        );
    }

    @Test
    public void NEG01_TeacherWithoutRequiredPermissions_ShouldNotCreateSchedule() throws JsonProcessingException {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");
        ScheduleDTO dto = new ScheduleDTO(null, LocalDateTime.now().plusDays(5), "zad1");

        requestFunctions.sendPostRequest(
                objectMapper.writeValueAsString(dto),
                HttpStatus.FORBIDDEN.value(),
                "/api/schedules/subject/" + subjectId,
                teacherWithoutEditHeaders
        );
    }

    @Test
    public void NEG02_Unauthenticated_ShouldNotCreateSchedule() throws JsonProcessingException {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");
        ScheduleDTO dto = new ScheduleDTO(null, LocalDateTime.now().plusDays(5), "zad1");

        requestFunctions.sendPostRequest(
                objectMapper.writeValueAsString(dto),
                HttpStatus.UNAUTHORIZED.value(),
                "/api/schedules/subject/" + subjectId,
                new Headers()
        );
    }
}
