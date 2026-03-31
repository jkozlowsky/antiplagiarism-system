package pl.lodz.p.it.ssbd2026.ssbd01.mok.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "account")
@Getter
@Setter
public class Account extends AbstractEntity {
    @Column(name = "login", nullable = false, unique = true, updatable = false, length = 50)
    private String login;

    @Column(name = "password_hash", nullable = false, length = 64)
    private String passwordHash;

    @Column(name = "email", nullable = false, unique = true, length = 50)
    private String email;

    @Column(name = "last_login_success_date_time")
    private LocalDateTime lastLoginSuccessDateTime;

    @Column(name = "last_login_failure_date_time")
    private LocalDateTime lastLoginFailureDateTime;

    @Column(name = "last_successful_login_ip", length = 30)
    private String lastSuccessfulLoginIp;

    @Column(name = "language_version", length = 2)
    private String languageVersion;

    @OneToMany(mappedBy = "account", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<AccessLevel> accessLevelList = new ArrayList<>();

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "name", nullable = false, length = 50)
    private String surname;
}
