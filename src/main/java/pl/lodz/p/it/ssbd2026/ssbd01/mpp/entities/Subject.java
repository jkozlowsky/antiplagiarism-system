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
public class Subject extends AbstractEntity {

    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Column(name = "edition", nullable = false, length = 16)
    private String edition;

    @Column(name = "gitea_url", nullable = false, length = 255)
    private String giteaURL;

    @Column(name = "archived", nullable = false)
    private Boolean archived;

    @OneToMany(mappedBy = "subject")
    private List<Report> reportList;

    @OneToOne(mappedBy = "subject")
    private RulePreset rulePreset;

    @OneToMany(mappedBy = "subject")
    private List<SubjectAssignment> subjectAssignmentList;

}