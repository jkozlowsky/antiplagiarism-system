package pl.lodz.p.it.ssbd2026.ssbd01.mpp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.entities.AbstractEntity;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "rule_preset")
public class RulePreset extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "raport_level_id", nullable = false)
    private RaportLevelDict raportLevel;

    @Column(name = "student_ticket_count", nullable = false)
    private Integer studentTicketCount;

    @Column(name = "is_preset", nullable = false)
    private Boolean isPreset;

    @OneToMany(mappedBy = "rulePreset", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    private List<Schedule> scheduleList;

    @OneToOne
    @JoinColumn(name = "subject_id", updatable = false)
    private Subject subject;



}