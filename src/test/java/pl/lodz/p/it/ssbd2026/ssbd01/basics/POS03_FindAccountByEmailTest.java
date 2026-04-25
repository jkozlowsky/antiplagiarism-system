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

public class POS03_FindAccountByEmailTest extends BaseTestSetup {

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    public void setup() {
        System.out.print("\n\n\n" +
                "Test POS03_FindAccountByEmailTest\n" +
                "Find account by email test\n\n\n");
    }

    @Test
    @DisplayName("Verify if an existing account can be found using its email address")
    public void POS03_FindAccountByEmailTest() {

        Account adminUser = accountRepository.findByLogin("admin").get();
        LanguageDict ld = adminUser.getLanguageVersion();

        Account newAccount = new Account();
        newAccount.setLogin("emailTestUser");
        newAccount.setEmail("search_me@user.pl");
        newAccount.setPasswordHash("testcontainers_hash");
        newAccount.setName("Tomasz");
        newAccount.setSurname("Klucz");
        newAccount.setIsBlocked(false);
        newAccount.setLanguageVersion(ld);

        ReflectionTestUtils.setField(newAccount, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(newAccount, "version", 0);

        accountRepository.save(newAccount);

        var foundAccount = accountRepository.findByEmail("search_me@user.pl");

        assertAll(
                () -> assertTrue(foundAccount.isPresent(), "Konto powinno zostać znalezione po adresie email"),
                () -> assertEquals("emailTestUser", foundAccount.get().getLogin(), "Login powinien odpowiadać temu przypisanemu do maila")
        );
    }
}