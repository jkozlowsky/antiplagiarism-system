package pl.lodz.p.it.ssbd2026.ssbd01.mok.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "access_level")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AccessLevel extends AbstractEntity {
    @ManyToOne
    @JoinColumn(name = "access_level_id", nullable = false)
    private AccessLevelDict accessLevelName;

    @ManyToOne
    @JoinColumn(name="account_id")
    private Account account;
}