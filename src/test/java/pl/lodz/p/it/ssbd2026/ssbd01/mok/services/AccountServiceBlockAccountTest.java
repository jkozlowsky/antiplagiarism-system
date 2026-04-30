package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccountIsBlockedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccountNotFound;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;

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
        account.setEmail("test@test.com");
        account.setLogin("block_tester");
        account.setIsBlocked(false);
        account.setPasswordHash("testcontainers_hash");

        System.out.print("\n\n\n" +
                "Unit test - Account service block user account function\n" +
                "Check if blocking user account is working properly\"\n\n\n");
    }

    @Test
    @DisplayName("Test successful work of block function")
    void POS01_blockUserAccountUnit() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        accountService.blockUserAccount(accountId);

        assertAll(
                () -> assertTrue(account.getIsBlocked()),
                () -> verify(accountRepository, times(1)).saveAndFlush(account),
                () -> verify(emailService, times(1)).sendAccountIsBlockedInfo(account.getEmail(), account.getLogin())
        );
    }

    @Test
    @DisplayName("Test block function if account does not exist")
    void NEG01_blockUserAccountUnit() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFound.class, () -> accountService.blockUserAccount(accountId));

        verify(emailService, never()).sendAccountIsBlockedInfo(anyString(), anyString());
    }

    @Test
    @DisplayName("Test block function if account is currently blocked")
    void NEG02_blockUserAccountUnit() {
        account.setIsBlocked(true);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThrows(AccountIsBlockedException.class, () -> accountService.blockUserAccount(accountId));

        verify(emailService, never()).sendAccountIsBlockedInfo(anyString(), anyString());
    }
}