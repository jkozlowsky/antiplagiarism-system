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
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.MokRefreshRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceBlockTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MokRefreshRepository mokRefreshRepository;

    @Mock
    private VersionHashService versionHashService;

    @Mock
    private PostCommitTransactionManager postCommitTransactionManager;

    @Mock
    private EmailRestClient emailRestClient;

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
    void blockUserAccount_ShouldBlockAccount_WhenValidDataProvided() {
        mockAccount.setIsBlocked(false);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));
        when(versionHashService.hashObjectVersion(accountId, mockAccount.getVersion())).thenReturn(validVersionHash);

        accountService.blockUserAccount(accountId, validVersionHash);

        assertTrue(mockAccount.getIsBlocked(), "Account should be blocked");

        verify(mokRefreshRepository).refresh(mockAccount);
        verify(accountRepository).saveAndFlush(mockAccount);
        verify(postCommitTransactionManager).register(any(Runnable.class));
    }

    @Test
    void blockUserAccount_ShouldThrowConcurrentUpdateException_WhenHashMismatches() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));
        when(versionHashService.hashObjectVersion(accountId, mockAccount.getVersion())).thenReturn("different-hash");

        assertThrows(ConcurrentUpdateException.class,
                () -> accountService.blockUserAccount(accountId, validVersionHash),
                "Should throw ConcurrentUpdateException when version hash does not match"
        );

        verify(accountRepository, never()).saveAndFlush(any());
    }


}