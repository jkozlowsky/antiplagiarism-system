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
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccountNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.InvalidOldPasswordException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.PasswordSameAsOldException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NEG08_ChangeOwnPasswordServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountService accountService;

    private Account account;
    private final String LOGIN = "negTestLogin";
    private final String OLD_PASSWORD_PLAIN = "PoprawneStareHaslo123!";
    private final String NEW_PASSWORD_PLAIN = "NoweHaslo321!";
    private final String HASHED_PASSWORD = "hashed_password_mock";
    private final String CURRENT_VERSION = "0";

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setLogin(LOGIN);
        account.setPasswordHash(HASHED_PASSWORD);
        ReflectionTestUtils.setField(account, "version", 0);
    }

    @Test
    void shouldThrowAccountNotFoundException() {
        when(accountRepository.findByLogin("nonExistingUser")).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () ->
                accountService.changeOwnPassword("nonExistingUser", OLD_PASSWORD_PLAIN, NEW_PASSWORD_PLAIN, CURRENT_VERSION));
    }

    @Test
    void shouldThrowConcurrentUpdateException() {
        when(accountRepository.findByLogin(LOGIN)).thenReturn(Optional.of(account));

        assertThrows(ConcurrentUpdateException.class, () ->
                accountService.changeOwnPassword(LOGIN, OLD_PASSWORD_PLAIN, NEW_PASSWORD_PLAIN, "999"));
    }

    @Test
    void shouldThrowInvalidOldPasswordException() {
        when(accountRepository.findByLogin(LOGIN)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("BledneHaslo123!", HASHED_PASSWORD)).thenReturn(false);

        assertThrows(InvalidOldPasswordException.class, () ->
                accountService.changeOwnPassword(LOGIN, "BledneHaslo123!", NEW_PASSWORD_PLAIN, CURRENT_VERSION));
    }

    @Test
    void shouldThrowPasswordSameAsOldException() {
        when(accountRepository.findByLogin(LOGIN)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(OLD_PASSWORD_PLAIN, HASHED_PASSWORD)).thenReturn(true);
        when(passwordEncoder.matches(NEW_PASSWORD_PLAIN, HASHED_PASSWORD)).thenReturn(true);

        assertThrows(PasswordSameAsOldException.class, () ->
                accountService.changeOwnPassword(LOGIN, OLD_PASSWORD_PLAIN, NEW_PASSWORD_PLAIN, CURRENT_VERSION));
    }
}