package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.OneTimeTokenEnt;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.OneTimeTokenRepository;

@Service
@RequiredArgsConstructor
public class EmailTokenService {

    private final OneTimeTokenRepository tokenRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW,  readOnly = false, transactionManager = "mokTransactionManager")
    public void burnToken(OneTimeTokenEnt token) {
        token.setUsed(true);
        tokenRepository.saveAndFlush(token);
    }
}
