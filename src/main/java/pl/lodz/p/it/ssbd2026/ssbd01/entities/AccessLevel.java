package pl.lodz.p.it.ssbd2026.ssbd01.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "access_level", indexes = {
        @Index(name = "idx_access_level_dict_id", columnList = "access_level_id"),
        @Index(name = "idx_access_level_account_id", columnList = "account_id"),
        @Index(name = "idx_access_level_created_by", columnList = "created_by"),
        @Index(name = "idx_access_level_modified_by", columnList = "modified_by")
})
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AccessLevel extends AbstractEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "access_level_id", nullable = false)
    private AccessLevelDict accessLevelDict;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="account_id")
    private Account account;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Transient
    public String getAccessLevelName() {
        return this.accessLevelDict != null ? this.accessLevelDict.getAccessLevelName() : null;
    }
}