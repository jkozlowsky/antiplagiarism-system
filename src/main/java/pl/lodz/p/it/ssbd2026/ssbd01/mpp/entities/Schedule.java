package pl.lodz.p.it.ssbd2026.ssbd01.mpp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.entities.AbstractEntity;


import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table
public class Schedule extends AbstractEntity {

    @Column(name = "schedule_date_time", nullable = false)
    private LocalDateTime scheduleDateTime;

    @Column(name = "tag", nullable = false, length = 64)
    private String tag;

    @ManyToOne
    @JoinColumn(name = "rule_preset_id")
    private RulePreset rulePreset;
}