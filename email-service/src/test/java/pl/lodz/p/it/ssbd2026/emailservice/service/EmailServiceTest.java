package pl.lodz.p.it.ssbd2026.emailservice.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pl.lodz.p.it.ssbd2026.emailservice.config.i18n.Translator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private Translator translator;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private EmailService emailService;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        mimeMessage = mock(MimeMessage.class);
    }

    @Test
    @DisplayName("Pomyślne wygenerowanie i wysłanie e-maila aktywacyjnego")
    void shouldSendActivationEmailSuccessfully() {
        String toAddress = "user@example.com";
        String token = "abc-123";
        String langCode = "pl";
        String frontendUrl = "http://localhost:3000";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(translator.translateForUser("email.activation.subject", langCode))
                .thenReturn("Aktywacja Konta");
        when(translator.translateForUser("email.activation.content", langCode, frontendUrl, token))
                .thenReturn("Kliknij link");
        when(templateEngine.process(eq("email-layout"), any(Context.class)))
                .thenReturn("<html>Aktywacja Konta</html>");

        emailService.sendActivationEmail(toAddress, token, langCode, frontendUrl);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);

        assertAll(
                () -> verify(templateEngine, times(1)).process(eq("email-layout"), contextCaptor.capture()),
                () -> verify(mailSender, times(1)).send(mimeMessage),
                () -> assertEquals("Aktywacja Konta", contextCaptor.getValue().getVariable("subject")),
                () -> assertEquals("Kliknij link", contextCaptor.getValue().getVariable("content"))
        );
    }

    @Test
    @DisplayName("Pomyślne pominięcie wysyłki e-maila dla systemowych kont testowych")
    void shouldBypassEmailSendingForTestAccounts() {
        String testAddress = "test_admin@ssbd.edu.pl";

        emailService.sendEmail(testAddress, "Temat testowy", "Treść testowa");

        assertAll(
                () -> verify(mailSender, never()).createMimeMessage(),
                () -> verify(mailSender, never()).send(any(MimeMessage.class)),
                () -> verify(templateEngine, never()).process(anyString(), any(Context.class))
        );
    }
}