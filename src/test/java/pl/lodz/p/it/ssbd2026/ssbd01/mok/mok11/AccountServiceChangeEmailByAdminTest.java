package pl.lodz.p.it.ssbd2026.ssbd01.mok.mok11;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.common.utils.PostCommitTransactionManager;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccountNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.AccountService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceChangeEmailByAdminTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PostCommitTransactionManager postCommitTransactionManager;

    @InjectMocks
    private AccountService accountService;

    private Account account;
    private UUID accountId;
    private final String oldEmail = "old@edu.p.lodz.pl";
    private final String newEmail = "new@edu.p.lodz.pl";

    @BeforeEach
    public void init() {
        accountId = UUID.randomUUID();
        account = new Account();
        account.setEmail(oldEmail);
        ReflectionTestUtils.setField(account, "id", accountId);
        ReflectionTestUtils.setField(account, "version", 2);
    }

    @Test
    public void POS01_ChangeEmailByAdmin_ShouldBeSuccessful() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.saveAndFlush(account)).thenReturn(account);

        Account updated = accountService.changeEmailByAdmin(account.getId(), newEmail, account.getVersion());
        assertEquals(newEmail, updated.getEmail());
        verify(accountRepository, times(1)).saveAndFlush(account);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(postCommitTransactionManager, times(2)) //times(2), bo wysyłamy dwa maile na stary i nowy adres.
                .register(runnableCaptor.capture());
    }

    @Test
    public void NEG01_ChangeEmailByAdmin_ShouldThrowAccountNotFoundException() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () ->
                accountService.changeEmailByAdmin(accountId, newEmail, 67));

        verify(accountRepository, times(0)).saveAndFlush(account);
        verify(postCommitTransactionManager, times(0)).register(any());
    }

    @Test
    public void NEG02_ChangeEmailByAdmin_ShouldThrowConcurrentUpdateException() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertAll(
                () -> assertThrows(ConcurrentUpdateException.class, () ->
                        accountService.changeEmailByAdmin(accountId, newEmail, account.getVersion() - 1)),
                () -> assertEquals(oldEmail, account.getEmail())
        );

        verify(accountRepository, times(0)).saveAndFlush(account);
        verify(postCommitTransactionManager, times(0)).register(any());
    }

}
