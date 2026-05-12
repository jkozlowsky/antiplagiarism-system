package pl.lodz.p.it.ssbd2026.ssbd01.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;

@Entity
@Table(name = "password_history", indexes = {
        @Index(name = "idx_password_history_account_id", columnList = "account_id")
})
@Getter
@Setter
@NoArgsConstructor
public class PasswordHistory extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, updatable = false)
    private Account account;

    @Column(name = "password_hash", nullable = false, updatable = false, length = AccountConsts.PASSWORD_HASH_LENGTH)
    private String passwordHash;

    public PasswordHistory(Account account, String passwordHash) {
        this.account = account;
        this.passwordHash = passwordHash;
    }
}