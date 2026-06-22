package pl.lodz.p.it.ssbd2026.ssbd01.mok.mok8.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AccountDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.ChangeOwnPasswordDto;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.AccountTestRepository;

public class POS01_ChangeOwnPasswordTest extends BaseTestSetup {

    @Autowired
    private RequestFunctions requestFunctions;

    @Autowired
    private AccountTestRepository accountTestRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    void POS01_shouldChangeOwnPassword() throws Exception {
        Account adminAccount = accountTestRepository.findByLogin("admin").orElseThrow();
        String originalHash = adminAccount.getPasswordHash();

        String temporaryKnownPassword = "TemporaryPassword123!";
        adminAccount.setPasswordHash(passwordEncoder.encode(temporaryKnownPassword));
        accountTestRepository.save(adminAccount);

        String value1 = requestFunctions.sendGetRequest(
                200,
                "/api/account/login/no-access-level/admin",
                new Headers()
        );
        AccountDTO getAccount = objectMapper.readValue(value1, AccountDTO.class);

        ChangeOwnPasswordDto dto = new ChangeOwnPasswordDto();
        dto.setOldPassword(temporaryKnownPassword);
        dto.setNewPassword("NewAdminPassword123!");

        requestFunctions.sendPostRequest(
                objectMapper.writeValueAsString(dto),
                204,
                "/api/account/security/password",
                new Headers(new Header("If-Match", getAccount.versionHash()))
        );

        Account adminAfterTest = accountTestRepository.findByLogin("admin").orElseThrow();
        adminAfterTest.setPasswordHash(originalHash);
        accountTestRepository.save(adminAfterTest);
    }
}