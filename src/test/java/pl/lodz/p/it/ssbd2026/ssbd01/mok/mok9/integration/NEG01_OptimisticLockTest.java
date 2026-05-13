package pl.lodz.p.it.ssbd2026.ssbd01.mok.mok9.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AccountDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.ChangeOtherPasswordDto;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;

import java.util.UUID;

public class NEG01_OptimisticLockTest extends BaseTestSetup {

    @Autowired
    private RequestFunctions requestFunctions;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
//    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void NEG01_shouldFailDueToOptimisticLock() throws Exception {
        String value1 = requestFunctions.sendGetRequest(
                200,
                "/api/account/login/no-access-level/anowak",
                new Headers()
        );
        AccountDTO getAccount = objectMapper.readValue(value1, AccountDTO.class);
        String targetLoginn = getAccount.login();

        ChangeOtherPasswordDto dto = new ChangeOtherPasswordDto();
        dto.setNewPassword("NoweWymuszoneHaslo123!");

        requestFunctions.sendPostRequest(
                objectMapper.writeValueAsString(dto),
                409,
                "/api/account/" + targetLoginn + "/password",
                new Headers(new Header("If-Match", "niepoprawny-stary-hash-wersji-12345"))
        );
    }
}