package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.lodz.p.it.ssbd2026.ssbd01.common.utils.PostCommitTransactionManager;
import pl.lodz.p.it.ssbd2026.ssbd01.common.utils.TwoFactorAuthenticationTokenGenerator;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.OneTimeTokenConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.*;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.LanguageDictRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.OneTimeTokenRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthService2FANegativeTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;
    @Mock
    private PostCommitTransactionManager postCommitTransactionManager;
    @Mock
    private OneTimeTokenRepository tokenRepository;
    @Mock
    private TwoFactorAuthenticationTokenGenerator tokenValueGenerator;
    @Mock
    private LanguageDictRepository languageDictRepository;
    @InjectMocks
    private AuthService authService;

    private Account testAccount;
    private final String IP_ADDRESS = "192.168.1.1";

    @BeforeEach
    void setUp() {
        LanguageDict langDict = new LanguageDict();
        langDict.setLanguage("pl");
        testAccount = new Account();
        testAccount.setLanguageVersion(langDict);
        testAccount.setLogin("testUser");
        testAccount.setPasswordHash("hashedPassword");
        testAccount.setEmail("test@example.com");
        testAccount.setIsBlocked(false);
        testAccount.setIsVerified(true);
        testAccount.setFailedAttempts(0);

        AccessLevel accessLevel = new Student();
        AccessLevelDict dict = new AccessLevelDict();
        dict.setAccessLevelName("ROLE_STUDENT");
        accessLevel.setAccessLevelDict(dict);
        accessLevel.setActive(true);
        testAccount.setAccessLevelList(List.of(accessLevel));
    }

    @Test
    void authenticate_ShouldThrowException_WhenAccountIsBlocked() {
        testAccount.setIsBlocked(true);
        when(accountRepository.findByLogin(testAccount.getLogin())).thenReturn(Optional.of(testAccount));

        assertThrows(AccountIsBlockedException.class, () ->
                authService.authenticate(testAccount.getLogin(), "anyPassword", IP_ADDRESS)
        );
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void authenticate_ShouldThrowException_WhenAccountIsNotVerified() {
        testAccount.setIsVerified(false);
        when(accountRepository.findByLogin(testAccount.getLogin())).thenReturn(Optional.of(testAccount));

        assertThrows(AccountIsNotVerifiedException.class, () ->
                authService.authenticate(testAccount.getLogin(), "anyPassword", IP_ADDRESS)
        );
    }

    @Test
    void authenticate_ShouldIncrementFailedAttemptsAndThrow_WhenPasswordIsIncorrect() {
        when(accountRepository.findByLogin(testAccount.getLogin())).thenReturn(Optional.of(testAccount));
        when(passwordEncoder.matches("wrongPassword", testAccount.getPasswordHash())).thenReturn(false);

        assertThrows(InvalidCredentialsProvidedException.class, () ->
                authService.authenticate(testAccount.getLogin(), "wrongPassword", IP_ADDRESS)
        );

        assertEquals(1, testAccount.getFailedAttempts());
        assertEquals(IP_ADDRESS, testAccount.getLastFailedLoginIp());
        verify(accountRepository, times(1)).saveAndFlush(testAccount);
    }

    @Test
    void authenticate_ShouldBlockAccount_WhenFailedAttemptsReachThree() {
        testAccount.setFailedAttempts(2);
        when(accountRepository.findByLogin(testAccount.getLogin())).thenReturn(Optional.of(testAccount));
        when(passwordEncoder.matches("wrongPassword", testAccount.getPasswordHash())).thenReturn(false);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(postCommitTransactionManager).register(any(Runnable.class));

        assertThrows(InvalidCredentialsProvidedException.class, () ->
                authService.authenticate(testAccount.getLogin(), "wrongPassword", IP_ADDRESS)
        );

        assertEquals(3, testAccount.getFailedAttempts());
        assertTrue(testAccount.getIsBlocked());
        verify(accountRepository, times(1)).saveAndFlush(testAccount);
        verify(emailService, times(1)).sendAutoBlockEmail(eq(testAccount.getEmail()), eq(testAccount.getLogin()), anyString());
    }

    @Test
    void verify2FactorCode_ShouldThrowException_WhenTokenIsInvalidOrExpired() {
        when(tokenRepository.findByTokenValueAndUsernameAndTokenTypeAndIsUsedIsFalseAndExpiresAtAfter(
                anyString(), eq(testAccount.getLogin()), eq(OneTimeTokenType.CODE_2FA), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        when(accountRepository.findByLogin(testAccount.getLogin())).thenReturn(Optional.of(testAccount));

        assertThrows(OneTimeTokenInvalidException.class, () ->
                authService.verify2FactorCode("invalidOTP", testAccount.getLogin(), IP_ADDRESS, "en")
        );

        assertEquals(1, testAccount.getFailedAttempts());
        verify(accountRepository, times(1)).saveAndFlush(testAccount);
    }

    @Test
    void resend2FactorCode_ShouldThrowException_WhenCooldownHasNotPassed() {
        OneTimeTokenEnt recentToken = new OneTimeTokenEnt();
        recentToken.setExpiresAt(LocalDateTime.now()
                .plusMinutes(OneTimeTokenConsts.EXPIRATION_ONE_TIME_TOKEN_MINUTES));

        when(accountRepository.findByLogin(testAccount.getLogin())).thenReturn(Optional.of(testAccount));
        when(tokenRepository.findByUsernameAndTokenTypeAndIsUsedIsFalse(testAccount.getLogin(), OneTimeTokenType.CODE_2FA))
                .thenReturn(List.of(recentToken));

        assertThrows(Resend2FATokenException.class, () ->
                authService.resend2FactorCode(testAccount.getLogin())
        );

        verify(tokenValueGenerator, never()).generate2FAToken();
        verify(tokenRepository, never()).saveAndFlush(any(OneTimeTokenEnt.class));
        verify(postCommitTransactionManager, never()).register(any(Runnable.class));
    }
}