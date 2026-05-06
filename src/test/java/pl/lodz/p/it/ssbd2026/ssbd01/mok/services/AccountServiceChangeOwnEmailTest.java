package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.common.utils.PostCommitTransactionManager;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.OneTimeTokenConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.OneTimeTokenEnt;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.OneTimeTokenType;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccountNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.InvalidCredentialsProvidedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.OneTimeTokenInvalidException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.MokRefreshRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.OneTimeTokenRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.CommonFunctions;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceChangeOwnEmailTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private OneTimeTokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private VersionHashService versionHashService;

    @Mock
    PostCommitTransactionManager postCommitTransactionManager;

    @Mock
    private MokRefreshRepository mokRefreshRepository;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;
    private UUID accountId;
    private final String FRONTEND_URL = "http://localhost:5173";

    private final CommonFunctions commonFunctions = new CommonFunctions();
    private final String validHash = commonFunctions.generateRandomStringOfLength(10);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(accountService, "frontendUrl", FRONTEND_URL);

        accountId = UUID.randomUUID();
        testAccount = new Account();
        testAccount.setLogin("ownemail_tester");
        testAccount.setEmail("test@testownemail.com");
        testAccount.setPasswordHash("testcontainers_hash");

        ReflectionTestUtils.setField(testAccount, "id", accountId);
        ReflectionTestUtils.setField(testAccount, "version", 0);
    }

    @Test
    @DisplayName("Test successful work of own email change function - create url")
    void POS01_changeOwnEmailUnit() {
        when(accountRepository.findByLogin("ownemail_tester")).thenReturn(Optional.of(testAccount));
        when(versionHashService.hashObjectVersion(accountId, testAccount.getVersion())).thenReturn(validHash);

        accountService.createUrlOwnEmailChange("ownemail_tester", validHash);

        ArgumentCaptor<OneTimeTokenEnt> tokenCaptor = ArgumentCaptor.forClass(OneTimeTokenEnt.class);
        verify(tokenRepository, times(1)).saveAndFlush(tokenCaptor.capture());
        OneTimeTokenEnt savedToken = tokenCaptor.getValue();

        assertAll(
                () -> assertEquals("ownemail_tester", savedToken.getUsername()),
                () -> assertFalse(savedToken.isUsed()),
                () -> assertNotNull(savedToken.getTokenValue()),
                () -> verify(postCommitTransactionManager, times(1)).register(any())
        );
    }

    @Test
    @DisplayName("Test successful work of own email change function - check if password was changed")
    void POS02_changeOwnEmailUnit() {
        String generatedUrl = UUID.randomUUID().toString();
        String newEmail = "newtest@testownemail.com";
        String password = "password";

        OneTimeTokenEnt tokenEntity = new OneTimeTokenEnt();
        tokenEntity.setTokenValue(generatedUrl);
        tokenEntity.setUsername("ownemail_tester");
        tokenEntity.setUsed(false);
        tokenEntity.setExpiresAt(LocalDateTime.now().plusMinutes(OneTimeTokenConsts.EXPIRATION_TIME_MINUTES));

        when(tokenRepository.findByTokenValueAndTokenTypeAndIsUsedIsFalseAndExpiresAtAfter(
                eq(generatedUrl),
                eq(OneTimeTokenType.OWN_EMAIL_CHANGE),
                any(LocalDateTime.class)))
                .thenReturn(Optional.of(tokenEntity));
        when(accountRepository.findByLogin("ownemail_tester"))
                .thenReturn(Optional.of(testAccount));
        when(passwordEncoder.matches(password, "testcontainers_hash"))
                .thenReturn(true);

        accountService.confirmOwnEmailChange(generatedUrl, password, newEmail);

        assertAll(
                () -> assertEquals(newEmail, testAccount.getEmail()),
                () -> assertTrue(tokenEntity.isUsed()),
                () -> verify(accountRepository, times(1)).saveAndFlush(testAccount),
                () -> verify(tokenRepository, times(1)).saveAndFlush(tokenEntity)
        );
    }

    @Test
    @DisplayName("Test block function if account does not exist")
    void NEG01_changeOwnEmailUnit() {
        when(accountRepository.findByLogin("no_existing_user")).thenReturn(Optional.empty());

        assertAll(
                () -> assertThrows(AccountNotFoundException.class, () -> accountService.createUrlOwnEmailChange("no_existing_user", validHash)),
                () -> verifyNoInteractions(tokenRepository),
                () -> verifyNoInteractions(emailService)
        );
    }

    @Test
    @DisplayName("Test block function if token is invalid")
    void NEG02_changeOwnEmailUnit() {
        when(tokenRepository.findByTokenValueAndTokenTypeAndIsUsedIsFalseAndExpiresAtAfter(
                anyString(),
                eq(OneTimeTokenType.OWN_EMAIL_CHANGE),
                any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        assertAll(
                () -> assertThrows(OneTimeTokenInvalidException.class, () -> accountService.confirmOwnEmailChange("wrong", "password", "newtest@testownemail.com")),
                () -> verifyNoInteractions(accountRepository),
                () -> verifyNoInteractions(passwordEncoder)
        );
    }

    @Test
    @DisplayName("Test block function if provided password is invalid")
    void NEG03_changeOwnEmailUnit() {
        String generatedUrl = UUID.randomUUID().toString();

        OneTimeTokenEnt tokenEntity = new OneTimeTokenEnt();
        tokenEntity.setTokenValue(generatedUrl);
        tokenEntity.setUsername("ownemail_tester");

        when(tokenRepository.findByTokenValueAndTokenTypeAndIsUsedIsFalseAndExpiresAtAfter(
                eq(generatedUrl),
                eq(OneTimeTokenType.OWN_EMAIL_CHANGE),
                any(LocalDateTime.class)))
                .thenReturn(Optional.of(tokenEntity));
        when(accountRepository.findByLogin("ownemail_tester"))
                .thenReturn(Optional.of(testAccount));
        when(passwordEncoder.matches("wrongPassword", "testcontainers_hash"))
                .thenReturn(false);

        assertAll(
                () -> assertThrows(InvalidCredentialsProvidedException.class, () -> accountService.confirmOwnEmailChange(generatedUrl, "wrongPassword", "newtest@testownemail.com")),
                () -> verify(accountRepository, never()).saveAndFlush(any())
        );
    }

    @Test
    @DisplayName("Test create url function if optimistic lock fails")
    void NEG04_changeOwnEmailUnit() {
        when(accountRepository.findByLogin("ownemail_tester")).thenReturn(Optional.of(testAccount));
        when(versionHashService.hashObjectVersion(accountId, testAccount.getVersion())).thenReturn(validHash);

        assertAll(
                () -> assertThrows(ConcurrentUpdateException.class, () -> accountService.createUrlOwnEmailChange("ownemail_tester", "invalid-hash")),
                () -> verifyNoInteractions(tokenRepository),
                () -> verifyNoInteractions(emailService)
        );
    }
}