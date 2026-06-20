package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.FilterableRequestSpecification;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RuleRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GetRulePresetsTest extends BaseTestSetup {

    @Autowired
    private JwtAuthService jwtAuthService;

    @Autowired
    private RequestFunctions requestFunctions;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private PlatformTransactionManager mppTransactionManager;

    @Autowired
    private EntityManager entityManager;

    private Headers teacherHeaders;
    private Headers studentHeaders;
    private Headers unauthenticatedHeaders;

    @BeforeEach
    public void setupDataAndAuth() {
        String teacherToken = jwtAuthService.generateToken("teacher", List.of(AccountConsts.ROLE_TEACHER), false);
        String studentToken = jwtAuthService.generateToken("student", List.of(AccountConsts.ROLE_STUDENT), false);

        teacherHeaders = new Headers(new Header("Authorization", "Bearer " + teacherToken));
        studentHeaders = new Headers(new Header("Authorization", "Bearer " + studentToken));
        unauthenticatedHeaders = new Headers();

        TransactionTemplate transactionTemplate = new TransactionTemplate(mppTransactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Test
    @DisplayName("Powinno pomyślnie pobrać listę szablonów dla nauczyciela")
    public void shouldGetRulePresetsSuccessfully() {
        String responseBody = requestFunctions.sendGetRequest(
                200,
                "/api/rules/templates",
                teacherHeaders
        );

        List<String> raportLevelNames = JsonPath.from(responseBody).getList("raportLevelName", String.class);
        assertTrue(raportLevelNames.contains("ONLY_PERCENTAGES"));
    }

    @Test
    @DisplayName("Powinno zabronić pobrania listy szablonów dla studenta")
    public void shouldFailToGetRulePresetsWithStudentToken() {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");
        requestFunctions.sendGetRequest(
                403,
                "/api/rules/templates",
                studentHeaders
        );
    }

    @Test
    @DisplayName("Powinno zabronić pobrania listy szablonów dla studenta")
    public void shouldFailToGetRulePresetsWithoutToken() {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");
        requestFunctions.sendGetRequest(
                401,
                "/api/rules/templates",
                unauthenticatedHeaders
        );
    }
}