package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.common.utils.PostCommitTransactionManager;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel.AccessLevelAlreadyRevokedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel.AccessLevelDictNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel.AccessLevelNotAssignedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccountNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccessLevelDictRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.MokRefreshRepository;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceRevokeAccessTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccessLevelDictRepository accessLevelDictRepository;

    @Mock
    private VersionHashService versionHashService;

    @Mock
    private MokRefreshRepository mokRefreshRepository;

    @Mock
    private PostCommitTransactionManager postCommitTransactionManager;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AccountService accountService;

    private Account account;
    private AccessLevelDict accessLevelDict;
    private AccessLevel accessLevel;
    private UUID accountId;
    private final String VALID_HASH = "mocked-valid-hash-123";

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        account = new Account();
        LanguageDict langDict = new LanguageDict();
        langDict.setLanguage("pl");
        account.setLanguageVersion(langDict);
        ReflectionTestUtils.setField(account, "id", accountId);
        ReflectionTestUtils.setField(account, "version", 0);
        account.setLogin("testUser");
        account.setEmail("test@example.com");
        account.setAccessLevelList(new ArrayList<>());

        accessLevelDict = new AccessLevelDict();
        ReflectionTestUtils.setField(accessLevelDict, "id", UUID.randomUUID());
        accessLevelDict.setAccessLevelName(AccountConsts.ROLE_TEACHER);

        accessLevel = new Teacher();
        accessLevel.setAccount(account);
        accessLevel.setAccessLevelDict(accessLevelDict);
        accessLevel.setActive(true);
    }

    @Test
    @DisplayName("Pomyślne miękkie usunięcie roli dostępu (dezaktywacja)")
    void shouldRevokeAccessLevelSuccessfully() {
        account.getAccessLevelList().add(accessLevel);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        doNothing().when(mokRefreshRepository).refresh(account);
        when(versionHashService.hashObjectVersion(accountId, 0)).thenReturn(VALID_HASH);
        when(accessLevelDictRepository.findByAccessLevelName(AccountConsts.ROLE_TEACHER)).thenReturn(Optional.of(accessLevelDict));

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        doNothing().when(postCommitTransactionManager).register(captor.capture());

        accountService.revokeAccessLevel(accountId, AccountConsts.ROLE_TEACHER, VALID_HASH);
        captor.getValue().run();

        assertAll(
                () -> verify(accountRepository, times(1)).saveAndFlush(account),
                () -> assertEquals(1, account.getAccessLevelList().size()),
                () -> assertFalse(account.getAccessLevelList().getFirst().getActive()),
                () -> verify(emailService, times(1)).sendAccessLevelRevokedInfo(eq(account.getEmail()), eq(account.getLogin()), eq(AccountConsts.ROLE_TEACHER), anyString())
        );
    }

    @Test
    @DisplayName("Wyrzucenie błędu AccessLevelAlreadyRevokedException, gdy rola jest już dezaktywowana")
    void shouldThrowAccessLevelAlreadyRevokedException() {
        accessLevel.setActive(false);
        account.getAccessLevelList().add(accessLevel);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        doNothing().when(mokRefreshRepository).refresh(account);
        when(versionHashService.hashObjectVersion(accountId, 0)).thenReturn(VALID_HASH);
        when(accessLevelDictRepository.findByAccessLevelName(AccountConsts.ROLE_TEACHER)).thenReturn(Optional.of(accessLevelDict));

        assertThrows(AccessLevelAlreadyRevokedException.class, () ->
                accountService.revokeAccessLevel(accountId, AccountConsts.ROLE_TEACHER, VALID_HASH));
        verify(postCommitTransactionManager, never()).register(any());
    }

    @Test
    @DisplayName("Wyrzucenie błędu AccountNotFoundException, gdy konto docelowe nie istnieje w bazie")
    void shouldThrowAccountNotFoundException() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () ->
                accountService.revokeAccessLevel(accountId, AccountConsts.ROLE_TEACHER, VALID_HASH));
        verify(emailService, never()).sendAccessLevelRevokedInfo(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Wyrzucenie błędu ConcurrentUpdateException w przypadku niezgodności wersji obiektu")
    void shouldThrowConcurrentUpdateException() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        doNothing().when(mokRefreshRepository).refresh(account);
        when(versionHashService.hashObjectVersion(accountId, 0)).thenReturn(VALID_HASH);

        assertThrows(ConcurrentUpdateException.class, () ->
                accountService.revokeAccessLevel(accountId, AccountConsts.ROLE_TEACHER, "invalid-hash-999"));
        verify(postCommitTransactionManager, never()).register(any());
    }

    @Test
    @DisplayName("Wyrzucenie błędu AccessLevelNotAssignedException, gdy konto w ogóle nie posiada danej roli (pusta lista)")
    void shouldThrowAccessLevelNotAssignedException() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        doNothing().when(mokRefreshRepository).refresh(account);
        when(versionHashService.hashObjectVersion(accountId, 0)).thenReturn(VALID_HASH);
        when(accessLevelDictRepository.findByAccessLevelName(AccountConsts.ROLE_TEACHER)).thenReturn(Optional.of(accessLevelDict));

        assertThrows(AccessLevelNotAssignedException.class, () ->
                accountService.revokeAccessLevel(accountId, AccountConsts.ROLE_TEACHER, VALID_HASH));
    }

    @Test
    @DisplayName("Wyrzucenie błędu AccessLevelDictNotFoundException, gdy podana rola nie istnieje w słowniku")
    void shouldThrowAccessLevelDictNotFoundException() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        doNothing().when(mokRefreshRepository).refresh(account);
        when(versionHashService.hashObjectVersion(accountId, 0)).thenReturn(VALID_HASH);
        when(accessLevelDictRepository.findByAccessLevelName("NON_EXISTENT_ROLE")).thenReturn(Optional.empty());

        assertThrows(AccessLevelDictNotFoundException.class, () ->
                accountService.revokeAccessLevel(accountId, "NON_EXISTENT_ROLE", VALID_HASH));
    }
}