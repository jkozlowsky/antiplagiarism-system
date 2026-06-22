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
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.MokRefreshRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.AccountService;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.CommonFunctions;

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

    @Mock
    private VersionHashService versionHashService;

    @Mock
    private MokRefreshRepository mokRefreshRepository;

    @InjectMocks
    private AccountService accountService;

    private final CommonFunctions commonFunctions = new CommonFunctions();

    private Account account;
    private UUID accountId;
    private String accountLogin;
    private final String oldEmail = "old@edu.p.lodz.pl";
    private final String newEmail = "new@edu.p.lodz.pl";
    private final String validHash = commonFunctions.generateRandomStringOfLength(10);
    private final String invalidHash = commonFunctions.generateRandomStringOfLength(10) + "1";

    @BeforeEach
    public void init() {
        accountId = UUID.randomUUID();
        accountLogin = commonFunctions.generateRandomLettersOfLength(12);
        account = new Account();
        account.setEmail(oldEmail);
        ReflectionTestUtils.setField(account, "id", accountId);
        ReflectionTestUtils.setField(account, "login", accountLogin);
        ReflectionTestUtils.setField(account, "version", 2);
    }

    @Test
    public void POS01_ChangeEmailByAdmin_ShouldBeSuccessful() {
        when(accountRepository.findByLogin(accountLogin)).thenReturn(Optional.of(account));
        when(versionHashService.hashObjectVersion(accountId, account.getVersion())).thenReturn(validHash);
        when(accountRepository.saveAndFlush(account)).thenReturn(account);

        Account updated = accountService.changeEmailByAdmin(accountLogin, newEmail, validHash);
        assertEquals(newEmail, updated.getEmail());
        verify(accountRepository, times(1)).saveAndFlush(account);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(postCommitTransactionManager, times(2)) //times(2), bo wysyłamy dwa maile na stary i nowy adres.
                .register(runnableCaptor.capture());
    }

    @Test
    public void NEG01_ChangeEmailByAdmin_ShouldThrowAccountNotFoundException() {
        when(accountRepository.findByLogin(accountLogin)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () ->
                accountService.changeEmailByAdmin(accountLogin, newEmail, validHash));

        verify(accountRepository, times(0)).saveAndFlush(account);
        verify(postCommitTransactionManager, times(0)).register(any());
    }

    @Test
    public void NEG02_ChangeEmailByAdmin_ShouldThrowConcurrentUpdateException() {
        when(accountRepository.findByLogin(accountLogin)).thenReturn(Optional.of(account));
        when(versionHashService.hashObjectVersion(accountId, account.getVersion())).thenReturn(validHash);

        assertAll(
                () -> assertThrows(ConcurrentUpdateException.class, () ->
                        accountService.changeEmailByAdmin(accountLogin, newEmail, invalidHash)),
                () -> assertEquals(oldEmail, account.getEmail())
        );

        verify(accountRepository, times(0)).saveAndFlush(account);
        verify(postCommitTransactionManager, times(0)).register(any());
    }

}
