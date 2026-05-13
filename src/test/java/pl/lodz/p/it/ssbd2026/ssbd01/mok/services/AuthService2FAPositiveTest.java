package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.lodz.p.it.ssbd2026.ssbd01.common.utils.PostCommitTransactionManager;
import pl.lodz.p.it.ssbd2026.ssbd01.common.utils.AuthenticationTokenValueGenerator;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.OneTimeTokenConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AuthResponseDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.LanguageDictRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.OneTimeTokenRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthService2FAPositiveTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtAuthService jwtAuthService;
    @Mock
    private EmailService emailService;
    @Mock
    private PostCommitTransactionManager postCommitTransactionManager;
    @Mock
    private AuthenticationTokenValueGenerator tokenValueGenerator;
    @Mock
    private OneTimeTokenRepository tokenRepository;
    @Mock
    private LanguageDictRepository languageDictRepository;
    @InjectMocks
    private AuthService authService;

    private Account testAccount;
    private final String IP_ADDRESS = "192.168.1.1";
    private final String OTP_CODE = "12345678";

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
    void authenticate_ShouldGenerateTokenAndTriggerEmail_WhenCredentialsAreValid() {
        when(accountRepository.findByLogin(testAccount.getLogin())).thenReturn(Optional.of(testAccount));
        when(passwordEncoder.matches("validPassword", testAccount.getPasswordHash())).thenReturn(true);
        when(tokenRepository.findByUsernameAndTokenTypeAndIsUsedIsFalse(testAccount.getLogin(), OneTimeTokenType.CODE_2FA))
                .thenReturn(Collections.emptyList());
        when(tokenValueGenerator.generate2FAToken()).thenReturn(OTP_CODE);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(postCommitTransactionManager).register(any(Runnable.class));

        authService.authenticate(testAccount.getLogin(), "validPassword", IP_ADDRESS);

        ArgumentCaptor<OneTimeTokenEnt> tokenCaptor = ArgumentCaptor.forClass(OneTimeTokenEnt.class);
        verify(tokenRepository, times(1)).saveAndFlush(tokenCaptor.capture());
        OneTimeTokenEnt savedToken = tokenCaptor.getValue();

        assertEquals(testAccount.getLogin(), savedToken.getUsername());
        assertEquals(OneTimeTokenType.CODE_2FA, savedToken.getTokenType());
        assertFalse(savedToken.isUsed());
        assertNotNull(savedToken.getTokenValue());

        verify(emailService, times(1)).send2FAuthEmail(eq(OTP_CODE), eq(testAccount.getEmail()), any());
    }

    @Test
    void verify2FactorCode_ShouldReturnJwtAndMarkTokenAsUsed_WhenCodeIsValid() {
        OneTimeTokenEnt validToken = new OneTimeTokenEnt();
        validToken.setUsername(testAccount.getLogin());
        validToken.setTokenType(OneTimeTokenType.CODE_2FA);
        validToken.setUsed(false);
        validToken.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(tokenRepository.findByTokenValueAndUsernameAndTokenTypeAndIsUsedIsFalseAndExpiresAtAfter(
                anyString(), eq(testAccount.getLogin()), eq(OneTimeTokenType.CODE_2FA), any(LocalDateTime.class)))
                .thenReturn(Optional.of(validToken));

        when(accountRepository.findByLogin(testAccount.getLogin())).thenReturn(Optional.of(testAccount));

        when(jwtAuthService.generateToken(eq(testAccount.getLogin()), anyList(),any())).thenReturn("accessToken123");
        when(jwtAuthService.generateRefreshToken(eq(testAccount.getLogin()), anyList(), any())).thenReturn("refreshToken123");

        AuthResponseDTO response = authService.verify2FactorCode(OTP_CODE, testAccount.getLogin(), IP_ADDRESS, "en");

        assertNotNull(response);
        assertEquals("accessToken123", response.token());
        assertEquals("refreshToken123", response.refreshToken());

        assertTrue(validToken.isUsed());
        verify(tokenRepository, times(1)).saveAndFlush(validToken);

        assertEquals(0, testAccount.getFailedAttempts());
        assertEquals(IP_ADDRESS, testAccount.getLastSuccessfulLoginIp());
        verify(accountRepository, times(1)).saveAndFlush(testAccount);
    }

    @Test
    void resend2FactorCode_ShouldGenerateNewToken_WhenCooldownHasPassed() {
        OneTimeTokenEnt oldToken = new OneTimeTokenEnt();
        oldToken.setExpiresAt(LocalDateTime.now()
                .minusMinutes(OneTimeTokenConsts.RESEND_TIMEOUT_2FA_IN_MINUTES + 1));

        when(accountRepository.findByLogin(testAccount.getLogin())).thenReturn(Optional.of(testAccount));
        when(tokenRepository.findByUsernameAndTokenTypeAndIsUsedIsFalse(testAccount.getLogin(), OneTimeTokenType.CODE_2FA))
                .thenReturn(List.of(oldToken));
        when(tokenValueGenerator.generate2FAToken()).thenReturn("87654321");

        authService.resend2FactorCode(testAccount.getLogin());

        verify(tokenRepository, times(1)).saveAndFlush(any(OneTimeTokenEnt.class));
        verify(postCommitTransactionManager, times(1)).register(any(Runnable.class));
    }
}