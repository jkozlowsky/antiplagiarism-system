package pl.lodz.p.it.ssbd2026.ssbd01.basics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.LanguageDict;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.AccountRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class POS04_UpdateAccountDataTest extends BaseTestSetup {

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    public void setup() {
        System.out.print("\n\n\n" +
                "Test POS04_UpdateAccountDataTest\n" +
                "Account data update test\n\n\n");
    }

    @Test
    @DisplayName("Verify if an account's personal data can be successfully updated")
    public void POS04_UpdateAccountDataTest() {

        Account adminUser = accountRepository.findByLogin("admin").get();
        LanguageDict ld = adminUser.getLanguageVersion();

        Account newAccount = new Account();
        newAccount.setLogin("updateTestUser");
        newAccount.setEmail("old_email@user.pl");
        newAccount.setPasswordHash("testcontainers_hash");
        newAccount.setName("Piotr");
        newAccount.setSurname("Stary");
        newAccount.setIsBlocked(false);
        newAccount.setLanguageVersion(ld);

        ReflectionTestUtils.setField(newAccount, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(newAccount, "version", 0);

        accountRepository.save(newAccount);

        Account accountToUpdate = accountRepository.findByLogin("updateTestUser").get();
        accountToUpdate.setEmail("brand_new_email@user.pl");
        accountToUpdate.setSurname("Nowy");
        accountRepository.save(accountToUpdate);

        Account updatedAccount = accountRepository.findByLogin("updateTestUser").get();
        assertAll(
                () -> assertEquals("brand_new_email@user.pl", updatedAccount.getEmail(), "Adres email powinien zostać zaktualizowany"),
                () -> assertEquals("Nowy", updatedAccount.getSurname(), "Nazwisko powinno zostać zaktualizowane")
        );
    }
}