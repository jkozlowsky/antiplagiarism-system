package pl.lodz.p.it.ssbd2026.ssbd01.mpp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.SubjectConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.entities.AbstractEntity;

import java.util.List;

@Getter
@Setter
@Entity
@Table
public class Subject extends AbstractEntity {

    @Column(name = "name", nullable = false, length = SubjectConsts.SUBJECT_NAME_LENGTH)
    private String name;

    @Column(name = "edition", nullable = false, length = SubjectConsts.SUBJECT_EDITION_LENGTH)
    private String edition;

    @Column(name = "subject_description", length = SubjectConsts.SUBJECT_DESCRIPTION_LENGTH)
    private String subjectDescription;

    @Column(name = "gitea_url", nullable = false, length = SubjectConsts.GITEA_URL_LENGTH)
    private String giteaURL;

    @Column(name = "archived", nullable = false)
    private Boolean archived;

    @OneToMany(mappedBy = "subject", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    private List<Report> reportList;

    @OneToMany(mappedBy = "subject", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    private List<Repository> repositoryList;

    @OneToOne(mappedBy = "subject", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    private RulePreset rulePreset;

    @OneToMany(mappedBy = "subject", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    private List<SubjectAssignment> subjectAssignmentList;

}