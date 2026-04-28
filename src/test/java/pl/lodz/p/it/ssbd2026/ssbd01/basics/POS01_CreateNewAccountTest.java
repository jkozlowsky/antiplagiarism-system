package pl.lodz.p.it.ssbd2026.ssbd01.basics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.LanguageDict;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.AccountTestRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class POS01_CreateNewAccountTest extends BaseTestSetup {

    @Autowired
    private AccountTestRepository accountTestRepository;

    @BeforeEach
    public void setup() {
        System.out.print("\n\n\n" +
                "Test POS01_CreateNewAccountTest\n" +
                "Account creation test\n\n\n");
    }

    @Test
    @DisplayName("Basic test to verify if database works - create new user")
    public void POS01_CreateNewAccountTest() {

        Account adminUser = accountTestRepository.findByLogin("admin").get();
        LanguageDict ld = adminUser.getLanguageVersion();

        Account newAccount = new Account();
        newAccount.setLogin("testNewUser");
        newAccount.setEmail("new@user.pl");
        newAccount.setPasswordHash("testcontainers_hash");
        newAccount.setName("Andrzej");
        newAccount.setSurname("Wazon");
        newAccount.setIsBlocked(false);
        newAccount.setLanguageVersion(ld);

        ReflectionTestUtils.setField(newAccount, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(newAccount, "version", 0);

        UUID uuidHolder = (accountTestRepository.save(newAccount)).getId();

        assertAll(
                () -> assertNotNull(accountTestRepository.findByLogin("testNewUser")),
                () -> assertNotNull(accountTestRepository.findByEmail("new@user.pl")),
                () -> assertEquals(uuidHolder, accountTestRepository.findByLogin("testNewUser").get().getId())
        );
    }
}