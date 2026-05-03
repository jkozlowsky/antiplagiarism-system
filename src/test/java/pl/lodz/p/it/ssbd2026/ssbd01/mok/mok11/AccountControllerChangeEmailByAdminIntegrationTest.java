package pl.lodz.p.it.ssbd2026.ssbd01.mok.mok11;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.AccountTestRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AccountControllerChangeEmailByAdminIntegrationTest extends BaseTestSetup {
    @Autowired
    private AccountTestRepository accountTestRepository;
    @Autowired
    private JwtAuthService jwtAuthService;

    private RequestFunctions requestFunctions;
    private Headers adminHeaders;
    private Headers studentHeaders;
    private Headers unauthenticatedHeaders;

    @BeforeEach
    public void setUpRestAssuredAndAuth() {
        String adminToken = jwtAuthService.generateToken("admin", List.of(AccountConsts.ROLE_ADMIN), 1);
        String studentToken = jwtAuthService.generateToken("anowak", List.of(AccountConsts.ROLE_STUDENT),1);
        adminHeaders = new Headers(new Header("Authorization", "Bearer " + adminToken));
        studentHeaders = new Headers(new Header("Authorization", "Bearer " + studentToken));
        unauthenticatedHeaders = new Headers();
        requestFunctions = new RequestFunctions();
    }

    @Test
    public void POS01_ChangeEmailByAdmin_ShouldBeSuccessful_AsAdmin() {
        Account account = accountTestRepository.findByLogin("anowak").orElseThrow();
        String newEmail = "new1@edu.p.lodz.pl";
        String reqBody = createRequestBody(newEmail, account.getVersion());

        String response = requestFunctions.sendPatchRequest(
                reqBody,
                HttpStatus.OK.value(),
                "/api/account/" + account.getId() + "/email",
                adminHeaders
        );

        String responseEmail = JsonPath.from(response).getString("email");
        assertEquals(newEmail, responseEmail);
    }

    @Test
    public void NEG01_ChangeEmailByAdmin_ShouldFail_AsStudent() {
        Account account = accountTestRepository.findByLogin("admin").orElseThrow();
        String newEmail = "new2@edu.p.lodz.pl";
        String reqBody = createRequestBody(newEmail, account.getVersion());

        sendPatchRequestWithoutGlobalAdminToken(
                reqBody,
                HttpStatus.FORBIDDEN.value(),
                "/api/account/" + account.getId() + "/email",
                studentHeaders
        );
    }

    @Test
    public void NEG02_ChangeEmail_ByAdmin_ShouldFail_AsUnauthenticated() {
        Account account = accountTestRepository.findByLogin("jkowalski").orElseThrow();
        String newEmail = "new3@edu.p.lodz.pl";
        String reqBody = createRequestBody(newEmail, account.getVersion());

        sendPatchRequestWithoutGlobalAdminToken(
                reqBody,
                HttpStatus.FORBIDDEN.value(), //TODO: UNAUTHORIZED?
                "/api/account/" + account.getId() + "/email",
                unauthenticatedHeaders
        );
    }

    @Test
    public void NEG03_ChangeEmailByAdmin_ShouldFail_ForInconsistentVersion() {
        Account account = accountTestRepository.findByLogin("jkowalski").orElseThrow();
        String newEmail = "new4@edu.p.lodz.pl";
        String reqBody = createRequestBody(newEmail, account.getVersion() - 1);

        requestFunctions.sendPatchRequest(
                reqBody,
                HttpStatus.CONFLICT.value(),
                "/api/account/" + account.getId() + "/email",
                adminHeaders
        );
    }

    @Test
    public void NEG04_ChangeEmailByAdmin_ShouldFail_ForNonExistentAccount() {
        String newEmail = "new5@edu.p.lodz.pl";
        String reqBody = createRequestBody(newEmail, 60);
        UUID accountId = generateNonExistentAccountId();

        requestFunctions.sendPatchRequest(
                reqBody,
                HttpStatus.NOT_FOUND.value(),
                "/api/account/" + accountId + "/email",
                adminHeaders
        );
    }

    @Test
    public void NEG05_ChangeEmailByAdmin_ShouldFail_ForInvalidEmailFormat() {
        Account account = accountTestRepository.findByLogin("anowak").orElseThrow();
        String newEmail = "????.edu.p.lodz.pl";
        String reqBody = createRequestBody(newEmail, account.getVersion());

        requestFunctions.sendPatchRequest(
                reqBody,
                HttpStatus.BAD_REQUEST.value(),
                "/api/account/" + account.getId() + "/email",
                adminHeaders
        );
    }

    private String createRequestBody(String newEmail, int version) {
        return """
                {
                "email": "%s",
                "version": %d
                }
                """.formatted(newEmail, version);
    }

    private UUID generateNonExistentAccountId() {
        UUID accountId = UUID.randomUUID();

        while (accountTestRepository.findById(accountId).isPresent()) {
            accountId = UUID.randomUUID();
        }

        return accountId;
    }

    public void sendPatchRequestWithoutGlobalAdminToken(String body, int expectedStatusCode, String url, Headers headers) {
        RestAssured.given()
                .auth().none()
                .header("Authorization", "")
                .headers(headers)
                .body(body)
                .when()
                .patch(url)
                .then()
                .statusCode(expectedStatusCode)
                .extract()
                .body()
                .asString();
    }
}
