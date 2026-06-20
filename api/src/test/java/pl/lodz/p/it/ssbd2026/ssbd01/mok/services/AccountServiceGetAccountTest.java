package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.AccessLevelDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Admin;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccountNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceGetAccountTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private Account account;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        account = new Account();
        account.setLogin("testUser");
        ReflectionTestUtils.setField(account, "id", accountId);

        AccessLevelDict dict = new AccessLevelDict();
        dict.setAccessLevelName(AccountConsts.ROLE_ADMIN);

        Admin adminRole = new Admin();
        adminRole.setAccessLevelDict(dict);
        adminRole.setActive(true);

        account.setAccessLevelList(new ArrayList<>());
        account.getAccessLevelList().add(adminRole);
    }

    @Test
    @DisplayName("Pomyślne pobranie istniejącego konta po UUID wraz z wymuszeniem pobrania ról")
    void shouldGetAccountSuccessfully() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        Account fetchedAccount = accountService.getAccountWithAccessLevels(accountId);

        assertAll(
                () -> assertNotNull(fetchedAccount),
                () -> assertEquals(accountId, fetchedAccount.getId()),
                () -> assertEquals("testUser", fetchedAccount.getLogin()),
                () -> assertFalse(fetchedAccount.getAccessLevelList().isEmpty()),
                () -> verify(accountRepository, times(1)).findById(accountId)
        );

    }

    @Test
    @DisplayName("Wyrzucenie błędu AccountNotFoundException, gdy poszukiwane konto nie istnieje")
    void shouldThrowAccountNotFoundExceptionWhenAccountDoesNotExist() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertAll(
                () -> assertThrows(AccountNotFoundException.class, () -> accountService.getAccountWithAccessLevels(accountId)),
                () -> verify(accountRepository, times(1)).findById(accountId)
        );
    }
}