package pl.lodz.p.it.ssbd2026.ssbd01.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GeneralDtoTest {

    @Test
    void shouldCoverGoogleLoginRequestDTO() {
        GoogleLoginRequestDTO dto1 = new GoogleLoginRequestDTO("token");
        GoogleLoginRequestDTO dto2 = new GoogleLoginRequestDTO("token");

        assertAll(
                () -> assertEquals("token", dto1.idToken()),
                () -> assertEquals(dto1, dto2),
                () -> assertEquals(dto1.hashCode(), dto2.hashCode()),
                () -> assertNotNull(dto1.toString())
        );
    }

    @Test
    void shouldCoverOwnEmailChangeDTO() {
        OwnEmailChangeDTO dto1 = new OwnEmailChangeDTO("tok", "pass", "new@ex.com");
        OwnEmailChangeDTO dto2 = new OwnEmailChangeDTO("tok", "pass", "new@ex.com");

        assertAll(
                () -> assertEquals("tok", dto1.token()),
                () -> assertEquals("pass", dto1.password()),
                () -> assertEquals("new@ex.com", dto1.newEmail()),
                () -> assertEquals(dto1, dto2),
                () -> assertEquals(dto1.hashCode(), dto2.hashCode()),
                () -> assertNotNull(dto1.toString())
        );
    }

    @Test
    void shouldCoverRefreshTokenRequestDTO() {
        RefreshTokenRequestDTO dto1 = new RefreshTokenRequestDTO("ref");
        RefreshTokenRequestDTO dto2 = new RefreshTokenRequestDTO("ref");

        assertAll(
                () -> assertEquals("ref", dto1.refreshToken()),
                () -> assertEquals(dto1, dto2),
                () -> assertEquals(dto1.hashCode(), dto2.hashCode()),
                () -> assertNotNull(dto1.toString())
        );
    }
}