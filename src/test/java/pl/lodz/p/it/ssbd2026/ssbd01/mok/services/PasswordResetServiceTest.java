package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.OneTimeTokenEnt;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.OneTimeTokenRepository;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private OneTimeTokenRepository tokenRepository;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Test
    void posTestInitialiseResetPassword() {

        String email = "test@example.com";

        OneTimeTokenEnt validToken = new OneTimeTokenEnt();


//        passwordResetService.initiatePasswordReset(email);



    }
}