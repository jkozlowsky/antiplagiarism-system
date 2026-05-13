package pl.lodz.p.it.ssbd2026.ssbd01.mok.mok8.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.ChangeOwnPasswordDto;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;

public class NEG01_OptimisticLockTest extends BaseTestSetup {

    @Autowired
    RequestFunctions requestFunctions;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void NEG01_shouldFailDueToOptimisticLock() throws JsonProcessingException {
        ChangeOwnPasswordDto dto = new ChangeOwnPasswordDto();
        dto.setOldPassword("AdminPassword123!");
        dto.setNewPassword("NewAdminPassword123!");

        requestFunctions.sendPostRequest(
                objectMapper.writeValueAsString(dto),
                409,
                "/api/account/security/password",
                new Headers(new Header("If-Match", "nieaktualny-hash-wersji-12345"))
        );
    }
}