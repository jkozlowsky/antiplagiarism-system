package pl.lodz.p.it.ssbd2026.ssbd01.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_revert_token")
@Getter
@Setter
@NoArgsConstructor
public class EmailRevertTokenEnt extends AbstractEntity {

    @Column(name = "token_value", nullable = false, unique = true)
    private String tokenValue;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "old_email", nullable = false)
    private String oldEmail;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_used", nullable = false)
    private boolean isUsed = false;
}