package pl.lodz.p.it.ssbd2026.emailservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.ssbd2026.emailservice.dto.*;
import pl.lodz.p.it.ssbd2026.emailservice.service.EmailService;

@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/activation")
    public void handleActivationEmail(@RequestBody ActivationEmailRequest request) {
        emailService.sendActivationEmail(request.toAddress(), request.token(), request.langCode(), request.frontendUrl());
    }

    @PostMapping("/activation-reminder")
    public void handleActivationReminderEmail(@RequestBody ActivationEmailRequest request) {
        emailService.sendActivationReminderEmail(request.toAddress(), request.token(), request.langCode(), request.frontendUrl());
    }

    @PostMapping("/account-deleted")
    public void handleAccountDeletedEmail(@RequestBody BasicEmailRequest request) {
        emailService.sendAccountDeletedEmail(request.toAddress(), request.langCode());
    }

    @PostMapping("/email-changed-by-admin")
    public void handleEmailAddressChangedByAdmin(@RequestBody BasicEmailRequest request) {
        emailService.sendEmailAddressChangedByAdmin(request.toAddress(), request.langCode());
    }

    @PostMapping("/account-blocked")
    public void handleAccountIsBlockedInfo(@RequestBody LoginEmailRequest request) {
        // POPRAWKA: Zamieniono miejscami login i langCode
        emailService.sendAccountIsBlockedInfo(request.toAddress(), request.login(), request.langCode());
    }

    @PostMapping("/account-unblocked")
    public void handleAccountIsUnblockedInfo(@RequestBody LoginEmailRequest request) {
        // POPRAWKA: Zamieniono miejscami login i langCode
        emailService.sendAccountIsUnblockedInfo(request.toAddress(), request.login(), request.langCode());
    }

    @PostMapping("/change-own-email")
    public void handleUrlToChangeOwnEmail(@RequestBody UrlEmailRequest request) {
        emailService.sendUrlToChangeOwnEmail(request.toAddress(), request.login(), request.url(), request.langCode());
    }

    @PostMapping("/access-level-revoked")
    public void handleAccessLevelRevokedInfo(@RequestBody AccessLevelEmailRequest request) {
        emailService.sendAccessLevelRevokedInfo(request.toAddress(), request.login(), request.accessLevelName(), request.langCode());
    }

    @PostMapping("/access-level-granted")
    public void handleAccessLevelGrantedInfo(@RequestBody AccessLevelEmailRequest request) {
        emailService.sendAccessLevelGrantedInfo(request.toAddress(), request.login(), request.accessLevelName(), request.langCode());
    }

    @PostMapping("/password-reset")
    public void handleResetPasswordEmail(@RequestBody ResetPasswordEmailRequest request) {
        emailService.sendResetPasswordEmail(request.toAddress(), request.token(), request.langCode(), request.frontendUrl());
    }

    @PostMapping("/auto-block")
    public void handleAutoBlockEmail(@RequestBody LoginEmailRequest request) {
        emailService.sendAutoBlockEmail(request.toAddress(), request.login(), request.langCode());
    }

    @PostMapping("/admin-login")
    public void handleAdminLoginNotification(@RequestBody AdminLoginEmailRequest request) {
        emailService.sendAdminLoginNotification(request.toAddress(), request.login(), request.ipAddress(), request.langCode());
    }

    @PostMapping("/2fa")
    public void handle2FAuthEmail(@RequestBody TwoFactorAuthEmailRequest request) {
        emailService.send2FAuthEmail(request.auth2F(), request.toAddress() ,request.langCode());
    }

    @PostMapping("/email-revert")
    public void handleComebackEmailAddressLink(@RequestBody RevertEmailRequest request) {
        emailService.sendComebackEmailAddressLink(request.toAddress(), request.login(), request.newEmail(), request.url(), request.langCode());
    }

    @PostMapping("/unblock-inactivity")
    public void handleUnblockEmailLinkAfterInactivity(@RequestBody UnblockInactiveEmailRequest request) {
        emailService.sendUnblockEmailLinkAfterInactivity(request.toAddress(), request.login(), request.token(), request.langCode(), request.frontendUrl());
    }

    @PostMapping("/auth-code")
    public void handleEmailOnlyAuthCode(@RequestBody AuthCodeEmailRequest request) {
        emailService.sendEmailOnlyAuthCode(request.toAddress(), request.authCode(), request.langCode());
    }
}