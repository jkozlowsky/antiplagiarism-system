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
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AccountDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AccountWithAccessLevelsDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.projections.AccountListProjectionDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccessLevelRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.AccountMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceGetAllAccountsTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccessLevelRepository accessLevelRepository;
    @Mock
    private AccountMapper accountMapper;
    @Mock
    private VersionHashService versionHashService;

    @InjectMocks
    private AccountService accountService;

    private List<AccountListProjectionDTO> mockAccounts;
    private Page<AccountListProjectionDTO> mockAccountsPage;
    private Page<AccountListProjectionDTO> emptyAccountsPage;

    private Account admin;

    @BeforeEach
    void setUp() {
        admin = new Account();
        admin.setLogin("admin");
        admin.setListPageSize(10);
        admin.setListSortBy("login");
        admin.setListSortDesc(false);

        mockAccounts = new ArrayList<>();

        mockAccounts.add(new AccountListProjectionDTO(UUID.randomUUID(), "j.kowalski", "Jan", "Kowalski", "jan.kowalski@example.com", null,1));
        mockAccounts.add(new AccountListProjectionDTO(UUID.randomUUID(), "a.nowak", "Anna", "Nowak", "anna.nowak@example.com", null, 1));
        mockAccounts.add(new AccountListProjectionDTO(UUID.randomUUID(), "j.zielinski", "Piotr", "Zielinski", "jan.zielinski@example.com", null, 1));

        mockAccountsPage = new PageImpl<>(mockAccounts, PageRequest.of(0, 10, Sort.by("login").ascending()), 3);
        emptyAccountsPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10, Sort.by("login").ascending()), 0);

        lenient().when(accessLevelRepository.findRolesByAccountIds(anyList())).thenReturn(new ArrayList<>());
        lenient().when(versionHashService.hashObjectVersion(any(), any())).thenReturn("dummyHash");

        lenient().when(accountMapper.projectionToAccountDTO(any(), anyString())).thenAnswer(invocation -> {
            AccountListProjectionDTO proj = invocation.getArgument(0);
            return new AccountDTO(proj.id(), "dummyHash", proj.login(), null, true, false, proj.name(), proj.surname(), proj.email(), null, null, null, null, null, null, null, null);
        });
    }

    @Test
    @DisplayName("Pomyślne pobranie listy wszystkich kont")
    void shouldReturnAllAccounts() {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("login").ascending());
        when(accountRepository.findByLogin("admin")).thenReturn(Optional.of(admin));

        when(accountRepository.findAllProjected(pageRequest)).thenReturn(mockAccountsPage);

        Page<AccountWithAccessLevelsDTO> result = accountService.getAllAccounts(0, 10, "login", false, "admin");

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(3, result.getTotalElements()),
                () -> verify(accountRepository, times(1)).findAllProjected(pageRequest)
        );
    }

    @Test
    @DisplayName("Zwrócenie pustej listy, gdy w bazie nie ma żadnych kont")
    void shouldReturnEmptyListWhenNoAccountsExist() {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("login").ascending());
        when(accountRepository.findByLogin("admin")).thenReturn(Optional.of(admin));

        when(accountRepository.findAllProjected(pageRequest)).thenReturn(emptyAccountsPage);

        Page<AccountWithAccessLevelsDTO> result = accountService.getAllAccounts(0, 10, "login", false, "admin");

        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty()),
                () -> verify(accountRepository, times(1)).findAllProjected(pageRequest)
        );
    }

    @Test
    @DisplayName("Pomyślne wyszukiwanie po imieniu (częściowe dopasowanie)")
    void shouldReturnAccountsFilteredByName() {
        String phrase = "Ja";
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("login").ascending());
        Page<AccountListProjectionDTO> filteredPage = new PageImpl<>(List.of(mockAccounts.get(0), mockAccounts.get(2)), pageRequest, 2);

        when(accountRepository.findByLogin("admin")).thenReturn(Optional.of(admin));

        when(accountRepository.searchAccountsProjected(phrase, pageRequest)).thenReturn(filteredPage);

        Page<AccountWithAccessLevelsDTO> result = accountService.searchAccounts(phrase, 0, 10, "login", false, "admin");

        assertAll(
                () -> assertEquals(2, result.getTotalElements()),
                () -> assertTrue(result.getContent().stream().anyMatch(a -> a.account().name().equals("Jan"))),
                () -> verify(accountRepository, times(1)).searchAccountsProjected(phrase, pageRequest)
        );
    }

    @Test
    @DisplayName("Pomyślne wyszukiwanie po nazwisku ignorując wielkość liter")
    void shouldReturnAccountsFilteredBySurnameIgnoreCase() {
        String phrase = "nOwAk";
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("login").ascending());
        Page<AccountListProjectionDTO> filteredPage = new PageImpl<>(List.of(mockAccounts.get(1)), pageRequest, 1);

        when(accountRepository.findByLogin("admin")).thenReturn(Optional.of(admin));

        when(accountRepository.searchAccountsProjected(phrase, pageRequest)).thenReturn(filteredPage);

        Page<AccountWithAccessLevelsDTO> result = accountService.searchAccounts(phrase, 0, 10, "login", false, "admin");

        assertAll(
                () -> assertEquals(1, result.getTotalElements()),
                () -> assertEquals("a.nowak", result.getContent().get(0).account().login()),
                () -> verify(accountRepository, times(1)).searchAccountsProjected(phrase, pageRequest)
        );
    }

    @Test
    @DisplayName("Pomyślne wyszukiwanie po loginie")
    void shouldReturnAccountsFilteredByLogin() {
        String phrase = "j.zielinski";
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("login").ascending());
        Page<AccountListProjectionDTO> filteredPage = new PageImpl<>(List.of(mockAccounts.get(2)), pageRequest, 1);

        when(accountRepository.findByLogin("admin")).thenReturn(Optional.of(admin));
        when(accountRepository.searchAccountsProjected(phrase, pageRequest)).thenReturn(filteredPage);

        Page<AccountWithAccessLevelsDTO> result = accountService.searchAccounts(phrase, 0, 10, "login", false, "admin");

        assertEquals(1, result.getTotalElements());
        assertEquals("j.zielinski", result.getContent().get(0).account().login());
    }

    @Test
    @DisplayName("Zwrócenie pustej listy, gdy żadne pole nie pasuje do frazy")
    void shouldReturnEmptyListWhenNoFieldsMatch() {
        String phrase = "nieistniejący_użytkownik_123";
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("login").ascending());

        when(accountRepository.findByLogin("admin")).thenReturn(Optional.of(admin));
        when(accountRepository.searchAccountsProjected(phrase, pageRequest)).thenReturn(emptyAccountsPage);

        Page<AccountWithAccessLevelsDTO> result = accountService.searchAccounts(phrase, 0, 10, "login", false, "admin");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Powinno zaktualizować preferencje użytkownika i wykonać zapis")
    void shouldUpdateUserPreferences() {
        PageRequest pageRequest = PageRequest.of(0, 5, Sort.by("email").descending());
        when(accountRepository.findByLogin("admin")).thenReturn(Optional.of(admin));
        when(accountRepository.findAllProjected(pageRequest)).thenReturn(mockAccountsPage);

        accountService.getAllAccounts(0, 5, "email", true, "admin");

        assertEquals(5, admin.getListPageSize());
        assertEquals("email", admin.getListSortBy());
        assertTrue(admin.getListSortDesc());
        verify(accountRepository, times(1)).saveAndFlush(admin);
    }
}
