package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccountIsNotBlockedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccountNotFound;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;

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

    @InjectMocks
    private AccountService accountService;

    private Account account;
    private UUID accountId;

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

        System.out.print("\n\n\n" +
                "Unit test - Account service unblock user account function\n" +
                "Check if unblocking user account is working properly\"\n\n\n");
    }

    @Test
    @DisplayName("Test successful work of unblock function")
    void POS01_unblockUserAccountUnit() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        accountService.unblockUserAccount(accountId);

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

        assertThrows(AccountNotFound.class, () -> accountService.unblockUserAccount(accountId));

        verify(emailService, never()).sendAccountIsUnblockedInfo(anyString(), anyString());
    }

    @Test
    @DisplayName("Test unblock function if account is currently not blocked")
    void NEG02_unblockUserAccountUnit() {
        account.setIsBlocked(false);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThrows(AccountIsNotBlockedException.class, () -> accountService.unblockUserAccount(accountId));

        verify(emailService, never()).sendAccountIsUnblockedInfo(anyString(), anyString());
    }
}