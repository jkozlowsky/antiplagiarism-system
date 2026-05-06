package pl.lodz.p.it.ssbd2026.ssbd01.mok.mok5;

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
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.EmailService;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;

import static org.junit.jupiter.api.Assertions.*;
import static pl.lodz.p.it.ssbd2026.ssbd01.utils.consts.TestConsts.ACCOUNT_UNBLOCK;

public class NEG01_unblockUserAccount extends BaseTestSetup {
    @Autowired
    private RequestFunctions requestFunctions;

    @Autowired
    private AccountRepository accountRepository;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private VersionHashService versionHashService;

    @Autowired
    @Qualifier("mokTransactionManager")
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    public void setup() {
        System.out.print("\n\n\n" +
                "Test NEG01_unblockUserAccount\n" +
                "Check if unblocking user account is working properly\"\n\n\n");
    }

    @Test
    @DisplayName("Check if unblocking user account is working properly")
    public void NEG01_unblockUserAccount() {
        TransactionTemplate transTemp = new TransactionTemplate(transactionManager);

        //TODO: na razie trzeba obsłużyć transakcję, potem do poprawienia
        Account testAccount = transTemp.execute(status -> {
            Account adminUser = accountRepository.findByLogin("admin").get();
            LanguageDict ld = adminUser.getLanguageVersion();

            Account account = new Account();
            account.setName("Tester");
            account.setSurname("Surname");
            account.setEmail("mok5@testerneg.com");
            account.setLogin("mok5testerneg");
            account.setIsBlocked(false);
            account.setLanguageVersion(ld);
            account.setPasswordHash("testcontainers_hash");

            return accountRepository.saveAndFlush(account);
        });

        String validHash = versionHashService.hashObjectVersion(testAccount.getId(), testAccount.getVersion());

        String responseBody = requestFunctions.sendPostRequest(
                "",
                409,
                ACCOUNT_UNBLOCK.getKeyName().replace("id", testAccount.getId().toString()),
                new Headers(new Header("If-Match", validHash))
        );

        transTemp.execute(status -> {
            Account accountToUnblock = accountRepository.findById(testAccount.getId()).get();

            assertAll(
                    () -> assertFalse(accountToUnblock.getIsBlocked(), "Assert that account is unblocked"),
                    () -> assertEquals("Account is not blocked", responseBody, "Assert that correct error message is returned")
                    //TODO: informacje o błędzie dodać do enuma np, żeby korzystać z odczytu ze zmiennych a nie tak manualnie
            );
            return null;
        });
    }
}