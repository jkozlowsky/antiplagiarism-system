package pl.lodz.p.it.ssbd2026.ssbd01.mok.mok8.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AccountDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.ChangeOwnPasswordDto;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;

public class NEG03_ConstraintViolationTest extends BaseTestSetup {

    @Autowired
    RequestFunctions requestFunctions;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    @Test
    void NEG03_shouldFailDueToTooShortPassword() throws JsonProcessingException {
        String value1 = requestFunctions.sendGetRequest(
                200,
                "/api/account/login/no-access-level/admin",
                new Headers()
        );
        AccountDTO getAccount = objectMapper.readValue(value1, AccountDTO.class);

        ChangeOwnPasswordDto dto = new ChangeOwnPasswordDto();
        dto.setOldPassword("AdminPassword123!");
        dto.setNewPassword("Short1!");

        requestFunctions.sendPostRequest(
                objectMapper.writeValueAsString(dto),
                400,
                "/api/account/security/password",
                new Headers(new Header("If-Match", getAccount.versionHash()))
        );
    }
}