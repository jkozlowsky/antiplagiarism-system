package pl.lodz.p.it.ssbd2026.ssbd01.mok.codelogin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.ssbd2026.ssbd01.common.utils.AuthenticationTokenValueGenerator;
import pl.lodz.p.it.ssbd2026.ssbd01.common.utils.PostCommitTransactionManager;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.OneTimeTokenConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.OneTimeTokenEnt;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.LanguageDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.OneTimeTokenType;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.*;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.LanguageDictRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.OneTimeTokenRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.AuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.EmailService;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AuthResponseDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import org.mockito.ArgumentCaptor;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.CommonFunctions;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AuthWithEmailOnlyTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private OneTimeTokenRepository tokenRepository;
    @Mock
    private AuthenticationTokenValueGenerator tokenValueGenerator;
    @Mock
    private EmailService emailService;
    @Mock
    private PostCommitTransactionManager postCommitTransactionManager;
    @Mock
    private JwtAuthService jwtAuthService;
    @Mock
    private LanguageDictRepository languageDictRepository;
    @InjectMocks
    private AuthService authService;

    private Account testAccount;
    private LanguageDict plLang;
    private final CommonFunctions commonFunctions = new CommonFunctions();

    @BeforeEach
    void setUp() {
        plLang = new LanguageDict();
        plLang.setLanguage("pl");

        testAccount = new Account();
        testAccount.setLogin("testuser");
        testAccount.setEmail("test@example.com");
        testAccount.setIsBlocked(false);
        testAccount.setIsVerified(true);
        testAccount.setFailedAttempts(0);
        testAccount.setLanguageVersion(plLang);
        testAccount.setAccessLevelList(new ArrayList<>());
    }

    @Test
    void POS01_authenticateOnlyWithEmail_ShouldBeSuccessful() {
        String rawToken = commonFunctions.generateRandomStringOfLength(OneTimeTokenConsts.CODE_LOGIN_TOKEN_LENGTH);
        when(accountRepository.findByEmail(testAccount.getEmail())).thenReturn(Optional.of(testAccount));
        when(tokenValueGenerator.generateAuthWithEmailOnlyCode()).thenReturn(rawToken);

        authService.authenticateOnlyWithEmail(testAccount.getEmail(), "127.0.0.1");

        verify(tokenRepository).findByUsernameAndTokenTypeAndIsUsedIsFalse(testAccount.getLogin(), OneTimeTokenType.EMAIL_ONLY_AUTH);

        ArgumentCaptor<OneTimeTokenEnt> tokenCaptor = ArgumentCaptor.forClass(OneTimeTokenEnt.class);
        verify(tokenRepository).saveAndFlush(tokenCaptor.capture());
        OneTimeTokenEnt savedToken = tokenCaptor.getValue();

        assertEquals(testAccount.getLogin(), savedToken.getUsername());
        assertEquals(OneTimeTokenType.EMAIL_ONLY_AUTH, savedToken.getTokenType());
        assertFalse(savedToken.getIsUsed());

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(postCommitTransactionManager).register(runnableCaptor.capture());

        runnableCaptor.getValue().run();
        verify(emailService).sendEmailOnlyAuthCode(eq(testAccount.getEmail()), eq(rawToken), eq("pl"));
    }

    @Test
    void NEG01_authenticateOnlyWithEmail_ShouldThrowAccountNotFoundException() {
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () ->
                authService.authenticateOnlyWithEmail("wrong@example.com", "127.0.0.1")
        );
    }

    @Test
    void NEG02_authenticateOnlyWithEmail_ShouldThrowAccountIsBlockedException() {
        testAccount.setIsBlocked(true);
        when(accountRepository.findByEmail(testAccount.getEmail())).thenReturn(Optional.of(testAccount));

        assertThrows(AccountIsBlockedException.class, () ->
                authService.authenticateOnlyWithEmail(testAccount.getEmail(), "127.0.0.1")
        );
    }

    @Test
    void NEG03_authenticateOnlyWithEmail_ShouldThrowAccountIsNotVerifiedException() {
        testAccount.setIsVerified(false);
        when(accountRepository.findByEmail(testAccount.getEmail())).thenReturn(Optional.of(testAccount));

        assertThrows(AccountIsNotVerifiedException.class, () ->
                authService.authenticateOnlyWithEmail(testAccount.getEmail(), "127.0.0.1")
        );
    }

    @Test
    void POS02_verifyEmailOnlyAuthCode_ShouldBeSuccessful() {
        String rawToken = "ABCDEFGHIJ";

        OneTimeTokenEnt activeToken = new OneTimeTokenEnt();
        activeToken.setUsername(testAccount.getLogin());
        activeToken.setTokenType(OneTimeTokenType.EMAIL_ONLY_AUTH);
        activeToken.setIsUsed(false);

        when(accountRepository.findByEmail(testAccount.getEmail())).thenReturn(Optional.of(testAccount));

        when(tokenRepository.findByTokenValueAndUsernameAndTokenTypeAndIsUsedIsFalseAndExpiresAtAfter(
                anyString(), eq(testAccount.getLogin()), eq(OneTimeTokenType.EMAIL_ONLY_AUTH), any(LocalDateTime.class)
        )).thenReturn(Optional.of(activeToken));

        when(languageDictRepository.findByLanguage("pl")).thenReturn(Optional.of(plLang));

        when(jwtAuthService.generateToken(eq(testAccount.getLogin()), anyList(), any())).thenReturn("short-jwt-token");
        when(jwtAuthService.generateRefreshToken(eq(testAccount.getLogin()), anyList(), any())).thenReturn("refresh-jwt-token");

        AuthResponseDTO response = authService.verifyEmailOnlyAuthCode(rawToken, testAccount.getEmail(), "127.0.0.1", "pl");

        assertNotNull(response);
        assertEquals("short-jwt-token", response.token());
        assertEquals("refresh-jwt-token", response.refreshToken());

        assertTrue(activeToken.getIsUsed());
        verify(tokenRepository).saveAndFlush(activeToken);

        assertEquals(0, testAccount.getFailedAttempts());
        assertEquals("127.0.0.1", testAccount.getLastSuccessfulLoginIp());
        assertNotNull(testAccount.getLastLoginSuccessDateTime());
        verify(accountRepository).saveAndFlush(testAccount);
    }

    @Test
    void NEG04_verifyEmailOnlyAuthCode_ShouldFailWithInvalidCode() {
        when(accountRepository.findByEmail(testAccount.getEmail())).thenReturn(Optional.of(testAccount));

        when(tokenRepository.findByTokenValueAndUsernameAndTokenTypeAndIsUsedIsFalseAndExpiresAtAfter(
                anyString(), eq(testAccount.getLogin()), eq(OneTimeTokenType.EMAIL_ONLY_AUTH), any(LocalDateTime.class)
        )).thenReturn(Optional.empty());

        assertThrows(OneTimeTokenInvalidException.class, () ->
                authService.verifyEmailOnlyAuthCode("WRONGCODE!", testAccount.getEmail(), "127.0.0.1", "pl")
        );
        assertEquals(1, testAccount.getFailedAttempts());
        assertEquals("127.0.0.1", testAccount.getLastFailedLoginIp());
        assertNotNull(testAccount.getLastLoginFailureDateTime());
        verify(accountRepository).saveAndFlush(testAccount);
    }
}