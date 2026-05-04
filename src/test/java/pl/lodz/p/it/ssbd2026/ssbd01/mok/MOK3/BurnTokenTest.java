package pl.lodz.p.it.ssbd2026.ssbd01.mok.MOK3;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.OneTimeTokenEnt;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.OneTimeTokenRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.schedulers.TokenScheduler;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class BurnTokenTest {
    @Mock
    private OneTimeTokenRepository tokenRepository;

    @InjectMocks
    private TokenScheduler tokenScheduler;

//    @Test
//    void POS01_burnToken_ShouldSetIsUsedToTrueAndSave() {
//        OneTimeTokenEnt token = new OneTimeTokenEnt();
//        token.setUsed(false);
//
//        tokenScheduler.DeleteExpiredTokens(token);
//
//        assertTrue(token.isUsed());
//        verify(tokenRepository).saveAndFlush(token);
//    }
}
