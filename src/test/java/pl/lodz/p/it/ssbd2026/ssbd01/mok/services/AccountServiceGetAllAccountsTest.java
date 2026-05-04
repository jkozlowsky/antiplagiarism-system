package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceGetAllAccountsTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private List<Account> mockAccounts;
    private List<Account> filteredAccounts;

    @BeforeEach
    void setUp() {
        mockAccounts = new ArrayList<>();
        Account acc1 = new Account();
        acc1.setLogin("user1");
        acc1.setEmail("user1@example.com");
        Account acc2 = new Account();
        acc2.setLogin("user2");
        acc2.setEmail("user2@example.com");
        mockAccounts.add(acc1);
        mockAccounts.add(acc2);

        filteredAccounts = new ArrayList<>();
        filteredAccounts.add(acc1);
    }

    @Test
    @DisplayName("Pomyślne pobranie listy wszystkich kont")
    void shouldReturnAllAccounts() {
        when(accountRepository.findAll()).thenReturn(mockAccounts);

        List<Account> result = accountService.getAllAccounts();

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size()),
                () -> assertEquals("user1", result.get(0).getLogin()),
                () -> assertEquals("user2", result.get(1).getLogin()),
                () -> verify(accountRepository, times(1)).findAll()
        );
    }

    @Test
    @DisplayName("Zwrócenie pustej listy, gdy w bazie nie ma żadnych kont")
    void shouldReturnEmptyListWhenNoAccountsExist() {
        when(accountRepository.findAll()).thenReturn(new ArrayList<>());

        List<Account> result = accountService.getAllAccounts();

        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty()),
                () -> verify(accountRepository, times(1)).findAll()
        );
    }

    @Test
    @DisplayName("Pomyślne pobranie listy kont przefiltrowanych po frazie")
    void shouldReturnFilteredAccounts() {
        String phrase = "user1";
        when(accountRepository.findByLoginContainingIgnoreCaseOrEmailContainingIgnoreCase(phrase, phrase)).thenReturn(filteredAccounts);

        List<Account> result = accountService.searchAccounts(phrase);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertEquals("user1", result.get(0).getLogin()),
                () -> verify(accountRepository, times(1)).findByLoginContainingIgnoreCaseOrEmailContainingIgnoreCase(phrase, phrase)
        );
    }

    @Test
    @DisplayName("Zwrócenie pustej listy, gdy żadne konto nie pasuje do wyszukiwanej frazy")
    void shouldReturnEmptyListWhenNoAccountsMatchPhrase() {
        String phrase = "nonexistent";
        when(accountRepository.findByLoginContainingIgnoreCaseOrEmailContainingIgnoreCase(phrase, phrase)).thenReturn(new ArrayList<>());

        List<Account> result = accountService.searchAccounts(phrase);

        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty()),
                () -> verify(accountRepository, times(1)).findByLoginContainingIgnoreCaseOrEmailContainingIgnoreCase(phrase, phrase)
        );
    }
}