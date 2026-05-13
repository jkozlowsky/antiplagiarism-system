package pl.lodz.p.it.ssbd2026.ssbd01.mok.mok12.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.CommonFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;

public class NEG02_ConstraintViolationTest extends BaseTestSetup {
    @Autowired
    RequestFunctions requestFunctions;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    @Qualifier("mokTransactionManager")
    private PlatformTransactionManager transactionManager;

    Account testAccount;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());    CommonFunctions commonFunctions;
    @BeforeEach
    void testSetup() {
        commonFunctions = new CommonFunctions();
    }


    @Test
    void NEG02_ConstraintViolationTest() throws JsonProcessingException {
        String changedName = commonFunctions.generateRandomStringOfLength(2);
        String changedSurname = commonFunctions.generateRandomStringOfLength(2);
        String value1 = requestFunctions.sendGetRequest(
                200,
                "/api/account/login/no-access-level/admin",
                new Headers()
        );
        AccountDTO getAccount = objectMapper.readValue(value1, AccountDTO.class);

        AccountDTO accountDTO = new AccountDTO(
                null,
                getAccount.versionHash(),
                null,
                null,
                null,
                null,
                changedName,
                changedSurname,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        String response = requestFunctions.sendPatchRequest(
                objectMapper.writeValueAsString(accountDTO),
                400,
                "/api/account/update-myself",
                new Headers()
        );
    }
}
