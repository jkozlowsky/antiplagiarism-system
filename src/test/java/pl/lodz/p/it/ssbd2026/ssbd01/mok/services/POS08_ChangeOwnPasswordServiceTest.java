package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.LanguageDict;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.AccountTestRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class POS08_ChangeOwnPasswordServiceTest extends BaseTestSetup {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountTestRepository accountTestRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() {
        System.out.print("\n\n\nTest POS08_ChangeOwnPasswordServiceTest\nAccount change password service test\n\n\n");
    }

    @Test
    @DisplayName("Verify if an account's password can be successfully changed via Service")
    public void POS08_ChangeOwnPasswordServiceTest() {

        Account adminUser = accountTestRepository.findByLogin("admin").get();
        LanguageDict ld = adminUser.getLanguageVersion();

        String oldPasswordPlain = "StareHaslo123!";
        String newPasswordPlain = "ZupelnieNoweHaslo321!";

        Account testAccount = new Account();
        testAccount.setLogin("passTestUser");
        testAccount.setEmail("pass_test@user.pl");
        testAccount.setPasswordHash(passwordEncoder.encode(oldPasswordPlain));
        testAccount.setName("Jan");
        testAccount.setSurname("Haslo");
        testAccount.setIsBlocked(false);
        testAccount.setLanguageVersion(ld);

        ReflectionTestUtils.setField(testAccount, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(testAccount, "version", 0);

        accountTestRepository.save(testAccount);

        String currentVersion = String.valueOf(testAccount.getVersion());

        assertDoesNotThrow(() -> {
            accountService.changeOwnPassword(
                    "passTestUser",
                    oldPasswordPlain,
                    newPasswordPlain,
                    currentVersion
            );
        });

        Account updatedAccount = accountTestRepository.findByLogin("passTestUser").get();

        assertAll("Sprawdzenie zmiany hasła",
                () -> assertNotNull(updatedAccount),
                () -> assertTrue(passwordEncoder.matches(newPasswordPlain, updatedAccount.getPasswordHash()), "Nowe hasło powinno pasować do nowego hasha w bazie"),
                () -> assertFalse(passwordEncoder.matches(oldPasswordPlain, updatedAccount.getPasswordHash()), "Stare hasło nie powinno już działać")
        );
    }
}