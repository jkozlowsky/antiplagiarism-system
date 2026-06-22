package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.ssbd2026.ssbd01.config.i18n.Translator;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.email_messages.*;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.clients.EmailServiceClient;

import java.util.List;


@RequiredArgsConstructor
@Service
@PreAuthorize("permitAll()")
@Slf4j
public class EmailRestClient {

    private final EmailServiceClient emailClient;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final Translator translator;

    private static final List<String> BYPASS_EMAILS = List.of(
            "test_admin@ssbd.edu.pl",
            "test_teacher@ssbd.edu.pl",
            "test_student@ssbd.edu.pl"
    );

    private boolean isBypassed(String toAddress) {
        if (toAddress != null && BYPASS_EMAILS.contains(toAddress)) {
            log.info("[DRY BYPASS] Pominięto wysyłanie zadania e-mail przez sieć dla konta testowego: {}", toAddress);
            return true;
        }
        return false;
    }

    @Async
    public void sendActivationEmail(String toAddress, String token, String langCode) {
        log.warn("------------frontend-url--------------");
        log.warn(frontendUrl);
        if (isBypassed(toAddress)) return;
        emailClient.sendActivationEmail(
                new ActivationEmailRequest(toAddress, token, langCode, frontendUrl)
        );
    }

    @PreAuthorize("permitAll()")
    public void sendActivationReminderEmail(String toAddress, String token, String langCode) {
        if (isBypassed(toAddress)) return;
        emailClient.sendActivationReminderEmail(
                new ActivationEmailRequest(toAddress, token, langCode, frontendUrl)
        );
    }

    @Async
    public void sendAccountDeletedEmail(String toAddress, String langCode) {
        if (isBypassed(toAddress)) return;
        emailClient.sendAccountDeletedEmail(new BasicEmailRequest(toAddress, langCode));
    }

    @Async
    public void sendEmailAddressChangedByAdmin(String toAddress, String langCode) {
        if (isBypassed(toAddress)) return;
        emailClient.sendEmailAddressChangedByAdmin(new BasicEmailRequest(toAddress, langCode));
    }

    @Async
    public void sendAccountIsBlockedInfo(String toAddress, String login, String langCode) {
        if (isBypassed(toAddress)) return;
        emailClient.sendAccountIsBlockedInfo(new LoginEmailRequest(toAddress, langCode, login));
    }

    @Async
    public void sendAccountIsUnblockedInfo(String toAddress, String login, String langCode) {
        if (isBypassed(toAddress)) return;
        emailClient.sendAccountIsUnblockedInfo(new LoginEmailRequest(toAddress, langCode, login));
    }

    @Async
    public void sendUrlToChangeOwnEmail(String toAddress, String login, String url, String langCode) {
        if (isBypassed(toAddress)) return;
        emailClient.sendUrlToChangeOwnEmail(new UrlEmailRequest(toAddress, langCode, login, url));
    }

    @Async
    public void sendAccessLevelRevokedInfo(String toAddress, String login, String accessLevelName, String langCode) {
        if (isBypassed(toAddress)) return;
        emailClient.sendAccessLevelRevokedInfo(new AccessLevelEmailRequest(toAddress, langCode, login, accessLevelName));
    }

    @Async
    public void sendAccessLevelGrantedInfo(String toAddress, String login, String accessLevelName, String langCode) {
        if (isBypassed(toAddress)) return;
        emailClient.sendAccessLevelGrantedInfo(new AccessLevelEmailRequest(toAddress, langCode, login, accessLevelName));
    }

    @Async
    public void sendResetPasswordEmail(String toAddress, String token, String langCode) {
        if (isBypassed(toAddress)) return;
        emailClient.sendResetPasswordEmail(new ResetPasswordEmailRequest(toAddress, token, langCode, frontendUrl));
    }

    @Async
    public void sendAutoBlockEmail(String toAddress, String login, String langCode) {
        if (isBypassed(toAddress)) return;
        emailClient.sendAutoBlockEmail(new LoginEmailRequest(toAddress, langCode, login));
    }

    @Async
    public void sendAdminLoginNotification(String toAddress, String login, String ipAddress, String langCode) {
        if (isBypassed(toAddress)) return;
        emailClient.sendAdminLoginNotification(new AdminLoginEmailRequest(toAddress, langCode, login, ipAddress));
    }

    @Async
    public void send2FAuthEmail(String auth2F, String toAddress, String langCode) {
        if (isBypassed(toAddress)) return;
        emailClient.send2FAuthEmail(new TwoFactorAuthEmailRequest(toAddress, auth2F, langCode));
    }

    @Async
    public void sendComebackEmailAddressLink(String toAddress, String login, String newEmail, String url, String langCode) {
        if (isBypassed(toAddress)) return;
        emailClient.sendComebackEmailAddressLink(new RevertEmailRequest(toAddress, langCode, login, newEmail, url));
    }

    @Async
    public void sendUnblockEmailLinkAfterInactivity(String toAddress, String login, String token, String langCode) {
        if (isBypassed(toAddress)) return;
        emailClient.sendUnblockEmailLinkAfterInactivity(new UnblockInactiveEmailRequest(toAddress, langCode, login, token, frontendUrl));
    }

    @Async
    public void sendEmailOnlyAuthCode(String toAddress, String authCode, String langCode) {
        if (isBypassed(toAddress)) return;
        emailClient.sendEmailOnlyAuthCode(new AuthCodeEmailRequest(toAddress, langCode, authCode));
    }
}

