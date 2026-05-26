package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.FilterableRequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.RulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.SubjectDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;

class SubjectControllerIT extends BaseTestSetup {

    @Autowired
    private JwtAuthService jwtAuthService;

    private SubjectDTO validDto;
    private String teacherToken;
    private String studentToken;

    @BeforeEach
    public void setup() {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");

        teacherToken = jwtAuthService.generateToken("jkowalski", List.of("TEACHER"), false);
        studentToken = jwtAuthService.generateToken("anowak", List.of("STUDENT"), false);

        RulePresetDTO rules = new RulePresetDTO(UUID.randomUUID(), "PEŁNY_WGLĄD", 1, 1, true);

        validDto = new SubjectDTO(
                null, null, "Inżynieria Oprogramowania", "elo zelo", "2026",
                "Opis przedmiotu", "https://gitea.example.com/io",
                null, null, null, null, null, null, rules
        );
    }

    @Test
    void createSubject_Success() {
        given()
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(ContentType.JSON)
                .body(validDto)
                .when()
                .post("/api/subjects")
                .then()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    void createSubject_ValidationError() {
        SubjectDTO invalidDto = new SubjectDTO(
                null, null, "", "", "",
                "Opis", "invalid_url",
                null, null, null, null, null, null, null
        );

        given()
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(ContentType.JSON)
                .body(invalidDto)
                .when()
                .post("/api/subjects")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void createSubject_ForbiddenForStudent() {
        given()
                .header("Authorization", "Bearer " + studentToken)
                .contentType(ContentType.JSON)
                .body(validDto)
                .when()
                .post("/api/subjects")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }
}