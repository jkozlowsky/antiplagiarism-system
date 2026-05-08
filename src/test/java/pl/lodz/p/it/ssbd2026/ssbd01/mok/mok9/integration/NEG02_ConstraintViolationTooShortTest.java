package pl.lodz.p.it.ssbd2026.ssbd01.mok.mok9.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AccountDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.ChangeOtherPasswordDto;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;

import java.util.UUID;

public class NEG02_ConstraintViolationTooShortTest extends BaseTestSetup {

    @Autowired
    private RequestFunctions requestFunctions;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void NEG02_shouldFailDueToTooShortPassword() throws Exception {
        String value1 = requestFunctions.sendGetRequest(
                200,
                "/api/account/login/no-access-level/anowak",
                new Headers()
        );
        AccountDTO getAccount = objectMapper.readValue(value1, AccountDTO.class);
        UUID targetId = getAccount.id();

        ChangeOtherPasswordDto dto = new ChangeOtherPasswordDto();
        dto.setNewPassword("Short1!");

        requestFunctions.sendPostRequest(
                objectMapper.writeValueAsString(dto),
                400,
                "/api/account/" + targetId + "/password",
                new Headers(new Header("If-Match", getAccount.versionHash()))
        );
    }
}