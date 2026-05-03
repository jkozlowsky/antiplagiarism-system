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
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.OneTimeTokenConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.OneTimeTokenEnt;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccountNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.InvalidCredentialsProvidedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.OneTimeTokenInvalidException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.OneTimeTokenRepository;

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

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;
    private final String FRONTEND_URL = "http://localhost:5173";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(accountService, "frontendUrl", FRONTEND_URL);

        testAccount = new Account();
        testAccount.setLogin("ownemail_tester");
        testAccount.setEmail("test@testownemail.com");
        testAccount.setPasswordHash("testcontainers_hash");
    }

    @Test
    @DisplayName("Test successful work of own email change function - create url")
    void POS01_changeOwnEmailUnit() {
        when(accountRepository.findByLogin("ownemail_tester")).thenReturn(Optional.of(testAccount));

        accountService.createUrlOwnEmailChange("ownemail_tester");

        ArgumentCaptor<OneTimeTokenEnt> tokenCaptor = ArgumentCaptor.forClass(OneTimeTokenEnt.class);
        verify(tokenRepository, times(1)).saveAndFlush(tokenCaptor.capture());
        OneTimeTokenEnt savedToken = tokenCaptor.getValue();

        assertAll(
                () -> assertEquals("ownemail_tester", savedToken.getUsername()),
                () -> assertFalse(savedToken.isUsed()),
                () -> assertNotNull(savedToken.getTokenValue()),
                () -> verify(emailService, times(1)).sendUrlToChangeOwnEmail(
                        eq("test@testownemail.com"),
                        eq("ownemail_tester"),
                        contains(FRONTEND_URL + "/change-email?token=")
                )
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

        when(tokenRepository.findByTokenValueAndIsUsedIsFalseAndExpiresAtAfter(eq(generatedUrl), any(LocalDateTime.class)))
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
                () -> assertThrows(AccountNotFoundException.class, () -> accountService.createUrlOwnEmailChange("no_existing_user")),
                () -> verifyNoInteractions(tokenRepository),
                () -> verifyNoInteractions(emailService)
        );
    }

    @Test
    @DisplayName("Test block function if token is invalid")
    void NEG02_changeOwnEmailUnit() {
        when(tokenRepository.findByTokenValueAndIsUsedIsFalseAndExpiresAtAfter(anyString(), any(LocalDateTime.class)))
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

        when(tokenRepository.findByTokenValueAndIsUsedIsFalseAndExpiresAtAfter(eq(generatedUrl), any(LocalDateTime.class)))
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
}