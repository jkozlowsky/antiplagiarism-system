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
public class Report extends AbstractEntity {

    @Column(name = "path_to_report", nullable = false, length = 255)
    private String pathToReport;

    @ManyToMany
    @JoinTable(
            name = "report_repository",
            joinColumns = @JoinColumn(name = "report_id"),
            inverseJoinColumns = @JoinColumn(name = "repository_id")
    )
    private List<Repository> repositoryList;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;
}