package pl.lodz.p.it.ssbd2026.ssbd01.mok.schedulers;


import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.EmailService;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AccountScheduler {
    private final AccountRepository accountRepository;
    private final EmailService emailService;

    @Scheduled(fixedDelayString = "${app.scheduler.check.interval.ms}")
    @Transactional(transactionManager = "mokTransactionManager")
    public void processInactiveAccounts() {
        LocalDateTime now = LocalDateTime.now();

        accountRepository.findAllByIsVerifiedFalseAndSecondEmailSentFalseAndCreationDateBefore(now.minusHours(12))
                .forEach(acc -> {
                    emailService.sendActivationReminderEmail(acc.getEmail(), acc.getActivationToken());
                    acc.setSecondEmailSent(true);
                });

        accountRepository.findAllByIsVerifiedFalseAndCreationDateBefore(now.minusHours(24))
                .forEach(acc -> {
                    emailService.sendAccountDeletedEmail(acc.getEmail());
                    accountRepository.delete(acc);
                });
    }
}
