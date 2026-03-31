package pl.lodz.p.it.ssbd2026.ssbd01.mok.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "access_level")
public abstract class AccessLevel extends AbstractEntity {
    @Column(name = "access_level_name", nullable = false)
    private AccessLevelEnum accessLevelName;

    @ManyToOne
    @JoinColumn(name="account_id")
    private Account account;
}