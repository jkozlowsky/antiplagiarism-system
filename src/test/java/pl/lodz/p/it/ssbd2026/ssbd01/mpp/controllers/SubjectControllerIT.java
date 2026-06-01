package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.FilterableRequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.CreateSubjectManualDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.CreateSubjectTemplateDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.RulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.GiteaService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;

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

    private String teacherToken;
    private String studentToken;

    private final UUID testSubjectId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @BeforeEach
    public void setup() {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");

        teacherToken = jwtAuthService.generateToken("jkowalski", List.of("TEACHER"), false);
        studentToken = jwtAuthService.generateToken("anowak", List.of("STUDENT"), false);

        when(giteaService.getRepositoriesFromOrganization(anyString())).thenReturn(List.of());
        when(giteaService.getRepoCollaborators(anyString(), anyString())).thenReturn(List.of());
    }

    @Test
    void createSubjectManual_Success() {
        RulePresetDTO rules = new RulePresetDTO(UUID.randomUUID(), "PEŁNY_WGLĄD", 1, 1, true, 1, "hash123");
        CreateSubjectManualDTO validDto = new CreateSubjectManualDTO(
                "Inżynieria Oprogramowania", "io-org-manual", "2026",
                "Opis przedmiotu", "https://gitea.example.com/io",
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
                "Opis przedmiotu", "https://gitea.example.com/bd",
                UUID.fromString("44444444-4444-4444-4444-444444444444"), "PEŁNY_WGLĄD", null
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
        CreateSubjectManualDTO invalidDto = new CreateSubjectManualDTO(
                "", "", "", "Opis", "invalid_url", null, null
        );

        given()
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(ContentType.JSON)
                .body(invalidDto)
                .when()
                .post("/api/subjects/manual")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void createSubject_ForbiddenForStudent() {
        RulePresetDTO rules = new RulePresetDTO(UUID.randomUUID(), "PEŁNY_WGLĄD", 1, 1, true, 1, "hash123");
        CreateSubjectManualDTO validDto = new CreateSubjectManualDTO(
                "Inżynieria Oprogramowania", "io-org", "2026",
                "Opis przedmiotu", "https://gitea.example.com/io",
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
        given()
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(ContentType.JSON)
                .when()
                .post("/api/subjects/" + testSubjectId + "/sync")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }
}