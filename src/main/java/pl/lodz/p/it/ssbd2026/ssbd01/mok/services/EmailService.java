package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public void sendEmail(String toAddress, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("252929@edu.p.lodz.pl");
        message.setTo(toAddress);
        message.setSubject(subject);
        message.setText(content);

        mailSender.send(message);
    }

    public void sendActivationEmail(String toAddress, String token) {
        String subject = "Aktywacja konta";
        String content = String.format("Kliknij w link aby aktywować: %s/activate?token=%s", frontendUrl, token);
        sendEmail(toAddress, subject, content);
    }

    public void sendActivationReminderEmail(String toAddress, String token) {
        String subject = "Przypomnienie o aktywacji";
        String content = String.format("Minęło 12h. Prosimy o aktywację: %s/activate?token=%s", frontendUrl, token);
        sendEmail(toAddress, subject, content);
    }

    public void sendAccountDeletedEmail(String toAddress) {
        String subject = "Konto usunięte";
        String content = "Twoje konto zostało usunięte z powodu braku aktywacji w ciągu 24h.";
        sendEmail(toAddress, subject, content);
    }

    @Async
    public void sendEmailAddressChangedByAdmin(String toAddress) {
        String mailSubject = "Your email address has been changed by administrator.";
        String mailBody = "Hello," +
                ",\n\nWe are writing to inform you that an administrator has updated your email address.\n" +
                "If you did not request this change, please contact the administrator immediately.\n\n" +
                "Best regards,\n"; //+ "Donose60"

        sendEmail(toAddress, mailSubject, mailBody);
    }

    public void sendAccountIsBlockedInfo(String toAddress, String login) {
        String subject = "Notification - Your account has been blocked";
        String content = String.format(
                "Hello %s,\n\nYour account has been blocked. For more information contact administrator.",
                login
        );

        sendEmail(toAddress, subject, content);
    }

    public void sendAccountIsUnblockedInfo(String toAddress, String login) {
        String subject = "Notification - Your account has been unblocked";
        String content = String.format(
                "Hello %s,\n\nYour account has been unblocked. You can now log into your account again.",
                login
        );

        sendEmail(toAddress, subject, content);
    }

    public void sendUrlToChangeOwnEmail(String toAddress, String login, String url) {
        String subject = "Notification - You have requested an email change";
        String content = String.format(
                "Hello %s,\n\nYou have requested an email change.\nPlease click the link below to confirm the change:\n%s\n\n",
                login, url
        );

        sendEmail(toAddress, subject, content);
    }

    @Async
    public void sendAccessLevelRevokedInfo(String toAddress, String login, String accessLevelName) {
        String subject = "Notification - Access Level Revoked";
        String content = String.format(
                "Hello %s,\n\nWe are writing to inform you that your access level has been revoked: %s.",
                login, accessLevelName
        );
        sendEmail(toAddress, subject, content);
    }
    
    public void sendAccessLevelGrantedInfo(String toAddress, String login, String accessLevelName) {
        String subject = "Notification - Access Level Granted";
        String content = String.format(
                "Hello %s,\n\nWe are writing to inform you that you have received a new access level: %s.",
                login, accessLevelName
        );
        sendEmail(toAddress, subject, content);
    }
}

