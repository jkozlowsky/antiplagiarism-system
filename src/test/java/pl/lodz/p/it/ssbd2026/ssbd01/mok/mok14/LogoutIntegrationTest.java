package pl.lodz.p.it.ssbd2026.ssbd01.mok.mok14;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.specification.FilterableRequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.AccessLevel;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.AccountTestRepository;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class LogoutIntegrationTest extends BaseTestSetup {
    @Autowired
    private AccountTestRepository accountTestRepository;

    @Autowired
    private JwtAuthService jwtAuthService;

    @Autowired
    @Qualifier("adminTransactionManager")
    private PlatformTransactionManager transactionManager;

    private RequestFunctions requestFunctions;
    private Account account;
    private String token;
    private Headers headers;

    @BeforeEach
    public void setUpRestAssuredAndAuth() {
        requestFunctions = new RequestFunctions();

        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization"); //usuwanie globalengo tokenu admina

        TransactionTemplate transTemp = new TransactionTemplate(transactionManager);
        token = transTemp.execute(status -> {
            account = accountTestRepository.findByLogin("anowak").orElseThrow();

            return jwtAuthService.generateToken(
                    account.getLogin(),
                    account.getAccessLevelList().stream()
                            .map(AccessLevel::getAccessLevelName)
                            .collect(Collectors.toList())
            );
        });

        headers = new Headers(new Header("Authorization", "Bearer " + token));
    }

    @Test
    public void POS01_Logout_ShouldReturnNoContent() {
        requestFunctions.sendPostRequest("", HttpStatus.NO_CONTENT.value(), "api/auth/logout", headers);

        assertTrue(jwtAuthService.isTokenBlacklisted(token));
    }

    @Test
    public void NEG01_Logout_AccessToProtectedEndpoint_ShouldReturn40XStatusCode() {
        requestFunctions.sendPostRequest("", HttpStatus.NO_CONTENT.value(), "api/auth/logout", headers);
        requestFunctions.sendGetRequest(HttpStatus.FORBIDDEN.value(), "api/account", headers); //TODO UNAUTHORIZED?
    }

}
