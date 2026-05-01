package pl.lodz.p.it.ssbd2026.ssbd01.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "account", indexes = {
        @Index(name = "idx_account_language_version_id", columnList = "language_version_id"),
        @Index(name = "idx_account_created_by", columnList = "created_by"),
        @Index(name = "idx_account_modified_by", columnList = "modified_by")
})
@Getter
@Setter
public class Account extends AbstractEntity {

    public static final String UNIQUE_LOGIN_INDEX_NAME = "account_login_key";
    public static final String UNIQUE_EMAIL_INDEX_NAME = "account_email_key";

    @Column(name = "login", nullable = false, unique = true, updatable = false, length = AccountConsts.LOGIN_LENGTH)
    private String login;

    @Column(name = "password_hash", nullable = false, length = AccountConsts.PASSWORD_HASH_LENGTH)
    private String passwordHash;

    @Column(name = "email", nullable = false, unique = true, length = AccountConsts.EMAIL_LENGTH)
    private String email;

    @Column(name = "last_login_success_date_time")
    private LocalDateTime lastLoginSuccessDateTime;

    @Column(name = "last_login_failure_date_time")
    private LocalDateTime lastLoginFailureDateTime;

    @Column(name = "last_successful_login_ip", length = AccountConsts.IP_LENGTH)
    private String lastSuccessfulLoginIp;

    @Column(name = "last_failed_login_ip", length = AccountConsts.IP_LENGTH)
    private String lastFailedLoginIp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_version_id",  nullable = false)
    private LanguageDict languageVersion;

    @Column(name = "failed_attempts")
    private Integer failedAttempts;

    @Column(name = "is_blocked", nullable = false)
    private Boolean isBlocked;

    @OneToMany(mappedBy = "account", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<AccessLevel> accessLevelList = new ArrayList<>();

    @Column(name = "name", nullable = false, length = AccountConsts.NAME_LENGTH)
    private String name;

    @Column(name = "surname", nullable = false, length = AccountConsts.SURNAME_LENGTH)
    private String surname;

    @Column(name = "token_version", nullable = false)
    private Integer tokenVersion;

    @PrePersist
    protected void onCreate(){
        if (tokenVersion == null){
            tokenVersion = 1;
        }
    }
}