package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.Headers;
import io.restassured.specification.FilterableRequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.CreateRulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;

import java.util.List;
import java.util.UUID;

public class CreateRulePresetControllerTest extends BaseTestSetup {

    @Autowired
    private JwtAuthService jwtAuthService;

    @Autowired
    private RequestFunctions requestFunctions;

    private Headers teacherHeaders;
    private Headers studentHeaders;
    private Headers emptyHeaders;

    @BeforeEach
    public void setupData() {
        String mockTeacherUuid = UUID.randomUUID().toString();
        String teacherToken = jwtAuthService.generateToken(mockTeacherUuid, List.of(AccountConsts.ROLE_TEACHER), false);
        teacherHeaders = new Headers(new io.restassured.http.Header("Authorization", "Bearer " + teacherToken));

        String mockStudentUuid = UUID.randomUUID().toString();
        String studentToken = jwtAuthService.generateToken(mockStudentUuid, List.of(AccountConsts.ROLE_STUDENT), false);
        studentHeaders = new Headers(new io.restassured.http.Header("Authorization", "Bearer " + studentToken));

        emptyHeaders = new Headers();
    }

    @Test
    @DisplayName("Powinno pomyślnie utworzyć predefiniowany szablon reguł przy poprawnych parametrach")
    public void shouldCreateRulePresetTemplateSuccessfully() throws Exception {
        CreateRulePresetDTO dto = new CreateRulePresetDTO(15, "FULL_VIEW", 20, true);
        String jsonBody = new ObjectMapper().writeValueAsString(dto);

        requestFunctions.sendPostRequest(
                jsonBody,
                200,
                "/api/rules/templates",
                teacherHeaders
        );
    }

    @Test
    @DisplayName("Powinno pomyślnie utworzyć szablon z poprawnymi wartościami brzegowymi")
    public void shouldCreateRulePresetTemplateWithBoundaryValues() throws Exception {
        CreateRulePresetDTO dto = new CreateRulePresetDTO(0, "FULL_VIEW", 1, false);
        String jsonBody = new ObjectMapper().writeValueAsString(dto);

        requestFunctions.sendPostRequest(
                jsonBody,
                200,
                "/api/rules/templates",
                teacherHeaders
        );
    }

    @Test
    @DisplayName("Powinno zwrócić kod 404, gdy wskazany poziom raportowania nie istnieje w systemie")
    public void shouldReturn404WhenRaportLevelNotFound() throws Exception {
        CreateRulePresetDTO dto = new CreateRulePresetDTO(15, "NON_EXISTENT_LEVEL_DICT", 20, true);
        String jsonBody = new ObjectMapper().writeValueAsString(dto);

        requestFunctions.sendPostRequest(
                jsonBody,
                404,
                "/api/rules/templates",
                teacherHeaders
        );
    }

    @Test
    @DisplayName("Powinno zwrócić kod 400, gdy obiekt DTO jest całkowicie pusty")
    public void shouldReturn400WhenAllParametersAreMissing() throws Exception {
        CreateRulePresetDTO dto = new CreateRulePresetDTO(null, null, null, null);
        String jsonBody = new ObjectMapper().writeValueAsString(dto);

        requestFunctions.sendPostRequest(
                jsonBody,
                400,
                "/api/rules/templates",
                teacherHeaders
        );
    }

    @Test
    @DisplayName("Powinno zwrócić kod 400, gdy minimumTokensMatch ma niedozwoloną wartość poniżej zera")
    public void shouldReturn400WhenMinimumTokensMatchIsNegative() throws Exception {
        CreateRulePresetDTO dto = new CreateRulePresetDTO(15, "FULL_VIEW", -5, true);
        String jsonBody = new ObjectMapper().writeValueAsString(dto);

        requestFunctions.sendPostRequest(
                jsonBody,
                400,
                "/api/rules/templates",
                teacherHeaders
        );
    }

    @Test
    @DisplayName("Powinno zwrócić kod 400, gdy minimumTokensMatch wynosi 0 (wymagane min. 1)")
    public void shouldReturn400WhenMinimumTokensMatchIsZero() throws Exception {
        CreateRulePresetDTO dto = new CreateRulePresetDTO(15, "FULL_VIEW", 0, true);
        String jsonBody = new ObjectMapper().writeValueAsString(dto);

        requestFunctions.sendPostRequest(
                jsonBody,
                400,
                "/api/rules/templates",
                teacherHeaders
        );
    }

    @Test
    @DisplayName("Powinno zwrócić kod 400, gdy studentTicketCount ma niedozwoloną wartość poniżej zera")
    public void shouldReturn400WhenStudentTicketCountIsNegative() throws Exception {
        CreateRulePresetDTO dto = new CreateRulePresetDTO(-1, "FULL_VIEW", 20, true);
        String jsonBody = new ObjectMapper().writeValueAsString(dto);

        requestFunctions.sendPostRequest(
                jsonBody,
                400,
                "/api/rules/templates",
                teacherHeaders
        );
    }

    @Test
    @DisplayName("Powinno zwrócić kod 403, gdy zalogowany użytkownik posiada rolę ucznia zamiast nauczyciela")
    public void shouldReturn403WhenUserIsStudent() throws Exception {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");

        CreateRulePresetDTO dto = new CreateRulePresetDTO(15, "FULL_VIEW", 20, true);
        String jsonBody = new ObjectMapper().writeValueAsString(dto);

        requestFunctions.sendPostRequest(
                jsonBody,
                403,
                "/api/rules/templates",
                studentHeaders
        );
    }

    @Test
    @DisplayName("Powinno zwrócić kod 401, gdy zapytanie nie posiada nagłówka autoryzacji")
    public void shouldReturn401WhenNoAuthorizationHeaderProvided() throws Exception {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");

        CreateRulePresetDTO dto = new CreateRulePresetDTO(15, "FULL_VIEW", 20, true);
        String jsonBody = new ObjectMapper().writeValueAsString(dto);

        requestFunctions.sendPostRequest(
                jsonBody,
                401,
                "/api/rules/templates",
                emptyHeaders
        );
    }
}