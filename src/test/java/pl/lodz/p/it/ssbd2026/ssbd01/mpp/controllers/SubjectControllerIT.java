package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.FilterableRequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.CreateSubjectManualDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.CreateSubjectTemplateDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.RulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.GiteaService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.*;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class SubjectControllerIT extends BaseTestSetup {

    @Autowired
    private JwtAuthService jwtAuthService;

    @MockitoBean
    private GiteaService giteaService;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SubjectAssignmentRepository subjectAssignmentRepository;

    @Autowired
    private GiteaRepositoryRepository giteaRepositoryRepository;

    @Autowired
    @Qualifier("mppTransactionManager")
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    private String teacherToken;
    private String studentToken;

    private UUID testSubjectId;
    private UUID templateId;

    @BeforeEach
    public void setup() {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");

        transactionTemplate = new TransactionTemplate(transactionManager);

        teacherToken = jwtAuthService.generateToken("test_teacher", List.of("TEACHER"), false);
        studentToken = jwtAuthService.generateToken("test_student", List.of("STUDENT"), false);

        when(giteaService.getRepositoriesFromOrganization(anyString())).thenReturn(List.of());
        when(giteaService.getRepoCollaborators(anyString(), anyString())).thenReturn(List.of());

        testSubjectId = subjectRepository.findAll().get(0).getId();
        templateId = ruleRepository.findAll().stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsPreset()))
                .findFirst()
                .orElseThrow()
                .getId();
    }

    @Test
    void createSubjectManual_Success() {
        RulePresetDTO rules = new RulePresetDTO(UUID.randomUUID(), "PEŁNY_WGLĄD", 1, 1, true, "hash123");
        CreateSubjectManualDTO validDto = new CreateSubjectManualDTO(
                "Inżynieria Oprogramowania", "io-org-manual", "2026",
                "Opis przedmiotu",
                rules, null
        );

        given()
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(ContentType.JSON)
                .body(validDto)
                .when()
                .post("/api/subjects/manual")
                .then()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    void createSubjectTemplate_Success() {
        CreateSubjectTemplateDTO validTemplateDto = new CreateSubjectTemplateDTO(
                "Bazy Danych", "bd-org-template", "2026",
                "Opis przedmiotu",
                templateId, "PEŁNY_WGLĄD", null
        );

        given()
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(ContentType.JSON)
                .body(validTemplateDto)
                .when()
                .post("/api/subjects/template")
                .then()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    void createSubject_ValidationError() {
        given()
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(ContentType.JSON)
                .body("[]")
                .when()
                .post("/api/subjects/manual")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void createSubject_ForbiddenForStudent() {
        RulePresetDTO rules = new RulePresetDTO(UUID.randomUUID(), "PEŁNY_WGLĄD", 1, 1, true, "hash123");
        CreateSubjectManualDTO validDto = new CreateSubjectManualDTO(
                "Inżynieria Oprogramowania", "io-org", "2026",
                "Opis przedmiotu",
                rules, null
        );

        given()
                .header("Authorization", "Bearer " + studentToken)
                .contentType(ContentType.JSON)
                .body(validDto)
                .when()
                .post("/api/subjects/manual")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void syncSubjectWithGitea_Success() {
        assignTeacherToTestSubject();

        given()
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(ContentType.JSON)
                .when()
                .post("/api/subjects/{subjectId}/sync", testSubjectId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    void getStudentSubjectDetails_Success() {
        assignStudentToTestSubject();

        given()
                .header("Authorization", "Bearer " + studentToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/subjects/{subjectId}/student", testSubjectId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("name", org.hamcrest.Matchers.notNullValue())
                .body("reportVisibilityLevel", org.hamcrest.Matchers.notNullValue());
    }

    @Test
    void getStudentSubjectDetails_ForbiddenForTeacher() {
        given()
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/subjects/{subjectId}/student", testSubjectId)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void getTeacherSubjectDetails_ForbiddenForStudent() {
        given()
                .header("Authorization", "Bearer " + studentToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/subjects/{subjectId}", testSubjectId)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    private void assignTeacherToTestSubject() {
        transactionTemplate.execute(status -> {
            Teacher teacher = teacherRepository.findActiveTeacherByLogin("test_teacher").orElseThrow();
            Subject subject = subjectRepository.findById(testSubjectId).orElseThrow();

            boolean exists = subjectAssignmentRepository.findByTeacherLoginAndSubjectId("test_teacher", testSubjectId).isPresent();

            if (!exists) {
                SubjectAssignment assignment = new SubjectAssignment();
                assignment.setSubject(subject);
                assignment.setTeacher(teacher);
                assignment.setSubjectRole(TeacherSubjectRole.OWNER);
                subjectAssignmentRepository.saveAndFlush(assignment);
            }
            return null;
        });
    }

    private void assignStudentToTestSubject() {
        transactionTemplate.execute(status -> {
            Student student = studentRepository.findAll().stream()
                    .filter(s -> "test_student".equals(s.getAccount().getLogin()))
                    .findFirst()
                    .orElseThrow();

            Subject subject = subjectRepository.findById(testSubjectId).orElseThrow();

            List<Repository> repos = giteaRepositoryRepository.findBySubjectId(testSubjectId);
            Repository targetRepo;

            if (repos.isEmpty()) {
                targetRepo = new Repository();
                targetRepo.setRepositoryName("test-repo");
                targetRepo.setSubject(subject);
                targetRepo.setStudentList(new ArrayList<>());
            } else {
                targetRepo = repos.get(0);
            }

            if (!targetRepo.getStudentList().contains(student)) {
                targetRepo.getStudentList().add(student);
            }

            giteaRepositoryRepository.saveAndFlush(targetRepo);
            return null;
        });
    }
}