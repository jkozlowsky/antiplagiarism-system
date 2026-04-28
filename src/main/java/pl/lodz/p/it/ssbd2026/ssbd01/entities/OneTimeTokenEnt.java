package pl.lodz.p.it.ssbd2026.ssbd01.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;

import java.time.LocalDateTime;

@Entity
@Table(name = "one_time_token")
@Getter
@Setter
public class OneTimeTokenEnt extends AbstractEntity{

    @Column(name = "token_value", nullable = false, unique = true, updatable = false, length = 255) //TODO: zmienić rozmiar na konstantę (wyznaczyć jaki)
    private String tokenValue;

    @Column(name = "username", nullable = false, updatable = false, length = AccountConsts.LOGIN_LENGTH)
    private String username;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private LocalDateTime expiresAt;

    @Column(name="is_used", nullable = false)
    private boolean isUsed;

}
