package pl.lodz.p.it.ssbd2026.ssbd01.mok.schedulers;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.OneTimeTokenRepository;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TokenScheduler {

    private final OneTimeTokenRepository tokenRepository;

    @Scheduled(fixedDelayString = "${app.scheduler.check.interval.ms}")
    @Transactional(propagation = Propagation.REQUIRES_NEW,  readOnly = false, transactionManager = "mokTransactionManager")
    public void DeleteExpiredTokens() {
        tokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }


}
