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
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
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
    @Autowired
    private VersionHashService versionHashService;

    private RequestFunctions requestFunctions;
    private Headers adminHeaders;
    private Headers studentHeaders;
    private Headers unauthenticatedHeaders;

    @BeforeEach
    public void setUpRestAssuredAndAuth() {
        String adminToken = jwtAuthService.generateToken("admin", List.of(AccountConsts.ROLE_ADMIN));
        String studentToken = jwtAuthService.generateToken("anowak", List.of(AccountConsts.ROLE_STUDENT));
        adminHeaders = new Headers(new Header("Authorization", "Bearer " + adminToken));
        studentHeaders = new Headers(new Header("Authorization", "Bearer " + studentToken));
        unauthenticatedHeaders = new Headers();
        requestFunctions = new RequestFunctions();
    }

    @Test
    public void POS01_ChangeEmailByAdmin_ShouldBeSuccessful_AsAdmin() {
        Account account = accountTestRepository.findByLogin("anowak").orElseThrow();
        String hash = versionHashService.hashObjectVersion(account.getId(), account.getVersion());
        String newEmail = "new1@edu.p.lodz.pl";
        String reqBody = createRequestBody(newEmail, hash);

        String response = requestFunctions.sendPatchRequest(
                reqBody,
                HttpStatus.OK.value(),
                "/api/account/" + account.getId() + "/email",
                new Headers(new Header("if-Match", hash))
        );

        String responseEmail = JsonPath.from(response).getString("email");
        assertEquals(newEmail, responseEmail);
    }

    @Test
    public void NEG01_ChangeEmailByAdmin_ShouldFail_AsStudent() {
        Account account = accountTestRepository.findByLogin("admin").orElseThrow();
        String hash = versionHashService.hashObjectVersion(account.getId(), account.getVersion());
        String newEmail = "new2@edu.p.lodz.pl";
        String reqBody = createRequestBody(newEmail, hash);

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
        String hash = versionHashService.hashObjectVersion(account.getId(), account.getVersion());
        String newEmail = "new3@edu.p.lodz.pl";
        String reqBody = createRequestBody(newEmail, hash);

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
        String badHash = versionHashService.hashObjectVersion(account.getId(), account.getVersion() - 1);
        String newEmail = "new4@edu.p.lodz.pl";
        String reqBody = createRequestBody(newEmail, badHash);

        requestFunctions.sendPatchRequest(
                reqBody,
                HttpStatus.CONFLICT.value(),
                "/api/account/" + account.getId() + "/email",
                new Headers(new Header("if-Match",badHash))
        );
    }

    @Test
    public void NEG04_ChangeEmailByAdmin_ShouldFail_ForNonExistentAccount() {
        String newEmail = "new5@edu.p.lodz.pl";
        String reqBody = createRequestBody(newEmail, "60");
        UUID accountId = generateNonExistentAccountId();

        requestFunctions.sendPatchRequest(
                reqBody,
                HttpStatus.NOT_FOUND.value(),
                "/api/account/" + accountId + "/email",
                new Headers(new Header("if-Match", ""))
        );
    }

    @Test
    public void NEG05_ChangeEmailByAdmin_ShouldFail_ForInvalidEmailFormat() {
        Account account = accountTestRepository.findByLogin("anowak").orElseThrow();
        String hash = versionHashService.hashObjectVersion(account.getId(), account.getVersion());
        String newEmail = "????.edu.p.lodz.pl";
        String reqBody = createRequestBody(newEmail, hash);

        requestFunctions.sendPatchRequest(
                reqBody,
                HttpStatus.BAD_REQUEST.value(),
                "/api/account/" + account.getId() + "/email",
                adminHeaders
        );
    }

    private String createRequestBody(String newEmail, String version) {
        return """
                {
                "email": "%s",
                "version": "%s"
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
