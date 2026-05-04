package pl.lodz.p.it.ssbd2026.ssbd01.mok.schedulers;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.account.unverified.expiration.hours:24}")
    private int expirationHours;

    @Scheduled(fixedDelayString = "${app.scheduler.check.interval.ms}")
    @Transactional(transactionManager = "mokTransactionManager")
    public void processInactiveAccounts() {
        LocalDateTime now = LocalDateTime.now();
        int reminderHours = expirationHours / 2;

        accountRepository.findAllByIsVerifiedFalseAndSecondEmailSentFalseAndCreationDateBefore(now.minusHours(reminderHours))
                .forEach(acc -> {
                    emailService.sendActivationReminderEmail(acc.getEmail(), acc.getActivationToken());
                    acc.setSecondEmailSent(true);
                });

        accountRepository.findAllByIsVerifiedFalseAndCreationDateBefore(now.minusHours(expirationHours))
                .forEach(acc -> {
                    emailService.sendAccountDeletedEmail(acc.getEmail());
                    accountRepository.delete(acc);
                });
    }
}
