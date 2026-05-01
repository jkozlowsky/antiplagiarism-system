package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class POS08_ChangeOwnPasswordServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountService accountService;

    private Account account;
    private final String LOGIN = "passTestUser";
    private final String OLD_PASSWORD_PLAIN = "StareHaslo123!";
    private final String NEW_PASSWORD_PLAIN = "ZupelnieNoweHaslo321!";
    private final String OLD_PASSWORD_HASH = "old_hashed_password";
    private final String NEW_PASSWORD_HASH = "new_hashed_password";

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setLogin(LOGIN);
        account.setPasswordHash(OLD_PASSWORD_HASH);
        ReflectionTestUtils.setField(account, "version", 0);
    }

    @Test
    void shouldChangeOwnPasswordSuccessfully() {
        when(accountRepository.findByLogin(LOGIN)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(OLD_PASSWORD_PLAIN, OLD_PASSWORD_HASH)).thenReturn(true);
        when(passwordEncoder.matches(NEW_PASSWORD_PLAIN, OLD_PASSWORD_HASH)).thenReturn(false);
        when(passwordEncoder.encode(NEW_PASSWORD_PLAIN)).thenReturn(NEW_PASSWORD_HASH);

        assertDoesNotThrow(() -> accountService.changeOwnPassword(LOGIN, OLD_PASSWORD_PLAIN, NEW_PASSWORD_PLAIN, "0"));

        assertAll(
                () -> assertEquals(NEW_PASSWORD_HASH, account.getPasswordHash(), "Hash hasła w obiekcie powinien zostać zaktualizowany"),
                () -> verify(accountRepository, times(1)).saveAndFlush(account) // Sprawdza czy poleciał zapis do bazy
        );
    }
}