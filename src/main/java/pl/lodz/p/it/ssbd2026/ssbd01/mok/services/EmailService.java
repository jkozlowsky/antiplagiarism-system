package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender){
        this.mailSender = mailSender;
    }

    public void sendEmail(String toAddress, String subject, String content){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("252929@edu.p.lodz.pl");
        message.setTo(toAddress);
        message.setSubject(subject);
        message.setText(content);

        mailSender.send(message);
    }

    public void sendAccountIsBlockedInfo(String toAddress, String login) {
        String subject = "Notification - Your account has been blocked";
        String content = String.format(
                "Hello %s,\n\nYour account has been blocked. For more information contact administrator.",
                login
        );

        sendEmail(toAddress, subject, content);
    }
}
