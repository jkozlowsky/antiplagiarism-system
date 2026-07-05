package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.ssbd2026.ssbd01.common.utils.PostCommitTransactionManager;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.AccountMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.LanguageDict;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccountActivationException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccessLevelDictRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.MokRefreshRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceActivateTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccessLevelDictRepository accessLevelDictRepository;

    @Mock
    private MokRefreshRepository mokRefreshRepository;

    @Mock
    private VersionHashService versionHashService;

    @Mock
    private PostCommitTransactionManager postCommitTransactionManager;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private EmailRestClient emailRestClient;

    @InjectMocks
    private AccountService accountService;

    private Account mockAccount;
    private final UUID accountId = UUID.randomUUID();
    private final String validToken = "activation-token-123";
    private final String validVersionHash = "valid-hash-123";

    @BeforeEach
    void setUp() {
        mockAccount = new Account();
//        mockAccount.setId(accountId);
//        mockAccount.setVersion(1L);
        mockAccount.setLogin("testUser");
        mockAccount.setEmail("test@example.com");

        LanguageDict languageDict = new LanguageDict();
        languageDict.setLanguage("en");
        mockAccount.setLanguageVersion(languageDict);
    }

    @Test
    void confirmActivation_ShouldActivateAccount_WhenTokenIsValidAndNotExpired() {
        mockAccount.setActivationToken(validToken);
        mockAccount.setCreationDate(LocalDateTime.now().minusHours(2));
        mockAccount.setIsVerified(false);
        mockAccount.setIsBlocked(true);

        when(accountRepository.findByActivationToken(validToken)).thenReturn(Optional.of(mockAccount));

        accountService.confirmActivation(validToken);

        assertTrue(mockAccount.getIsVerified(), "Account should be verified");
        assertFalse(mockAccount.getIsBlocked(), "Account should be unblocked");
        assertNull(mockAccount.getActivationToken(), "Activation token should be cleared");

        verify(accountRepository).saveAndFlush(mockAccount);
    }

    @Test
    void confirmActivation_ShouldThrowAccountActivationException_WhenTokenIsExpired() {
        mockAccount.setActivationToken(validToken);
        mockAccount.setCreationDate(LocalDateTime.now().minusHours(25));

        when(accountRepository.findByActivationToken(validToken)).thenReturn(Optional.of(mockAccount));

        assertThrows(AccountActivationException.class,
                () -> accountService.confirmActivation(validToken),
                "Should throw exception when activation token is older than 24 hours"
        );

        verify(accountRepository, never()).saveAndFlush(any());
    }


    @Test
    void grantAccessLevel_ShouldThrowConcurrentUpdateException_WhenVersionHashDoesNotMatch() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));
        when(versionHashService.hashObjectVersion(accountId, mockAccount.getVersion())).thenReturn("invalid-hash");

        assertThrows(ConcurrentUpdateException.class,
                () -> accountService.grantAccessLevel(accountId, AccountConsts.ROLE_ADMIN, validVersionHash)
        );

        verify(accessLevelDictRepository, never()).findByAccessLevelName(anyString());
        verify(accountRepository, never()).saveAndFlush(any());
    }
}