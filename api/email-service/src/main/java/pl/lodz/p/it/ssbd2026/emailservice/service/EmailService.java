package pl.lodz.p.it.ssbd2026.emailservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import pl.lodz.p.it.ssbd2026.emailservice.config.i18n.Translator;
import org.thymeleaf.context.Context;

import java.util.List;


@RequiredArgsConstructor
@Service
//@PreAuthorize("permitAll()")
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

//    @Value("${app.frontend.url}")
//    private String frontendUrl;

    private final Translator translator;

    private final TemplateEngine templateEngine;

    private static final List<String> BYPASS_EMAILS = List.of(
            "test_admin@ssbd.edu.pl",
            "test_teacher@ssbd.edu.pl",
            "test_student@ssbd.edu.pl"
    );

    //    @PreAuthorize("permitAll()")
    public void sendEmail(String toAddress, String subject, String content) {
        if (toAddress != null && BYPASS_EMAILS.contains(toAddress)) {
            log.info("[DRY BYPASS] Pominięto fizyczne wysyłanie wiadomości e-mail dla konta testowego: {}", toAddress);
            return;
        }

        try {
            // 2. Prepare variables for the HTML template
            Context context = new Context();
            context.setVariable("subject", subject);
            // Replace \n with <br/> so plain text properties files look good in HTML
            context.setVariable("content", content.replace("\n", "<br/>"));

            // 3. Process the HTML file (looks for email-layout.html in resources/templates/)
            String htmlContent = templateEngine.process("email-layout", context);

            // 4. Send the email as HTML
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("ssbd01@athame.pl");
            helper.setTo(toAddress);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(message);

        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}", toAddress, e);
        }
    }

    //    @PreAuthorize("permitAll()")
    public void sendActivationEmail(String toAddress, String token, String langCode, String frontendUrl) {
        String subject = translator.translateForUser("email.activation.subject", langCode);
        String content = translator.translateForUser("email.activation.content", langCode, frontendUrl, token);
        sendEmail(toAddress, subject, content);
    }

    //    @PreAuthorize("permitAll()")
    public void sendActivationReminderEmail(String toAddress, String token, String langCode, String frontendUrl) {
        String subject = translator.translateForUser("email.activation.reminder.subject", langCode);
        String content = translator.translateForUser("email.activation.reminder.content", langCode, frontendUrl, token);
        sendEmail(toAddress, subject, content);
    }

    //    @PreAuthorize("permitAll()")
    public void sendAccountDeletedEmail(String toAddress, String langCode) {
        String subject = translator.translateForUser("email.account.deleted.subject", langCode);
        String content = translator.translateForUser("email.account.deleted.content", langCode);
        sendEmail(toAddress, subject, content);
    }

    //    @PreAuthorize("permitAll()")
    @Async
    public void sendEmailAddressChangedByAdmin(String toAddress, String langCode) {
        String subject = translator.translateForUser("email.email.changed.subject", langCode);
        String content = translator.translateForUser("email.email.changed.content", langCode);
        sendEmail(toAddress, subject, content);
    }

    //    @PreAuthorize("permitAll()")
    public void sendAccountIsBlockedInfo(String toAddress, String login, String langCode) {
        String subject = translator.translateForUser("email.account.blocked.subject", langCode);
        String content = translator.translateForUser("email.account.blocked.content", langCode, login);
        sendEmail(toAddress, subject, content);
    }

    //    @PreAuthorize("permitAll()")
    public void sendAccountIsUnblockedInfo(String toAddress, String login, String langCode) {
        String subject = translator.translateForUser("email.account.unblocked.subject", langCode);
        String content = translator.translateForUser("email.account.unblocked.content", langCode, login);
        sendEmail(toAddress, subject, content);
    }

    //    @PreAuthorize("permitAll()")
    public void sendUrlToChangeOwnEmail(String toAddress, String login, String url, String langCode) {
        String subject = translator.translateForUser("email.email.change.request.subject", langCode);
        String content = translator.translateForUser("email.email.change.request.content", langCode, login, url);
        sendEmail(toAddress, subject, content);
    }

    //    @PreAuthorize("permitAll()")
    @Async
    public void sendAccessLevelRevokedInfo(String toAddress, String login, String accessLevelName, String langCode) {
        String subject = translator.translateForUser("email.accesslevel.revoked.subject", langCode);
        String content = translator.translateForUser("email.accesslevel.revoked.content", langCode, login, accessLevelName);
        sendEmail(toAddress, subject, content);
    }

    //    @PreAuthorize("permitAll()")
    public void sendAccessLevelGrantedInfo(String toAddress, String login, String accessLevelName, String langCode) {
        String subject = translator.translateForUser("email.accesslevel.granted.subject", langCode);
        String content = translator.translateForUser("email.accesslevel.granted.content", langCode, login, accessLevelName);
        sendEmail(toAddress, subject, content);
    }

    //    @PreAuthorize("permitAll()")
    public void sendResetPasswordEmail(String toAddress, String token, String langCode, String frontendUrl) {
        String magicLink = frontendUrl + "/reset-password?token=" + token;
        String subject = translator.translateForUser("email.password.reset.subject", langCode);
        String content = translator.translateForUser("email.password.reset.content", langCode, magicLink);
        sendEmail(toAddress, subject, content);
    }

    //    @PreAuthorize("permitAll()")
    @Async
    public void sendAutoBlockEmail(String toAddress, String login, String langCode) {
        String subject = translator.translateForUser("email.autoblock.subject", langCode);
        String content = translator.translateForUser("email.autoblock.content", langCode, login);
        sendEmail(toAddress, subject, content);
    }

    //    @PreAuthorize("permitAll()")
    @Async
    public void sendAdminLoginNotification(String toAddress, String login, String ipAddress, String langCode) {
        String subject = translator.translateForUser("email.admin.login.subject", langCode);
        String content = translator.translateForUser("email.admin.login.content", langCode, login, ipAddress);
        sendEmail(toAddress, subject, content);
    }

    //    @PreAuthorize("permitAll()")
    @Async
    public void send2FAuthEmail(String Auth2F, String toAddres, String langCode) {
        String subject = translator.translateForUser("email.2fa.subject", langCode);
        String content = translator.translateForUser("email.2fa.content", langCode, Auth2F);
        sendEmail(toAddres, subject, content);
    }

    //    @PreAuthorize("permitAll()")
    @Async
    public void sendComebackEmailAddressLink(String toAddress, String login, String newEmail, String url, String langCode) {
        String subject = translator.translateForUser("email.email.revert.subject", langCode);
        String content = translator.translateForUser("email.email.revert.content", langCode, login, newEmail, url);
        sendEmail(toAddress, subject, content);
    }

    @Async
    public void sendUnblockEmailLinkAfterInactivity(String toAddress, String login, String token, String langCode, String frontendUrl) {
        String url = frontendUrl + "/unblock-account?token=" + token;
        String subject = translator.translateForUser("email.account.inactive.unblock.subject", langCode);
        String content = translator.translateForUser("email.account.inactive.unblock.content", langCode, login, url);
        sendEmail(toAddress, subject, content);
    }

    public void sendEmailOnlyAuthCode(String toAddress, String authCode, String langCode) {
        String subject = translator.translateForUser("email.only.auth.subject", langCode);
        String content = translator.translateForUser("email.only.auth.content", langCode, authCode);
        sendEmail(toAddress, subject, content);
    }
}

