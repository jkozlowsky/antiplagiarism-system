package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
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
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.TeacherAssignmentDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.UpdateSubjectDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.SubjectAssignment;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.TeacherSubjectRole;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.LanguageDictRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.SubjectAssignmentTestRepository;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public class SubjectControllerUpdateIntegrationTest extends BaseTestSetup {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtAuthService jwtAuthService;

    @Autowired
    private SubjectAssignmentTestRepository subjectAssignmentTestRepository;

    @Autowired
    private VersionHashService versionHashService;

    @Autowired
    @Qualifier("adminTransactionManager")
    private PlatformTransactionManager transactionManager;

    private String adminToken;
    private String teacherToken;
    private UUID testSubjectId;
    private String validETag;

    @BeforeAll
    public void setUp() {
        adminToken = jwtAuthService.generateToken("test_admin", List.of(AccountConsts.ROLE_ADMIN), false);
        teacherToken = jwtAuthService.generateToken("test_teacher", List.of(AccountConsts.ROLE_TEACHER), false);

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.execute(status -> {
            SubjectAssignment assignment = subjectAssignmentTestRepository.findByTeacherAccountLogin("test_teacher").getFirst();
            testSubjectId = assignment.getSubject().getId();
            validETag = versionHashService.hashObjectVersion(testSubjectId, assignment.getSubject().getVersion());
            return null;
        });
    }

    @BeforeEach
    public void setupRestAssured() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    @DisplayName("POS01: Poprawna aktualizacja przedmiotu zwraca 204 No Content")
    public void POS01_ShouldUpdateSubjectAndReturn204() {
        UpdateSubjectDTO validDto = new UpdateSubjectDTO(
                "Zaktualizowana Nazwa",
                "Nowy opis",

                List.of(new TeacherAssignmentDTO("test_teacher", TeacherSubjectRole.OWNER)),
                null
        );

        given()
                .header("Authorization", "Bearer " + teacherToken)
                .header("If-Match", validETag)
                .header("Accept-Language", "pl")
                .contentType(ContentType.JSON)
                .body(validDto)
                .when()
                .put("/api/subjects/" + testSubjectId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DisplayName("NEG01: Aktualizacja bez If-Match (ETag) powinna zwrócić 400 Bad Request")
    public void NEG01_ShouldReturnBadRequestWhenMissingIfMatchHeader() {
        UpdateSubjectDTO validDto = new UpdateSubjectDTO("Test", "Desc",  List.of(), null);

        given()
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(ContentType.JSON)
                .body(validDto)
                .when()
                .put("/api/subjects/" + testSubjectId)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("NEG02: Aktualizacja ze złą walidacją DTO (pusta nazwa) zwraca 400")
    public void NEG02_ShouldReturn400ForValidationFailure() {
        UpdateSubjectDTO invalidDto = new UpdateSubjectDTO(
                "",
                "Desc",
                List.of(new TeacherAssignmentDTO("test_teacher", TeacherSubjectRole.OWNER)),
                null
        );

        given()
                .header("Authorization", "Bearer " + teacherToken)
                .header("If-Match", validETag)
                .contentType(ContentType.JSON)
                .body(invalidDto)
                .when()
                .put("/api/subjects/" + testSubjectId)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }


    @Test
    @DisplayName("NEG03: Admin nie powinien mieć uprawnień do edycji przedmiotu")
    public void NEG03_AdminShouldNotBeAbleToUpdateSubject() {
        UpdateSubjectDTO validDto = new UpdateSubjectDTO(
                "Nazwa Admina",
                "Opis",
                List.of(new TeacherAssignmentDTO("test_teacher", TeacherSubjectRole.OWNER)),
                null
        );

        given()
                .header("Authorization", "Bearer " + adminToken)
                .header("If-Match", validETag)
                .header("Accept-Language", "pl")
                .contentType(ContentType.JSON)
                .body(validDto)
                .when()
                .put("/api/subjects/" + testSubjectId)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }
}