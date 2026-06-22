package pl.lodz.p.it.ssbd2026.emailservice.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.lodz.p.it.ssbd2026.emailservice.service.EmailService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmailControllerIT {

    @LocalServerPort
    private int port;

    @MockitoBean
    private EmailService emailService;

    @BeforeEach
    public void setUpRestAssured() {
        RestAssured.port = port;
    }

    @TestConfiguration
    static class SyncAsyncTestConfig {
        @Bean
        @Primary
        public TaskExecutor taskExecutor() {
            return new SyncTaskExecutor();
        }
    }

    @Test
    @DisplayName("Pomyślne odebranie żądania i wywołanie wysłania e-maila aktywacyjnego")
    public void shouldHandleActivationEmailSuccessfully() {
        String requestBody = """
                {
                    "toAddress": "student@example.com",
                    "token": "secret-token-123",
                    "langCode": "pl",
                    "frontendUrl": "http://localhost:3000"
                }
                """;

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/emails/activation")
                .then()
                .statusCode(200);

        verify(emailService, times(1)).sendActivationEmail(
                eq("student@example.com"),
                eq("secret-token-123"),
                eq("pl"),
                eq("http://localhost:3000")
        );
    }

    @Test
    @DisplayName("Pomyślne odebranie żądania i wywołanie asynchronicznego wysłania e-maila dla powiadomienia admina")
    public void shouldHandleAdminLoginNotificationSuccessfully() {
        String requestBody = """
                {
                    "toAddress": "admin@ssbd.pl",
                    "langCode": "uk",
                    "login": "superadmin",
                    "ipAddress": "192.168.1.1"
                }
                """;

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/emails/admin-login")
                .then()
                .statusCode(200);

        verify(emailService, times(1)).sendAdminLoginNotification(
                eq("admin@ssbd.pl"),
                eq("superadmin"),
                eq("192.168.1.1"),
                eq("uk")
        );
    }

    @Test
    @DisplayName("Pomyślne przetworzenie żądania zmiany e-maila przez administratora")
    public void shouldHandleEmailChangedByAdminSuccessfully() {
        String requestBody = """
                {
                    "toAddress": "user@example.com",
                    "langCode": "en"
                }
                """;

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/emails/email-changed-by-admin")
                .then()
                .statusCode(200);

        verify(emailService, times(1)).sendEmailAddressChangedByAdmin(
                eq("user@example.com"),
                eq("en")
        );
    }
}