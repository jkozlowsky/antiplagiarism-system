package pl.lodz.p.it.ssbd2026.ssbd01.mpp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.entities.AbstractEntity;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "rule_preset", indexes = {
        @Index(name = "idx_rule_preset_raport_level_id", columnList = "raport_level_id"),
        @Index(name = "idx_rule_preset_subject_id", columnList = "subject_id"),
        @Index(name = "idx_rule_preset_created_by", columnList = "created_by"),
        @Index(name = "idx_rule_preset_modified_by", columnList = "modified_by")
})
public class RulePreset extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raport_level_id", nullable = false)
    private RaportLevelDict raportLevel;

    @Column(name = "student_ticket_count", nullable = false)
    private Integer studentTicketCount;

    @Column(name = "is_preset", nullable = false)
    private Boolean isPreset;

    @OneToMany(mappedBy = "rulePreset", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    private List<Schedule> scheduleList;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", updatable = false)
    private Subject subject;



}