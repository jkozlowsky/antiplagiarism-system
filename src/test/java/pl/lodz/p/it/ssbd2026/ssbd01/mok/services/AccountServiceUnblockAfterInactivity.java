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
import pl.lodz.p.it.ssbd2026.ssbd01.entities.OneTimeTokenEnt;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.OneTimeTokenType;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.OneTimeTokenInvalidException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.OneTimeTokenRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceUnblockAfterInactivity {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private OneTimeTokenRepository tokenRepository;

    @Mock
    private PostCommitTransactionManager postCommitTransactionManager;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AccountService accountService;

    private Account mockAccount;
    private OneTimeTokenEnt mockToken;

    @BeforeEach
    void setUp() {
        LanguageDict plDict = new LanguageDict();
        plDict.setLanguage("pl");

        mockAccount = new Account();
        mockAccount.setLogin("inactiveias64user");
        mockAccount.setEmail("iasuser64@sigma.com");
        mockAccount.setIsBlocked(true);
        mockAccount.setLanguageVersion(plDict);

        mockToken = new OneTimeTokenEnt();
        mockToken.setTokenValue("64mlodychadeptowiasu");
        mockToken.setUsername("inactiveias64user");
        mockToken.setTokenType(OneTimeTokenType.ACCOUNT_UNBLOCK);
        mockToken.setExpiresAt(LocalDateTime.now().plusDays(1));
        mockToken.setIsUsed(false);
    }

    @Test
    void shouldSuccessfullyUnblockAccountWithValidToken() {
        String tokenString = "ias64token";
        when(tokenRepository.findByTokenValueAndTokenTypeAndIsUsedIsFalseAndExpiresAtAfter(
                eq(tokenString), eq(OneTimeTokenType.ACCOUNT_UNBLOCK), any(LocalDateTime.class)))
                .thenReturn(Optional.of(mockToken));
        when(accountRepository.findByLogin("inactiveias64user")).thenReturn(Optional.of(mockAccount));

        accountService.unblockAccountViaEmailToken(tokenString);

        assertFalse(mockAccount.getIsBlocked());
        assertTrue(mockToken.getIsUsed());
        assertNotNull(mockAccount.getLastLoginSuccessDateTime());

        verify(accountRepository).saveAndFlush(mockAccount);
        verify(tokenRepository).saveAndFlush(mockToken);
        verify(postCommitTransactionManager).register(any(Runnable.class));
    }

    @Test
    void shouldThrowExceptionWhenTokenIsExpiredOrUsed() {
        String tokenString = "ias64token";
        when(tokenRepository.findByTokenValueAndTokenTypeAndIsUsedIsFalseAndExpiresAtAfter(
                eq(tokenString), eq(OneTimeTokenType.ACCOUNT_UNBLOCK), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        assertThrows(OneTimeTokenInvalidException.class, () -> {
            accountService.unblockAccountViaEmailToken(tokenString);
        });

        verify(accountRepository, never()).saveAndFlush(any());
    }
}
