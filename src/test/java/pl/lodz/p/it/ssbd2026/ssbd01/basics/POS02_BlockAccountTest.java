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

import static org.junit.jupiter.api.Assertions.*;

public class POS02_BlockAccountTest extends BaseTestSetup {

    @Autowired
    private AccountTestRepository accountTestRepository;

    @BeforeEach
    public void setup() {
        System.out.print("\n\n\n" +
                "Test POS02_BlockAccountTest\n" +
                "Account blocking test\n\n\n");
    }

    @Test
    @DisplayName("Verify if existing account can be successfully blocked")
    public void POS02_BlockAccountTest() {
        Account adminUser = accountTestRepository.findByLogin("admin").get();
        LanguageDict ld = adminUser.getLanguageVersion();

        Account newAccount = new Account();
        newAccount.setLogin("userToBlock");
        newAccount.setEmail("block@user.pl");
        newAccount.setPasswordHash("testcontainers_hash");
        newAccount.setName("Jan");
        newAccount.setSurname("Kowalski");
        newAccount.setIsBlocked(false);
        newAccount.setLanguageVersion(ld);

        ReflectionTestUtils.setField(newAccount, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(newAccount, "version", 0);

        accountTestRepository.save(newAccount);

        Account accountToBlock = accountTestRepository.findByLogin("userToBlock").get();
        accountToBlock.setIsBlocked(true);
        accountTestRepository.save(accountToBlock);

        Account blockedAccount = accountTestRepository.findByLogin("userToBlock").get();
        assertAll(
                () -> assertNotNull(blockedAccount),
                () -> assertTrue(blockedAccount.getIsBlocked(), "Konto powinno mieć flagę isBlocked = true")
        );
    }
}