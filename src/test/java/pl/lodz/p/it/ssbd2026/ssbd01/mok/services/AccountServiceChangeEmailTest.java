package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.ssbd2026.ssbd01.common.utils.PostCommitTransactionManager;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.LanguageDict;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccountNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.MokRefreshRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class AccountServiceChangeEmailTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MokRefreshRepository mokRefreshRepository;

    @Mock
    private VersionHashService versionHashService;

    @Mock
    private PostCommitTransactionManager postCommitTransactionManager;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AccountService accountService;

    private Account mockAccount;
    private final UUID accountId = UUID.randomUUID();
    private final String validVersionHash = "valid-hash-123";

    @BeforeEach
    void setUp() {
        mockAccount = new Account();

        mockAccount.setEmail("test@example.com");
        mockAccount.setLogin("testUser");

        LanguageDict languageDict = new LanguageDict();
        languageDict.setLanguage("en");
        mockAccount.setLanguageVersion(languageDict);
    }

    @Test
    void changeEmailByAdmin_ShouldChangeEmail_WhenValidDataProvided() {
        String newEmail = "new_admin_email@example.com";
        String oldEmail = mockAccount.getEmail();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));
        when(versionHashService.hashObjectVersion(accountId, mockAccount.getVersion())).thenReturn(validVersionHash);
        when(accountRepository.saveAndFlush(any(Account.class))).thenReturn(mockAccount);

        Account updatedAccount = accountService.changeEmailByAdmin(accountId, newEmail, validVersionHash);

        assertNotNull(updatedAccount);
        assertEquals(newEmail, updatedAccount.getEmail(), "Email should be updated to the new email");

        verify(mokRefreshRepository).refresh(mockAccount);
        verify(accountRepository).saveAndFlush(mockAccount);
        verify(postCommitTransactionManager, times(2)).register(any(Runnable.class));
    }

    @Test
    void changeEmailByAdmin_ShouldThrowAccountNotFoundException_WhenAccountDoesNotExist() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> accountService.changeEmailByAdmin(accountId, "new@example.com", validVersionHash)
        );

        verify(versionHashService, never()).hashObjectVersion(any(), any());
        verify(accountRepository, never()).saveAndFlush(any());
    }
}
