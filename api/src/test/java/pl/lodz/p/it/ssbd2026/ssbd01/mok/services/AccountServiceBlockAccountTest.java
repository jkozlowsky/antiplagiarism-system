package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.common.utils.PostCommitTransactionManager;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccountIsBlockedException;
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
public class AccountServiceBlockAccountTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private VersionHashService versionHashService;

    @Mock
    private MokRefreshRepository mokRefreshRepository;

    @Mock
    private PostCommitTransactionManager postCommitTransactionManager;

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
        account.setEmail("test@testblock.com");
        account.setLogin("block_tester");
        account.setIsBlocked(false);
        account.setPasswordHash("testcontainers_hash");

        ReflectionTestUtils.setField(account, "id", accountId);
        ReflectionTestUtils.setField(account, "version", 0);

        System.out.print("\n\n\n" +
                "Unit test - Account service block user account function\n" +
                "Check if blocking user account is working properly\"\n\n\n");
    }

    @Test
    @DisplayName("Test successful work of block function")
    void POS01_blockUserAccountUnit() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(versionHashService.hashObjectVersion(accountId, account.getVersion())).thenReturn(validHash);

        accountService.blockUserAccount(accountId, validHash);

        assertAll(
                () -> assertTrue(account.getIsBlocked()),
                () -> verify(accountRepository, times(1)).saveAndFlush(account),
                () -> verify(postCommitTransactionManager, times(1)).register(any())
        );
    }

    @Test
    @DisplayName("Test block function if account does not exist")
    void NEG01_blockUserAccountUnit() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.blockUserAccount(accountId, validHash));

        verify(emailService, never()).sendAccountIsBlockedInfo(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Test block function if account is currently blocked")
    void NEG02_blockUserAccountUnit() {
        account.setIsBlocked(true);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(versionHashService.hashObjectVersion(accountId, account.getVersion())).thenReturn(validHash);

        assertThrows(AccountIsBlockedException.class, () -> accountService.blockUserAccount(accountId, validHash));

        verify(emailService, never()).sendAccountIsBlockedInfo(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Test block function if optimistic lock fails")
    void NEG03_blockUserAccountUnit() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(versionHashService.hashObjectVersion(accountId, account.getVersion())).thenReturn(validHash);

        assertThrows(ConcurrentUpdateException.class, () -> accountService.blockUserAccount(accountId, "invalid-hash"));

        verify(emailService, never()).sendAccountIsBlockedInfo(anyString(), anyString(), any());
        verify(accountRepository, never()).saveAndFlush(any());
    }
}