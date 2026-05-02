package pl.lodz.p.it.ssbd2026.ssbd01.mok.MOK3;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.OneTimeTokenEnt;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccountNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.OneTimeTokenInvalidException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.OneTimeTokenRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.EmailService;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.EmailTokenService;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.PasswordResetService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.CommonFunctions;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ResolvePasswordResetTest{

    @Mock
    private OneTimeTokenRepository tokenRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailTokenService tokenService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Captor
    private ArgumentCaptor<OneTimeTokenEnt> tokenCaptor;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    private final CommonFunctions commonFunctions = new CommonFunctions();

    @Test
    void NEG01_resolvePasswordReset_ShouldThrowException_WhenTokenIsInvalidOrExpired() {
        when(tokenRepository.findByTokenValueAndIsUsedIsFalseAndExpiresAtAfter(anyString(), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        assertThrows(OneTimeTokenInvalidException.class, () ->
                passwordResetService.resolvePasswordReset(
                        commonFunctions.generateRandomStringOfLength(12),
                        commonFunctions.generateRandomStringOfLength(12)
                )
        );
        verifyNoInteractions(accountRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void NEG02_resolvePasswordReset_ShouldThrowException_WhenAccountNotFound() {
        String email = commonFunctions.generateRandomEmail();

        OneTimeTokenEnt tokenEntity = new OneTimeTokenEnt();
        tokenEntity.setUsername(email);

        when(tokenRepository.findByTokenValueAndIsUsedIsFalseAndExpiresAtAfter(anyString(), any(LocalDateTime.class)))
                .thenReturn(Optional.of(tokenEntity));
        when(accountRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () ->
                passwordResetService.resolvePasswordReset(
                        commonFunctions.generateRandomStringOfLength(12),
                        commonFunctions.generateRandomStringOfLength(12)
                )
        );

        assertTrue(tokenEntity.isUsed());
    }

    @Test
    void POS01_resolvePasswordReset_ShouldSuccessfullyUpdatePassword_WhenValid() {
        String email = commonFunctions.generateRandomEmail();
        String rawPassword = commonFunctions.generateRandomStringOfLength(12);
        String hashedPassword = commonFunctions.generateRandomStringOfLength(16);

        OneTimeTokenEnt tokenEntity = new OneTimeTokenEnt();
        tokenEntity.setUsername(email);
        tokenEntity.setUsed(false);

        Account account = new Account();
//        account.setTokenVersion(1);
        account.setEmail(email);

        when(tokenRepository.findByTokenValueAndIsUsedIsFalseAndExpiresAtAfter(anyString(), any(LocalDateTime.class)))
                .thenReturn(Optional.of(tokenEntity));
        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(account));
        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);

        passwordResetService.resolvePasswordReset(
                commonFunctions.generateRandomStringOfLength(12),
                rawPassword
        );

        assertTrue(tokenEntity.isUsed(), "Token should be marked as used");

        assertEquals(hashedPassword, account.getPasswordHash(), "Password should be updated");
//        assertEquals(2, account.getTokenVersion(), "Token version should have been incremented");
    }

}