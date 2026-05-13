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
import java.util.Optional;

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

    private Account admin;

    @BeforeEach
    void setUp() {
        admin = new Account();
        admin.setLogin("admin");
        admin.setListPageSize(10);
        admin.setListSortBy("login");
        admin.setListSortDesc(false);

        mockAccounts = new ArrayList<>();
        Account acc1 = new Account();
        acc1.setLogin("j.kowalski");
        acc1.setEmail("jan.kowalski@example.com");
        acc1.setName("Jan");
        acc1.setSurname("Kowalski");

        Account acc2 = new Account();
        acc2.setLogin("a.nowak");
        acc2.setEmail("anna.nowak@example.com");
        acc2.setName("Anna");
        acc2.setSurname("Nowak");

        Account acc3 = new Account();
        acc3.setLogin("j.zielinski");
        acc3.setEmail("jan.zielinski@example.com");
        acc3.setName("Piotr");
        acc3.setSurname("Zielinski");

        mockAccounts.add(acc1);
        mockAccounts.add(acc2);
        mockAccounts.add(acc3);

        mockAccountsPage = new PageImpl<>(mockAccounts, PageRequest.of(0, 10, Sort.by("login").ascending()), 3);
        emptyAccountsPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10, Sort.by("login").ascending()), 0);
    }

    @Test
    @DisplayName("Pomyślne pobranie listy wszystkich kont")
    void shouldReturnAllAccounts() {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("login").ascending());
        when(accountRepository.findByLogin("admin")).thenReturn(Optional.of(admin));
        when(accountRepository.findAll(pageRequest)).thenReturn(mockAccountsPage);

        Page<Account> result = accountService.getAllAccounts(0, 10, "login", false, "admin");

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(3, result.getTotalElements()),
                () -> verify(accountRepository, times(1)).findAll(pageRequest)
        );
    }

    @Test
    @DisplayName("Zwrócenie pustej listy, gdy w bazie nie ma żadnych kont")
    void shouldReturnEmptyListWhenNoAccountsExist() {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("login").ascending());
        when(accountRepository.findByLogin("admin")).thenReturn(Optional.of(admin));
        when(accountRepository.findAll(pageRequest)).thenReturn(emptyAccountsPage);

        Page<Account> result = accountService.getAllAccounts(0, 10, "login", false, "admin");

        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty()),
                () -> verify(accountRepository, times(1)).findAll(pageRequest)
        );
    }

    @Test
    @DisplayName("Pomyślne wyszukiwanie po imieniu (częściowe dopasowanie)")
    void shouldReturnAccountsFilteredByName() {
        String phrase = "Ja";
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("login").ascending());
        Page<Account> filteredPage = new PageImpl<>(List.of(mockAccounts.get(0), mockAccounts.get(2)), pageRequest, 2);

        when(accountRepository.findByLogin("admin")).thenReturn(Optional.of(admin));
        when(accountRepository.findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(phrase, phrase, pageRequest)).thenReturn(filteredPage);

        Page<Account> result = accountService.searchAccounts(phrase, 0, 10, "login", false, "admin");

        assertAll(
                () -> assertEquals(2, result.getTotalElements()),
                () -> assertTrue(result.getContent().stream().anyMatch(a -> a.getName().equals("Jan"))),
                () -> verify(accountRepository, times(1)).findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(phrase, phrase, pageRequest)
        );
    }

    @Test
    @DisplayName("Pomyślne wyszukiwanie po nazwisku ignorując wielkość liter")
    void shouldReturnAccountsFilteredBySurnameIgnoreCase() {
        String phrase = "nOwAk";
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("login").ascending());
        Page<Account> filteredPage = new PageImpl<>(List.of(mockAccounts.get(1)), pageRequest, 1);

        when(accountRepository.findByLogin("admin")).thenReturn(Optional.of(admin));
        when(accountRepository.findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(phrase, phrase, pageRequest)).thenReturn(filteredPage);

        Page<Account> result = accountService.searchAccounts(phrase, 0, 10, "login", false, "admin");

        assertAll(
                () -> assertEquals(1, result.getTotalElements()),
                () -> assertEquals("Nowak", result.getContent().get(0).getSurname()),
                () -> verify(accountRepository, times(1)).findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(phrase, phrase, pageRequest)
        );
    }

    @Test
    @DisplayName("Pomyślne wyszukiwanie po loginie")
    void shouldReturnAccountsFilteredByLogin() {
        String phrase = "j.zielinski";
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("login").ascending());
        Page<Account> filteredPage = new PageImpl<>(List.of(mockAccounts.get(2)), pageRequest, 1);

        when(accountRepository.findByLogin("admin")).thenReturn(Optional.of(admin));
        when(accountRepository.findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(phrase, phrase, pageRequest)).thenReturn(filteredPage);

        Page<Account> result = accountService.searchAccounts(phrase, 0, 10, "login", false, "admin");

        assertEquals(1, result.getTotalElements());
        assertEquals("j.zielinski", result.getContent().get(0).getLogin());
    }

    @Test
    @DisplayName("Zwrócenie pustej listy, gdy żadne pole nie pasuje do frazy")
    void shouldReturnEmptyListWhenNoFieldsMatch() {
        String phrase = "nieistniejący_użytkownik_123";
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("login").ascending());

        when(accountRepository.findByLogin("admin")).thenReturn(Optional.of(admin));
        when(accountRepository.findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(phrase, phrase, pageRequest)).thenReturn(emptyAccountsPage);

        Page<Account> result = accountService.searchAccounts(phrase, 0, 10, "login", false, "admin");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Powinno zaktualizować preferencje użytkownika i wykonać zapis")
    void shouldUpdateUserPreferences() {
        PageRequest pageRequest = PageRequest.of(0, 5, Sort.by("email").descending());
        when(accountRepository.findByLogin("admin")).thenReturn(Optional.of(admin));
        when(accountRepository.findAll(pageRequest)).thenReturn(mockAccountsPage);

        accountService.getAllAccounts(0, 5, "email", true, "admin");

        assertEquals(5, admin.getListPageSize());
        assertEquals("email", admin.getListSortBy());
        assertTrue(admin.getListSortDesc());
        verify(accountRepository, times(1)).saveAndFlush(admin);
    }
}