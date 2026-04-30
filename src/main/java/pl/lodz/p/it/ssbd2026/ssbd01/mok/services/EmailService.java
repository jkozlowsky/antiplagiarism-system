package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;

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

    public void sendEmailChangedNotification(String toAddress, Account account) {
        String mailSubject = "Your email address has been changed by administrator.";
        String mailBody = "Dear " + account.getName() +
                ",\n\nWe are writing to inform you that an administrator has updated your email address.\n" +
                "If you did not request this change, please contact the administrator immediately.\n\n" +
                "Best regards,\n"; //+ "Donose60"

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("252929@edu.p.lodz.pl");
        message.setTo(toAddress);
        message.setSubject(mailSubject);
        message.setText(mailBody);

        mailSender.send(message);
    }
}
