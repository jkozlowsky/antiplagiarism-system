package pl.lodz.p.it.ssbd2026.ssbd01.mok.MOK3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.OneTimeTokenEnt;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.OneTimeTokenRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.EmailService;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.PasswordResetService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.CommonFunctions;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;
@ExtendWith(MockitoExtension.class)
public class InitiatePasswordResetTest {

    @Mock
    private OneTimeTokenRepository tokenRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Captor
    private ArgumentCaptor<OneTimeTokenEnt> tokenCaptor;
    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    private final CommonFunctions commonFunctions = new CommonFunctions();

    private final String frontendUrl = "http://localhost:5173";

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(passwordResetService, "frontendUrl", frontendUrl);
    }

    @Test
    void NEG01_initiatePasswordReset_ShouldReturnEarly_WhenUserNotFound() {
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        passwordResetService.initiatePasswordReset(commonFunctions.generateRandomEmail());

        verifyNoInteractions(tokenRepository);
        verifyNoInteractions(emailService);
    }

    @Test
    void POS01_initiatePasswordReset_ShouldInvalidateOldTokensAndCreateNew_WhenUserFound() {
        String email = commonFunctions.generateRandomEmail();
        Account account = new Account();
        OneTimeTokenEnt oldToken = new OneTimeTokenEnt();
        oldToken.setUsed(false);

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(account));
        when(tokenRepository.findByUsernameAndIsUsedIsFalse(email)).thenReturn(List.of(oldToken));

        passwordResetService.initiatePasswordReset(email);

        assertTrue(oldToken.isUsed());
        verify(tokenRepository).saveAllAndFlush(anyList());

        verify(tokenRepository).saveAndFlush(tokenCaptor.capture());
        OneTimeTokenEnt savedToken = tokenCaptor.getValue();
        assertEquals(email, savedToken.getUsername());
        assertFalse(savedToken.isUsed());
        assertNotNull(savedToken.getTokenValue());

        verify(emailService).sendEmail(eq(email), eq("Password Reset"), contains(frontendUrl+"/reset-password?token="));
    }

    @Test
    void POS02_initiatePasswordReset_ShouldCreateToken_WhenNoPreviousTokensExist() {
        String email = commonFunctions.generateRandomEmail();
        Account account = new Account();

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(account));
        when(tokenRepository.findByUsernameAndIsUsedIsFalse(email)).thenReturn(List.of());

        passwordResetService.initiatePasswordReset(email);

        verify(tokenRepository, never()).saveAllAndFlush(anyList());

        verify(tokenRepository).saveAndFlush(tokenCaptor.capture());
        assertEquals(email, tokenCaptor.getValue().getUsername());
        assertFalse(tokenCaptor.getValue().isUsed());
    }

    @Test
    void POS03_initiatePasswordReset_ShouldNeverSaveRawTokenToDatabase() {
        String email = commonFunctions.generateRandomEmail();
        Account account = new Account();

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(account));
        when(tokenRepository.findByUsernameAndIsUsedIsFalse(email)).thenReturn(List.of());

        passwordResetService.initiatePasswordReset(email);

        verify(tokenRepository).saveAndFlush(tokenCaptor.capture());
        String savedDbToken = tokenCaptor.getValue().getTokenValue();

        ArgumentCaptor<String> emailBodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmail(eq(email), anyString(), emailBodyCaptor.capture());

        String emailBody = emailBodyCaptor.getValue();
        String rawTokenFromEmail = emailBody.substring(emailBody.indexOf("token=") + 6);

        assertNotEquals(rawTokenFromEmail, savedDbToken);
    }
}
