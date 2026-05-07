package pl.lodz.p.it.ssbd2026.ssbd01.mok.schedulers;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.EmailService;

import java.time.LocalDateTime;
import java.util.List;

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
                    String lang = acc.getLanguageVersion().getLanguage();
                    emailService.sendActivationReminderEmail(acc.getEmail(), acc.getActivationToken(), lang);
                    acc.setSecondEmailSent(true);
                });

        accountRepository.findAllByIsVerifiedFalseAndCreationDateBefore(now.minusHours(expirationHours))
                .forEach(acc -> {
                    String lang = acc.getLanguageVersion().getLanguage();
                    emailService.sendAccountDeletedEmail(acc.getEmail(), lang);
                    accountRepository.delete(acc);
                });
    }

    @Scheduled(fixedDelayString = "${app.scheduler.check.interval.ms}")
    @Transactional(transactionManager = "mokTransactionManager")
    public void unblockAccountsAfter24Hours() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        List<Account> accountsToUnblock = accountRepository
                .findByIsBlockedTrueAndFailedAttemptsGreaterThanEqualAndLastLoginFailureDateTimeBefore(3, threshold);

        accountsToUnblock.forEach(acc -> {
            String lang = acc.getLanguageVersion().getLanguage();
            acc.setIsBlocked(false);
            acc.setFailedAttempts(0);
            emailService.sendAccountIsUnblockedInfo(acc.getEmail(), acc.getLogin(), lang);
        });

        accountRepository.saveAllAndFlush(accountsToUnblock);
    }
}
