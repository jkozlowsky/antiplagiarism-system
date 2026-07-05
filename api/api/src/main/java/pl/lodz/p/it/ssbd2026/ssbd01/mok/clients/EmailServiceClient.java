package pl.lodz.p.it.ssbd2026.ssbd01.mok.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.email_messages.*;

@FeignClient(name = "email-service")
public interface EmailServiceClient {

    @PostMapping("/api/emails/activation")
    void sendActivationEmail(@RequestBody ActivationEmailRequest request);

    @PostMapping("/api/emails/activation-reminder")
    void sendActivationReminderEmail(@RequestBody ActivationEmailRequest request);

    @PostMapping("/api/emails/account-deleted")
    void sendAccountDeletedEmail(@RequestBody BasicEmailRequest request);

    @PostMapping("/api/emails/email-changed-by-admin")
    void sendEmailAddressChangedByAdmin(@RequestBody BasicEmailRequest request);

    @PostMapping("/api/emails/account-blocked")
    void sendAccountIsBlockedInfo(@RequestBody LoginEmailRequest request);

    @PostMapping("/api/emails/account-unblocked")
    void sendAccountIsUnblockedInfo(@RequestBody LoginEmailRequest request);

    @PostMapping("/api/emails/change-own-email")
    void sendUrlToChangeOwnEmail(@RequestBody UrlEmailRequest request);

    @PostMapping("/api/emails/access-level-revoked")
    void sendAccessLevelRevokedInfo(@RequestBody AccessLevelEmailRequest request);

    @PostMapping("/api/emails/access-level-granted")
    void sendAccessLevelGrantedInfo(@RequestBody AccessLevelEmailRequest request);

    @PostMapping("/api/emails/password-reset")
    void sendResetPasswordEmail(@RequestBody ResetPasswordEmailRequest request);

    @PostMapping("/api/emails/auto-block")
    void sendAutoBlockEmail(@RequestBody LoginEmailRequest request);

    @PostMapping("/api/emails/admin-login")
    void sendAdminLoginNotification(@RequestBody AdminLoginEmailRequest request);

    @PostMapping("/api/emails/2fa")
    void send2FAuthEmail(@RequestBody TwoFactorAuthEmailRequest request);

    @PostMapping("/api/emails/email-revert")
    void sendComebackEmailAddressLink(@RequestBody RevertEmailRequest request);

    @PostMapping("/api/emails/unblock-inactivity")
    void sendUnblockEmailLinkAfterInactivity(@RequestBody UnblockInactiveEmailRequest request);

    @PostMapping("/api/emails/auth-code")
    void sendEmailOnlyAuthCode(@RequestBody AuthCodeEmailRequest request);
}