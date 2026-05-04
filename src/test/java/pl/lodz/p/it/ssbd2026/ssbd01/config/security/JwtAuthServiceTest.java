package pl.lodz.p.it.ssbd2026.ssbd01.config.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class JwtAuthServiceTest {
    private JwtAuthService jwtAuthService;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        jwtAuthService = new JwtAuthService(accountRepository);

        String testSecret = "n10rb1FUMXskFj1RV4PDQOmMvRtrhB9NBWeeGZ+fSGvCmMVIOMFqikGHy66Icdmka4pdXpmkpLtsdqSkSHQC1A==";
        ReflectionTestUtils.setField(jwtAuthService, "secret", testSecret);
        ReflectionTestUtils.setField(jwtAuthService, "expirationTime", 900000);

        jwtAuthService.init();
    }

    @Test
    void testGenerateToken() {
        String username = "user";
        List<String> roles = List.of("ADMIN");

        String token = jwtAuthService.generateToken(username, roles);

        assertNotNull(token);
        assertTrue(jwtAuthService.isTokenValid(token));

        String extractedUsername = jwtAuthService.extractUsername(token);
        List<String> extractedRoles = jwtAuthService.extractRoles(token);

        assertEquals(username, extractedUsername);
        assertEquals(extractedRoles, roles);
    }

    @Test
    void testRejectTamperedToken() {
        String username = "user";
        List<String> roles = List.of("ADMIN");

        String token = jwtAuthService.generateToken(username, roles);
        assertNotNull(token);

        token = token + "tamper";
        assertFalse(jwtAuthService.isTokenValid(token));
    }

    @Test
    void testExpiredToken() {
        ReflectionTestUtils.setField(jwtAuthService, "expirationTime", -1000L);

        String username = "user";
        List<String> roles = List.of("ADMIN");
        String expiredToken = jwtAuthService.generateToken(username, roles);

        assertFalse(jwtAuthService.isTokenValid(expiredToken));
    }

    @Test
    void testMalformedToken() {
        String gibberishToken = "bad.token";
        assertFalse(jwtAuthService.isTokenValid(gibberishToken));

        String emptyToken = "";
        assertFalse(jwtAuthService.isTokenValid(emptyToken));
    }


}
