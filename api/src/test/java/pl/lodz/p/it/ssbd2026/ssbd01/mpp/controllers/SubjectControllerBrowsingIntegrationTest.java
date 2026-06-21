package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.SubjectDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.SubjectAssignment;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.SubjectAssignmentTestRepository;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SubjectControllerBrowsingIntegrationTest extends BaseTestSetup {

    @LocalServerPort
    private int port;

    @Autowired
    private RequestFunctions requestFunctions;

    @Autowired
    private JwtAuthService jwtAuthService;

    @Autowired
    @Qualifier("adminTransactionManager")
    private PlatformTransactionManager transactionManager;

    @Autowired
    private SubjectAssignmentTestRepository subjectAssignmentTestRepository;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private Headers adminHeaders;
    private Headers teacherHeaders;
    private Headers studentHeaders;

    private UUID testSubjectId;

    @BeforeAll
    public void setUp() {
        String adminToken = jwtAuthService.generateToken("test_admin", List.of(AccountConsts.ROLE_ADMIN), false);
        adminHeaders = new Headers(new Header("Authorization", "Bearer " + adminToken));

        String teacherToken = jwtAuthService.generateToken("test_teacher", List.of(AccountConsts.ROLE_TEACHER), false);
        teacherHeaders = new Headers(new Header("Authorization", "Bearer " + teacherToken));

        String studentToken = jwtAuthService.generateToken("test_student", List.of(AccountConsts.ROLE_STUDENT), false);
        studentHeaders = new Headers(new Header("Authorization", "Bearer " + studentToken));

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.execute(status -> {
            SubjectAssignment assignment = subjectAssignmentTestRepository.findByTeacherAccountLogin("test_teacher").getFirst();
            testSubjectId = assignment.getSubject().getId();
            return null;
        });
    }

    @BeforeEach
    public void setupRestAssured() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    @DisplayName("POS01: Admin pobiera listę wszystkich przedmiotów")
    public void POS01_AdminShouldGetAllSubjects() throws JsonProcessingException {
        String response = requestFunctions.sendGetRequest(
                HttpStatus.OK.value(), "/api/subjects/all", adminHeaders);

        List<SubjectDTO> subjects = Arrays.asList(objectMapper.readValue(response, SubjectDTO[].class));

        assertFalse(subjects.isEmpty());
        assertTrue(subjects.get(0).canEdit());
    }

    @Test
    @DisplayName("POS02: Prowadzący pobiera listę swoich przedmiotów")
    public void POS02_TeacherShouldGetOwnSubjects() throws JsonProcessingException {
        String response = requestFunctions.sendGetRequest(
                HttpStatus.OK.value(), "/api/subjects", teacherHeaders);

        List<SubjectDTO> subjects = Arrays.asList(objectMapper.readValue(response, SubjectDTO[].class));

        assertFalse(subjects.isEmpty());
        assertTrue(subjects.get(0).canEdit());
    }

    @Test
    @DisplayName("POS03: Student pobiera listę swoich przedmiotów")
    public void POS03_StudentShouldBeAbleToHitEndpoint() throws JsonProcessingException {
        String response = requestFunctions.sendGetRequest(
                HttpStatus.OK.value(), "/api/subjects/student/list", studentHeaders);

        List<SubjectDTO> subjects = Arrays.asList(objectMapper.readValue(response, SubjectDTO[].class));
        assertNotNull(subjects);
    }

    @Test
    @DisplayName("POS04: Prowadzący pobiera szczegóły przedmiotu")
    public void POS04_TeacherShouldGetSubjectDetails() throws JsonProcessingException {
        String response = requestFunctions.sendGetRequest(
                HttpStatus.OK.value(), "/api/subjects/" + testSubjectId, teacherHeaders);

        SubjectDTO subject = objectMapper.readValue(response, SubjectDTO.class);

        assertNotNull(subject);
        assertEquals(testSubjectId, subject.id());
    }

    @Test
    @DisplayName("NEG01: 404 dla nieistniejącego przedmiotu")
    public void NEG01_ShouldReturn404ForNonExistentSubject() {
        requestFunctions.sendGetRequest(
                HttpStatus.NOT_FOUND.value(), "/api/subjects/" + UUID.randomUUID(), teacherHeaders);
    }

    @Test
    @DisplayName("NEG02: 401 dla niezalogowanego")
    public void NEG02_UnauthenticatedUserShouldGet401() {
        requestFunctions.sendGetRequest(
                HttpStatus.UNAUTHORIZED.value(), "/api/subjects", new Headers());
    }
}