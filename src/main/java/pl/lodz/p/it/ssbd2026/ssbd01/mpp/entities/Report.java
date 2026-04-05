package pl.lodz.p.it.ssbd2026.ssbd01.mpp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.ReportConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.entities.AbstractEntity;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "report")
public class Report extends AbstractEntity {

    @Column(name = "path_to_report", nullable = false, length = ReportConsts.PATH_LENGTH)
    private String pathToReport;

    @ManyToMany
    @JoinTable(
            name = "report_repository",
            joinColumns = @JoinColumn(name = "report_id", updatable = false),
            inverseJoinColumns = @JoinColumn(name = "repository_id", updatable = false)
    )
    private List<Repository> repositoryList;

    @ManyToOne
    @JoinColumn(name = "subject_id", updatable = false)
    private Subject subject;
}