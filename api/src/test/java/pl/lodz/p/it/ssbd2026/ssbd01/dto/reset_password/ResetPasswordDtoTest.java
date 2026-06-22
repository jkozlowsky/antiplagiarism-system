package pl.lodz.p.it.ssbd2026.ssbd01.dto.reset_password;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ResetPasswordDtoTest {

    @Test
    void shouldCoverConfirmResetDto() {
        ConfirmResetDto dto1 = new ConfirmResetDto("token123", "NewP@ssword1!");
        ConfirmResetDto dto2 = new ConfirmResetDto("token123", "NewP@ssword1!");

        assertAll(
                () -> assertEquals("token123", dto1.token()),
                () -> assertEquals("NewP@ssword1!", dto1.newPassword()),
                () -> assertEquals(dto1, dto2),
                () -> assertEquals(dto1.hashCode(), dto2.hashCode()),
                () -> assertNotNull(dto1.toString())
        );
    }

    @Test
    void shouldCoverResetPasswordDTO() {
        ResetPasswordDTO dto1 = new ResetPasswordDTO("test@ex.com");
        ResetPasswordDTO dto2 = new ResetPasswordDTO("test@ex.com");

        assertAll(
                () -> assertEquals("test@ex.com", dto1.email()),
                () -> assertEquals(dto1, dto2),
                () -> assertEquals(dto1.hashCode(), dto2.hashCode()),
                () -> assertNotNull(dto1.toString())
        );
    }
}