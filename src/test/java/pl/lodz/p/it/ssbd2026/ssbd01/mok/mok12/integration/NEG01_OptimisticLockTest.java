package pl.lodz.p.it.ssbd2026.ssbd01.mok.mok12.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AccountDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AccountWithAccessLevelsDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.LanguageDict;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.CommonFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;

public class NEG01_OptimisticLockTest extends BaseTestSetup {
    @Autowired
    RequestFunctions requestFunctions;

    private final ObjectMapper objectMapper = new ObjectMapper();

    CommonFunctions commonFunctions;
    @BeforeEach
    void testSetup() {
        commonFunctions = new CommonFunctions();
    }

    @Test
    void NEG01_OptimisticLockTest() throws JsonProcessingException {
        String changedName = commonFunctions.generateRandomStringOfLength(10);
        String changedSurname = commonFunctions.generateRandomStringOfLength(10);
        String value1 = requestFunctions.sendGetRequest(
                200,
                "/api/account/login/no-access-level/admin",
                new Headers()
        );
        AccountDTO getAccount = objectMapper.readValue(value1, AccountDTO.class);

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                changedName,
                changedSurname,
                null,
                null
        );
        String response = requestFunctions.sendPatchRequest(
                objectMapper.writeValueAsString(accountDTO),
                200,
                "/api/account/update-myself",
                new Headers(new Header("if-Match",getAccount.versionHash()))
        );

        String response2 = requestFunctions.sendPatchRequest(
                objectMapper.writeValueAsString(accountDTO),
                409,
                "/api/account/update-myself",
                new Headers(new Header("if-Match",getAccount.versionHash()))
        );
    }
}
