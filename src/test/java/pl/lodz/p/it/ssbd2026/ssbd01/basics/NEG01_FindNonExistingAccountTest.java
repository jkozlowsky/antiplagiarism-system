package pl.lodz.p.it.ssbd2026.ssbd01.basics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.AccountRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class NEG01_FindNonExistingAccountTest extends BaseTestSetup {

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    public void setup() {
        System.out.print("\n\n\n" +
                "Test NEG01_FindNonExistingAccountTest\n" +
                "Find non-existing account test\n\n\n");
    }

    @Test
    @DisplayName("Verify that finding a non-existing user returns an empty Optional")
    public void NEG01_FindNonExistingAccountTest() {

        Optional<Account> missingAccount = accountRepository.findByLogin("some_fake_login_123");

        assertTrue(missingAccount.isEmpty(), "System powinien zwrócić pusty wynik dla nieistniejącego loginu");
    }
}