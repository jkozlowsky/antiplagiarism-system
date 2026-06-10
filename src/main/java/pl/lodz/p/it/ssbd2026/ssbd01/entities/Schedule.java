package pl.lodz.p.it.ssbd2026.ssbd01.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.TagConsts;


import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "schedule", indexes = {
        @Index(name = "idx_schedule_rule_preset_id", columnList = "rule_preset_id"),
        @Index(name = "idx_schedule_created_by", columnList = "created_by"),
        @Index(name = "idx_schedule_modified_by", columnList = "modified_by"),
        @Index(name = "idx_schedule_is_executed_date", columnList = "is_executed, schedule_date_time"),
})
public class Schedule extends AbstractEntity {

    @Column(name = "schedule_date_time", nullable = false)
    private LocalDateTime scheduleDateTime;

    @Column(name = "tag", nullable = false, length = TagConsts.TAG_LENGTH)
    private String tag;

    @Column(name = "is_executed", nullable = false)
    private boolean isExecuted = false;

    @Column(name = "has_failed", nullable = false)
    private boolean hasFailed = false;

    @Column(name = "modifiable_until", nullable = false)
    private LocalDateTime modifiableUntil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_preset_id")
    private RulePreset rulePreset;
}