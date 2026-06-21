package pl.lodz.p.it.ssbd2026.ssbd01.mok.mok4;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.LanguageDict;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.EmailRestClient;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;

import static org.junit.jupiter.api.Assertions.*;
import static pl.lodz.p.it.ssbd2026.ssbd01.utils.consts.TestConsts.ACCOUNT_BLOCK;

public class NEG01_blockUserAccount extends BaseTestSetup {

    @Autowired
    private RequestFunctions requestFunctions;

    @Autowired
    private AccountRepository accountRepository;

    @MockitoBean
    private EmailRestClient emailRestClient;

    @Autowired
    private VersionHashService versionHashService;

    @Autowired
    @Qualifier("mokTransactionManager")
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    public void setup() {
        System.out.print("\n\n\n" +
                "Test NEG01_blockUserAccount\n" +
                "Check if blocking user account is working properly\"\n\n\n");
    }

    @Test
    @DisplayName("Check if blocking user account is working properly")
    public void NEG01_blockUserAccount() {
        TransactionTemplate transTemp = new TransactionTemplate(transactionManager);

        //TODO: na razie trzeba obsłużyć transakcję, potem do poprawienia
        Account testAccount = transTemp.execute(status -> {
            Account adminUser = accountRepository.findByLogin("admin").get();
            LanguageDict ld = adminUser.getLanguageVersion();

            Account account = new Account();
            account.setName("Tester");
            account.setSurname("Surname");
            account.setEmail("mok4@testerneg.com");
            account.setLogin("mok4testerneg");
            account.setIsBlocked(true);
            account.setLanguageVersion(ld);
            account.setPasswordHash("testcontainers_hash");

            return accountRepository.saveAndFlush(account);
        });

        String validHash = versionHashService.hashObjectVersion(testAccount.getId(), testAccount.getVersion());

        String responseBody = requestFunctions.sendPostRequest(
                "",
                409,
                ACCOUNT_BLOCK.getKeyName().replace("id", testAccount.getId().toString()),
                new Headers(new Header("If-Match", validHash))
        );

        transTemp.execute(status -> {
            Account accountToBlock = accountRepository.findById(testAccount.getId()).get();

            assertAll(
                    () -> assertTrue(accountToBlock.getIsBlocked(), "Assert that account is blocked"),
                    () -> assertEquals("Account is already blocked.", responseBody, "Assert that correct error message is returned")
                    //TODO: informacje o błędzie dodać do enuma np, żeby korzystać z odczytu ze zmiennych a nie tak manualnie
            );
            return null;
        });
    }
}