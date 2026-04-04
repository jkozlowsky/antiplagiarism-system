package pl.lodz.p.it.ssbd2026.ssbd01.mpp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.entities.AbstractEntity;

import java.util.List;

@Getter
@Setter
@Entity
@Table
public class RulePreset extends AbstractEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "raport_level", nullable = false, length = 32)
    private RaportLevelEnum raportLevel;

    @Column(name = "student_ticket_count", nullable = false)
    private Integer studentTicketCount;

    @Column(name = "is_preset", nullable = false)
    private Boolean isPreset;

    @OneToMany(mappedBy = "rulePreset")
    private List<Schedule> scheduleList;

    @OneToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;



}