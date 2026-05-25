package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.specification.FilterableRequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.UpdateRulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RaportLevelDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RaportLevelRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RuleRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UpdateRulePresetTest extends BaseTestSetup {

    @Autowired
    private JwtAuthService jwtAuthService;

    @Autowired
    private RequestFunctions requestFunctions;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private RaportLevelRepository raportLevelRepository;

    @Autowired
    private PlatformTransactionManager mppTransactionManager;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private Headers teacherHeaders;
    private Headers studentHeaders;
    private Headers unauthenticatedHeaders;

    private UUID testRuleId;
    private final String targetRaportLevelName = "FULL_VIEW";

    @BeforeEach
    public void setupDataAndAuth() {
        String teacherToken = jwtAuthService.generateToken("teacher", List.of(AccountConsts.ROLE_TEACHER), false);
        String studentToken = jwtAuthService.generateToken("student", List.of(AccountConsts.ROLE_STUDENT), false);

        teacherHeaders = new Headers(new Header("Authorization", "Bearer " + teacherToken));
        studentHeaders = new Headers(new Header("Authorization", "Bearer " + studentToken));
        unauthenticatedHeaders = new Headers();

        TransactionTemplate transactionTemplate = new TransactionTemplate(mppTransactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        transactionTemplate.execute(status -> {
            RaportLevelDict existingLevel = raportLevelRepository.findByName(targetRaportLevelName)
                    .orElseThrow(() -> new IllegalStateException("Brak domyślnego poziomu raportu FULL_VIEW w słowniku bazy testowej!"));

            RulePreset rulePreset = new RulePreset();
            rulePreset.setRaportLevel(existingLevel);
            rulePreset.setStudentTicketCount(5);
            rulePreset.setMinimumTokensMatch(10);
            rulePreset.setEnableNormalization(false);
            rulePreset.setIsPreset(true);

            RulePreset savedRule = ruleRepository.save(rulePreset);
            testRuleId = savedRule.getId();

            return null;
        });
    }

    @Test
    @DisplayName("Powinno pomyślnie zaktualizować regułę przez nauczyciela")
    public void shouldUpdateRulePresetSuccessfully() throws Exception {
        UpdateRulePresetDTO dto = new UpdateRulePresetDTO(targetRaportLevelName, 20, 50, true);

        requestFunctions.sendPutRequest(
                objectMapper.writeValueAsString(dto),
                200,
                "/api/rules/" + testRuleId,
                teacherHeaders
        );

        TransactionTemplate transactionTemplate = new TransactionTemplate(mppTransactionManager);
        transactionTemplate.execute(status -> {
            RulePreset updatedRule = ruleRepository.findById(testRuleId).orElseThrow();
            assertEquals(20, updatedRule.getStudentTicketCount());
            assertEquals(50, updatedRule.getMinimumTokensMatch());
            assertTrue(updatedRule.getEnableNormalization());
            assertEquals(targetRaportLevelName, updatedRule.getRaportLevel().getName());
            return null;
        });
    }

    @Test
    @DisplayName("Powinno zwrócić błąd 400 z powodu walidacji danych (ujemne bilety)")
    public void shouldFailToUpdateWithInvalidData() throws Exception {
        UpdateRulePresetDTO dto = new UpdateRulePresetDTO(targetRaportLevelName, -5, 50, true);

        requestFunctions.sendPutRequest(
                objectMapper.writeValueAsString(dto),
                400,
                "/api/rules/" + testRuleId,
                teacherHeaders
        );
    }

    @Test
    @DisplayName("Powinno zabronić aktualizacji reguły dla studenta")
    public void shouldFailToUpdateRulePresetWithStudentToken() throws Exception {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");

        UpdateRulePresetDTO dto = new UpdateRulePresetDTO(targetRaportLevelName, 20, 50, true);

        requestFunctions.sendPutRequest(
                objectMapper.writeValueAsString(dto),
                403,
                "/api/rules/" + testRuleId,
                studentHeaders
        );
    }

    @Test
    @DisplayName("Powinno zabronić aktualizacji reguły bez tokena")
    public void shouldFailToUpdateRulePresetWithoutToken() throws Exception {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");

        UpdateRulePresetDTO dto = new UpdateRulePresetDTO(targetRaportLevelName, 20, 50, true);

        requestFunctions.sendPutRequest(
                objectMapper.writeValueAsString(dto),
                401,
                "/api/rules/" + testRuleId,
                unauthenticatedHeaders
        );
    }

    @Test
    @DisplayName("Powinno zwrócić błąd 404, jeśli reguła nie istnieje")
    public void shouldReturn404WhenRuleDoesNotExist() throws Exception {
        UUID randomId = UUID.randomUUID();
        UpdateRulePresetDTO dto = new UpdateRulePresetDTO(targetRaportLevelName, 20, 50, true);

        requestFunctions.sendPutRequest(
                objectMapper.writeValueAsString(dto),
                404,
                "/api/rules/" + randomId,
                teacherHeaders
        );
    }

    @Test
    @DisplayName("Powinno zwrócić błąd 404, jeśli podany raport_level nie istnieje w słowniku")
    public void shouldReturn404WhenRaportLevelDictDoesNotExist() throws Exception {
        UpdateRulePresetDTO dto = new UpdateRulePresetDTO("NON_EXISTENT_LEVEL", 20, 50, true);

        requestFunctions.sendPutRequest(
                objectMapper.writeValueAsString(dto),
                404,
                "/api/rules/" + testRuleId,
                teacherHeaders
        );
    }
}