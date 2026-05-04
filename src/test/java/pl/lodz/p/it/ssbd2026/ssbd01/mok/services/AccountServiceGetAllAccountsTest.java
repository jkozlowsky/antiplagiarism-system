package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private Page<Account> mockAccountsPage;
    private Page<Account> emptyAccountsPage;
    private Page<Account> filteredAccountsPage;

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

        mockAccountsPage = new PageImpl<>(mockAccounts, PageRequest.of(0, 10, Sort.by("login").ascending()), 2);
        emptyAccountsPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10, Sort.by("login").ascending()), 0);
        filteredAccountsPage = new PageImpl<>(List.of(acc1), PageRequest.of(0, 10, Sort.by("login").ascending()), 1);
    }

    @Test
    @DisplayName("Pomyślne pobranie listy wszystkich kont")
    void shouldReturnAllAccounts() {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("login").ascending());
        when(accountRepository.findAll(pageRequest)).thenReturn(mockAccountsPage);

        Page<Account> result = accountService.getAllAccounts(0, 10);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(2, result.getTotalElements()),
                () -> assertEquals("user1", result.getContent().get(0).getLogin()),
                () -> assertEquals("user2", result.getContent().get(1).getLogin()),
                () -> verify(accountRepository, times(1)).findAll(pageRequest)
        );
    }

    @Test
    @DisplayName("Zwrócenie pustej listy, gdy w bazie nie ma żadnych kont")
    void shouldReturnEmptyListWhenNoAccountsExist() {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("login").ascending());
        when(accountRepository.findAll(pageRequest)).thenReturn(emptyAccountsPage);

        Page<Account> result = accountService.getAllAccounts(0, 10);

        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty()),
                () -> verify(accountRepository, times(1)).findAll(pageRequest)
        );
    }

    @Test
    @DisplayName("Pomyślne pobranie listy kont przefiltrowanych po frazie")
    void shouldReturnFilteredAccounts() {
        String phrase = "user1";
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("login").ascending());
        when(accountRepository.findByLoginContainingIgnoreCaseOrEmailContainingIgnoreCase(phrase, phrase, pageRequest)).thenReturn(filteredAccountsPage);

        Page<Account> result = accountService.searchAccounts(phrase, 0, 10);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1, result.getTotalElements()),
                () -> assertEquals("user1", result.getContent().get(0).getLogin()),
                () -> verify(accountRepository, times(1)).findByLoginContainingIgnoreCaseOrEmailContainingIgnoreCase(phrase, phrase, pageRequest)
        );
    }

    @Test
    @DisplayName("Zwrócenie pustej listy, gdy żadne konto nie pasuje do wyszukiwanej frazy")
    void shouldReturnEmptyListWhenNoAccountsMatchPhrase() {
        String phrase = "nonexistent";
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("login").ascending());
        when(accountRepository.findByLoginContainingIgnoreCaseOrEmailContainingIgnoreCase(phrase, phrase, pageRequest)).thenReturn(emptyAccountsPage);

        Page<Account> result = accountService.searchAccounts(phrase, 0, 10);

        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty()),
                () -> verify(accountRepository, times(1)).findByLoginContainingIgnoreCaseOrEmailContainingIgnoreCase(phrase, phrase, pageRequest)
        );
    }
}