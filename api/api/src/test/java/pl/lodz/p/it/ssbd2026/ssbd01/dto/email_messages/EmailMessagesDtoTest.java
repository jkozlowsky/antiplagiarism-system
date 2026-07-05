package pl.lodz.p.it.ssbd2026.ssbd01.dto.email_messages;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EmailMessagesDtoTest {

    @Test
    void shouldCoverAccessLevelEmailRequest() {
        AccessLevelEmailRequest dto1 = new AccessLevelEmailRequest("test@ex.com", "pl", "login1", "ADMIN");
        AccessLevelEmailRequest dto2 = new AccessLevelEmailRequest("test@ex.com", "pl", "login1", "ADMIN");

        assertAll(
                () -> assertEquals("test@ex.com", dto1.toAddress()),
                () -> assertEquals("pl", dto1.langCode()),
                () -> assertEquals("login1", dto1.login()),
                () -> assertEquals("ADMIN", dto1.accessLevelName()),
                () -> assertEquals(dto1, dto2),
                () -> assertEquals(dto1.hashCode(), dto2.hashCode()),
                () -> assertNotNull(dto1.toString())
        );
    }

    @Test
    void shouldCoverAdminLoginEmailRequest() {
        AdminLoginEmailRequest dto1 = new AdminLoginEmailRequest("test@ex.com", "pl", "login1", "127.0.0.1");
        AdminLoginEmailRequest dto2 = new AdminLoginEmailRequest("test@ex.com", "pl", "login1", "127.0.0.1");

        assertAll(
                () -> assertEquals("test@ex.com", dto1.toAddress()),
                () -> assertEquals("pl", dto1.langCode()),
                () -> assertEquals("login1", dto1.login()),
                () -> assertEquals("127.0.0.1", dto1.ipAddress()),
                () -> assertEquals(dto1, dto2),
                () -> assertEquals(dto1.hashCode(), dto2.hashCode()),
                () -> assertNotNull(dto1.toString())
        );
    }

    @Test
    void shouldCoverAuthCodeEmailRequest() {
        AuthCodeEmailRequest dto1 = new AuthCodeEmailRequest("test@ex.com", "pl", "CODE123");
        AuthCodeEmailRequest dto2 = new AuthCodeEmailRequest("test@ex.com", "pl", "CODE123");

        assertAll(
                () -> assertEquals("test@ex.com", dto1.toAddress()),
                () -> assertEquals("pl", dto1.langCode()),
                () -> assertEquals("CODE123", dto1.authCode()),
                () -> assertEquals(dto1, dto2),
                () -> assertEquals(dto1.hashCode(), dto2.hashCode()),
                () -> assertNotNull(dto1.toString())
        );
    }

    @Test
    void shouldCoverLoginEmailRequest() {
        LoginEmailRequest dto1 = new LoginEmailRequest("test@ex.com", "pl", "log");
        LoginEmailRequest dto2 = new LoginEmailRequest("test@ex.com", "pl", "log");

        assertAll(
                () -> assertEquals("test@ex.com", dto1.toAddress()),
                () -> assertEquals("pl", dto1.langCode()),
                () -> assertEquals("log", dto1.login()),
                () -> assertEquals(dto1, dto2),
                () -> assertEquals(dto1.hashCode(), dto2.hashCode()),
                () -> assertNotNull(dto1.toString())
        );
    }

    @Test
    void shouldCoverResetPasswordEmailRequest() {
        ResetPasswordEmailRequest dto1 = new ResetPasswordEmailRequest("test@ex.com", "tok", "pl", "http://url");
        ResetPasswordEmailRequest dto2 = new ResetPasswordEmailRequest("test@ex.com", "tok", "pl", "http://url");

        assertAll(
                () -> assertEquals("test@ex.com", dto1.toAddress()),
                () -> assertEquals("tok", dto1.token()),
                () -> assertEquals("pl", dto1.langCode()),
                () -> assertEquals("http://url", dto1.frontendUrl()),
                () -> assertEquals(dto1, dto2),
                () -> assertEquals(dto1.hashCode(), dto2.hashCode()),
                () -> assertNotNull(dto1.toString())
        );
    }

    @Test
    void shouldCoverRevertEmailRequest() {
        RevertEmailRequest dto1 = new RevertEmailRequest("test@ex.com", "pl", "log", "new@ex.com", "http://url");
        RevertEmailRequest dto2 = new RevertEmailRequest("test@ex.com", "pl", "log", "new@ex.com", "http://url");

        assertAll(
                () -> assertEquals("test@ex.com", dto1.toAddress()),
                () -> assertEquals("pl", dto1.langCode()),
                () -> assertEquals("log", dto1.login()),
                () -> assertEquals("new@ex.com", dto1.newEmail()),
                () -> assertEquals("http://url", dto1.url()),
                () -> assertEquals(dto1, dto2),
                () -> assertEquals(dto1.hashCode(), dto2.hashCode()),
                () -> assertNotNull(dto1.toString())
        );
    }

    @Test
    void shouldCoverTwoFactorAuthEmailRequest() {
        TwoFactorAuthEmailRequest dto1 = new TwoFactorAuthEmailRequest("test@ex.com", "2FA", "pl");
        TwoFactorAuthEmailRequest dto2 = new TwoFactorAuthEmailRequest("test@ex.com", "2FA", "pl");

        assertAll(
                () -> assertEquals("test@ex.com", dto1.toAddress()),
                () -> assertEquals("2FA", dto1.auth2F()),
                () -> assertEquals("pl", dto1.langCode()),
                () -> assertEquals(dto1, dto2),
                () -> assertEquals(dto1.hashCode(), dto2.hashCode()),
                () -> assertNotNull(dto1.toString())
        );
    }

    @Test
    void shouldCoverUnblockInactiveEmailRequest() {
        UnblockInactiveEmailRequest dto1 = new UnblockInactiveEmailRequest("test@ex.com", "pl", "log", "tok", "url");
        UnblockInactiveEmailRequest dto2 = new UnblockInactiveEmailRequest("test@ex.com", "pl", "log", "tok", "url");

        assertAll(
                () -> assertEquals("test@ex.com", dto1.toAddress()),
                () -> assertEquals("pl", dto1.langCode()),
                () -> assertEquals("log", dto1.login()),
                () -> assertEquals("tok", dto1.token()),
                () -> assertEquals("url", dto1.frontendUrl()),
                () -> assertEquals(dto1, dto2),
                () -> assertEquals(dto1.hashCode(), dto2.hashCode()),
                () -> assertNotNull(dto1.toString())
        );
    }

    @Test
    void shouldCoverUrlEmailRequest() {
        UrlEmailRequest dto1 = new UrlEmailRequest("test@ex.com", "pl", "log", "url");
        UrlEmailRequest dto2 = new UrlEmailRequest("test@ex.com", "pl", "log", "url");

        assertAll(
                () -> assertEquals("test@ex.com", dto1.toAddress()),
                () -> assertEquals("pl", dto1.langCode()),
                () -> assertEquals("log", dto1.login()),
                () -> assertEquals("url", dto1.url()),
                () -> assertEquals(dto1, dto2),
                () -> assertEquals(dto1.hashCode(), dto2.hashCode()),
                () -> assertNotNull(dto1.toString())
        );
    }
}