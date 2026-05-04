package pl.lodz.p.it.ssbd2026.ssbd01.mok.mok14;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class LogoutTest {
    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private JwtAuthService jwtAuthService;

    @BeforeEach
    void setUp() {
        String testSecret = "ToJestTesttowyS3CR3Tiniejestslabybochcezebybylsilnyijwtdajmizrobictentestttttttttttttt===";

        ReflectionTestUtils.setField(jwtAuthService, "secret", testSecret);
        ReflectionTestUtils.setField(jwtAuthService, "expirationTime", 900000L);

        jwtAuthService.init();
    }

    @Test
    public void POS01_Logout_ShouldBeSuccessfulAndShouldBlacklistToken() {
        String token = jwtAuthService.generateToken("test", List.of(AccountConsts.ROLE_STUDENT));
        assertTrue(jwtAuthService.isTokenValid(token));
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        jwtAuthService.logout(request, response, null);

        assertAll(() -> {
            assertFalse(jwtAuthService.isTokenValid(token));
            assertTrue(jwtAuthService.isTokenBlacklisted(token));
        });
    }

    @Test
    public void NEG01_Logout_ShouldIgnoreInvalidAuthorizationHeader() {
        when(request.getHeader("Authorization")).thenReturn("NieBearer sjndasjdnasljdnakjfbasjbdsj,5r5tyfhj!!!!@@@@");

        assertDoesNotThrow(() -> jwtAuthService.logout(request, response, null));
    }




}
