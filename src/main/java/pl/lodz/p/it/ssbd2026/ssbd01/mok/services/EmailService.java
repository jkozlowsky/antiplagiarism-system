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
        SimpleMailMessage massage = new SimpleMailMessage();
        massage.setFrom("no-reply@team-1.proj-sum.it.p.lodz.pl");
        massage.setTo(toAddress);
        massage.setSubject(subject);
        massage.setText(content);

        mailSender.send(massage);
    }

}
