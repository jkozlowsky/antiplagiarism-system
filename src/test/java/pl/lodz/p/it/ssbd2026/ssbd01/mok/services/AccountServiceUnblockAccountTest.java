package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccountIsNotBlockedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccountNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.MokRefreshRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.CommonFunctions;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceUnblockAccountTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private VersionHashService versionHashService;

    @Mock
    private MokRefreshRepository mokRefreshRepository;

    @InjectMocks
    private AccountService accountService;

    private Account account;
    private UUID accountId;

    private final CommonFunctions commonFunctions = new CommonFunctions();
    private final String validHash = commonFunctions.generateRandomStringOfLength(10);

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        account = new Account();
        account.setName("Tester");
        account.setSurname("Surname");
        account.setEmail("test@testunblock.com");
        account.setLogin("unblock_tester");
        account.setIsBlocked(true);
        account.setPasswordHash("testcontainers_hash");

        ReflectionTestUtils.setField(account, "id", accountId);
        ReflectionTestUtils.setField(account, "version", 0);

        System.out.print("\n\n\n" +
                "Unit test - Account service unblock user account function\n" +
                "Check if unblocking user account is working properly\"\n\n\n");
    }

    @Test
    @DisplayName("Test successful work of unblock function")
    void POS01_unblockUserAccountUnit() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(versionHashService.hashObjectVersion(accountId, account.getVersion())).thenReturn(validHash);

        accountService.unblockUserAccount(accountId, validHash);

        assertAll(
                () -> assertFalse(account.getIsBlocked()),
                () -> verify(accountRepository, times(1)).saveAndFlush(account),
                () -> verify(emailService, times(1)).sendAccountIsUnblockedInfo(account.getEmail(), account.getLogin())
        );
    }

    @Test
    @DisplayName("Test unblock function if account does not exist")
    void NEG01_unblockUserAccountUnit() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.unblockUserAccount(accountId, validHash));

        verify(emailService, never()).sendAccountIsUnblockedInfo(anyString(), anyString());
    }

    @Test
    @DisplayName("Test unblock function if account is currently not blocked")
    void NEG02_unblockUserAccountUnit() {
        account.setIsBlocked(false);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(versionHashService.hashObjectVersion(accountId, account.getVersion())).thenReturn(validHash);

        assertThrows(AccountIsNotBlockedException.class, () -> accountService.unblockUserAccount(accountId, validHash));

        verify(emailService, never()).sendAccountIsUnblockedInfo(anyString(), anyString());
    }

    @Test
    @DisplayName("Test unblock function if optimistic lock fails")
    void NEG03_unblockUserAccountUnit() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(versionHashService.hashObjectVersion(accountId, account.getVersion())).thenReturn(validHash);

        assertThrows(ConcurrentUpdateException.class, () -> accountService.unblockUserAccount(accountId, "invalid-hash"));

        verify(emailService, never()).sendAccountIsUnblockedInfo(anyString(), anyString());
        verify(accountRepository, never()).saveAndFlush(any());
    }
}