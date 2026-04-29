package pl.lodz.p.it.ssbd2026.ssbd01.mok.controllers;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.LanguageDict;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.AccountTestRepository;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;

public class GrantAccessLevelTest extends BaseTestSetup {

    @Autowired
    private AccountTestRepository accountRepository;

    @Autowired
    private JwtAuthService jwtAuthService;

    @LocalServerPort
    private int port;

    private RequestSpecification adminRequestSpec;
    private RequestSpecification studentRequestSpec;
    private RequestSpecification unauthenticatedRequestSpec;

    @BeforeEach
    public void setUpRestAssuredAndAuth() {
        String adminToken = jwtAuthService.generateToken("admin", List.of("ADMINISTRATOR"));
        String studentToken = jwtAuthService.generateToken("student", List.of("STUDENT"));

        adminRequestSpec = new RequestSpecBuilder()
                .setBaseUri("http://localhost")
                .setPort(port)
                .setBasePath("/api")
                .setContentType(ContentType.JSON)
                .addHeader("Authorization", "Bearer " + adminToken)
                .build();

        studentRequestSpec = new RequestSpecBuilder()
                .setBaseUri("http://localhost")
                .setPort(port)
                .setBasePath("/api")
                .setContentType(ContentType.JSON)
                .addHeader("Authorization", "Bearer " + studentToken)
                .build();

        unauthenticatedRequestSpec = new RequestSpecBuilder()
                .setBaseUri("http://localhost")
                .setPort(port)
                .setBasePath("/api")
                .setContentType(ContentType.JSON)
                .build();
    }

    private Account createTestAccount(String prefix) {
        Account admin = accountRepository.findByLogin("admin").get();
        LanguageDict language = admin.getLanguageVersion();
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

        Account testAccount = new Account();
        testAccount.setLogin(prefix + "_" + uniqueId);
        testAccount.setEmail(prefix + "_" + uniqueId + "@example.com");
        testAccount.setPasswordHash("password_hash");
        testAccount.setName("TestName");
        testAccount.setSurname("TestSurname");
        testAccount.setIsBlocked(false);
        testAccount.setLanguageVersion(language);

        return accountRepository.save(testAccount);
    }

    @Test
    @DisplayName("Pomyślne nadanie uprawnień dla istniejącego konta")
    public void shouldGrantAccessLevelSuccessfully() {
        Account testAccount = createTestAccount("user");
        UUID accountId = testAccount.getId();
        Integer currentVersion = testAccount.getVersion();
        String requestBody = "{\"version\": " + currentVersion + "}";

        RestAssured.given(adminRequestSpec)
                .body(requestBody)
                .when()
                .post("/account/{id}/access-levels/{accessLevelName}", accountId, "TEACHER")
                .then()
                .statusCode(200)
                .body("account.login", equalTo(testAccount.getLogin()));
    }

    @Test
    @DisplayName("Odrzucenie próby nadania poziomu dostępu dla nieistniejącego konta")
    public void shouldFailForNonExistentAccount() {
        UUID nonExistentId = UUID.randomUUID();
        String requestBody = "{\"version\": 0}";

        RestAssured.given(adminRequestSpec)
                .body(requestBody)
                .when()
                .post("/account/{id}/access-levels/{accessLevelName}", nonExistentId, "STUDENT")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Odrzucenie próby nadania poziomu dostępu dla przestarzałej wersji obiektu")
    public void shouldFailForWrongVersion() {
        Account testAccount = createTestAccount("ver");
        UUID accountId = testAccount.getId();
        Integer wrongVersion = testAccount.getVersion() + 1;
        String requestBody = "{\"version\": " + wrongVersion + "}";

        RestAssured.given(adminRequestSpec)
                .body(requestBody)
                .when()
                .post("/account/{id}/access-levels/{accessLevelName}", accountId, "ADMINISTRATOR")
                .then()
                .statusCode(409);
    }

    @Test
    @DisplayName("Odrzucenie próby nadania poziomu dostępu bez ważnego tokena uwierzytelniającego")
    public void shouldFailWithoutToken() {
        Account testAccount = createTestAccount("auth");
        UUID accountId = testAccount.getId();
        String requestBody = "{\"version\": " + testAccount.getVersion() + "}";

        RestAssured.given(unauthenticatedRequestSpec)
                .body(requestBody)
                .when()
                .post("/account/{id}/access-levels/{accessLevelName}", accountId, "TEACHER")
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("Odrzucenie próby nadania poziomu dostępu z uprawnieniami studenta zamiast administratora")
    public void shouldFailWithStudentToken() {
        Account testAccount = createTestAccount("priv");
        UUID accountId = testAccount.getId();
        String requestBody = "{\"version\": " + testAccount.getVersion() + "}";

        RestAssured.given(studentRequestSpec)
                .body(requestBody)
                .when()
                .post("/account/{id}/access-levels/{accessLevelName}", accountId, "TEACHER")
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("Odrzucenie próby przypisania poziomu dostępu, który użytkownik już posiada")
    public void shouldFailWhenRoleAlreadyAssigned() {
        Account testAccount = createTestAccount("dup");
        UUID accountId = testAccount.getId();
        Integer currentVersion = testAccount.getVersion();
        String requestBody1 = "{\"version\": " + currentVersion + "}";

        RestAssured.given(adminRequestSpec)
                .body(requestBody1)
                .when()
                .post("/account/{id}/access-levels/{accessLevelName}", accountId, "TEACHER")
                .then()
                .statusCode(200);

        Account updatedAccount = accountRepository.findById(accountId).get();
        String requestBody2 = "{\"version\": " + updatedAccount.getVersion() + "}";

        RestAssured.given(adminRequestSpec)
                .body(requestBody2)
                .when()
                .post("/account/{id}/access-levels/{accessLevelName}", accountId, "TEACHER")
                .then()
                .statusCode(409);
    }

    @Test
    @DisplayName("Odrzucenie próby nadania poziomu dostępu, który nie istnieje w słowniku")
    public void shouldFailForNonExistentRole() {
        Account testAccount = createTestAccount("role");
        UUID accountId = testAccount.getId();
        String requestBody = "{\"version\": " + testAccount.getVersion() + "}";

        RestAssured.given(adminRequestSpec)
                .body(requestBody)
                .when()
                .post("/account/{id}/access-levels/{accessLevelName}", accountId, "SUPER_HERO")
                .then()
                .statusCode(not(200));
    }
}