package pl.lodz.p.it.ssbd2026.ssbd01.mok.mok13.integration;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.junit.jupiter.api.Assertions.*;

public class POS01_shouldEditNameAndSurname extends BaseTestSetup {
    @Autowired
    RequestFunctions requestFunctions;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    VersionHashService versionHashService;

    @Autowired
    @Qualifier("mokTransactionManager")
    private PlatformTransactionManager transactionManager;

    Account testAccount;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String accountEmail;
    private String accountLogin;
    CommonFunctions commonFunctions;
    @BeforeEach
    void testSetup() {
        commonFunctions = new CommonFunctions();
        accountEmail = commonFunctions.generateRandomEmail();
        accountLogin = commonFunctions.generateRandomStringOfLength(8);

        TransactionTemplate transTemp = new TransactionTemplate(transactionManager);
        testAccount = transTemp.execute(status -> {
            Account adminUser = accountRepository.findByLogin("admin").get();
            LanguageDict ld = adminUser.getLanguageVersion();

            Account account = new Account();
            account.setName("Tester");
            account.setSurname("Surname");
            account.setEmail(accountEmail);
            account.setLogin(accountLogin);
            account.setIsBlocked(false);
            account.setLanguageVersion(ld);
            account.setPasswordHash("testcontainers_hash");

            return accountRepository.saveAndFlush(account);
        });
    }

    @Test
    void POS01_shouldEditNameAndSurname() throws JsonProcessingException {
        String changedName = commonFunctions.generateRandomStringOfLength(10);
        String changedSurname = commonFunctions.generateRandomStringOfLength(10);
        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                versionHashService.hashObjectVersion(testAccount.getId(), testAccount.getVersion()),
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
                "/api/account/" + testAccount.getId(),
                new Headers()
        );
        AccountWithAccessLevelsDTO result = objectMapper.readValue(response, AccountWithAccessLevelsDTO.class);
        assertAll(
                ()-> assertEquals(changedName, result.account().name()),
                ()-> assertEquals(changedSurname, result.account().surname())
        );
    }
}
