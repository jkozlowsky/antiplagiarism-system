package pl.lodz.p.it.ssbd2026.ssbd01.mok.mok9.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.ChangeOtherPasswordDto;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;

import java.util.UUID;

public class NEG03_UserNotFoundTest extends BaseTestSetup {

    @Autowired
    private RequestFunctions requestFunctions;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void NEG04_shouldFailWhenUserDoesNotExist() throws Exception {
        ChangeOtherPasswordDto dto = new ChangeOtherPasswordDto();
        dto.setNewPassword("NoweWymuszoneHaslo123!");

        UUID nonExistentId = UUID.randomUUID();

        requestFunctions.sendPostRequest(
                objectMapper.writeValueAsString(dto),
                404,
                "/api/account/" + nonExistentId + "/password",
                new Headers(new Header("If-Match", "dowolny-hash"))
        );
    }
}