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
import pl.lodz.p.it.ssbd2026.ssbd01.entities.AccessLevel;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.AccessLevelDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Teacher;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccessLevelDictNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccessLevelNotAssignedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccountNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccessLevelDictRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccessLevelRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;

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
    private AccessLevelRepository accessLevelRepository;

    @InjectMocks
    private AccountService accountService;

    private Account account;
    private AccessLevelDict accessLevelDict;
    private AccessLevel accessLevel;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        account = new Account();

        ReflectionTestUtils.setField(account, "version", 0);
        account.setAccessLevelList(new ArrayList<>());

        accessLevelDict = new AccessLevelDict();
        ReflectionTestUtils.setField(accessLevelDict, "id", UUID.randomUUID());
        accessLevelDict.setAccessLevelName(AccountConsts.ROLE_TEACHER);

        accessLevel = new Teacher();
        accessLevel.setAccount(account);
        accessLevel.setAccessLevelDict(accessLevelDict);
    }

    @Test
    @DisplayName("Pomyślne usunięcie roli dostępu z wybranego konta użytkownika")
    void shouldRevokeAccessLevelSuccessfully() {
        account.getAccessLevelList().add(accessLevel);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accessLevelDictRepository.findByAccessLevelName(AccountConsts.ROLE_TEACHER)).thenReturn(Optional.of(accessLevelDict));

        accountService.revokeAccessLevel(accountId, AccountConsts.ROLE_TEACHER, 0);

        assertAll(
                () -> verify(accountRepository, times(1)).saveAndFlush(account),
                () -> assertTrue(account.getAccessLevelList().isEmpty())
        );
    }

    @Test
    @DisplayName("Wyrzucenie błędu AccountNotFoundException, gdy konto docelowe nie istnieje w bazie")
    void shouldThrowAccountNotFoundException() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () ->
                accountService.revokeAccessLevel(accountId, AccountConsts.ROLE_TEACHER, 0));
    }

    @Test
    @DisplayName("Wyrzucenie błędu ConcurrentUpdateException w przypadku niezgodności wersji obiektu")
    void shouldThrowConcurrentUpdateException() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThrows(ConcurrentUpdateException.class, () ->
                accountService.revokeAccessLevel(accountId, AccountConsts.ROLE_TEACHER, 2));
    }

    @Test
    @DisplayName("Wyrzucenie błędu AccessLevelDictNotFoundException dla nieistniejącej nazwy roli")
    void shouldThrowAccessLevelDictNotFoundException() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accessLevelDictRepository.findByAccessLevelName("NON_EXISTENT")).thenReturn(Optional.empty());

        assertThrows(AccessLevelDictNotFoundException.class, () ->
                accountService.revokeAccessLevel(accountId, "NON_EXISTENT", 0));
    }

    @Test
    @DisplayName("Wyrzucenie błędu AccessLevelNotAssignedException, gdy konto nie posiada danej roli")
    void shouldThrowAccessLevelNotAssignedException() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accessLevelDictRepository.findByAccessLevelName(AccountConsts.ROLE_TEACHER)).thenReturn(Optional.of(accessLevelDict));

        assertThrows(AccessLevelNotAssignedException.class, () ->
                accountService.revokeAccessLevel(accountId, AccountConsts.ROLE_TEACHER, 0));
    }
}