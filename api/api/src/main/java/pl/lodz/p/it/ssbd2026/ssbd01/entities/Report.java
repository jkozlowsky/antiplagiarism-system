package pl.lodz.p.it.ssbd2026.ssbd01.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.ReportConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.TagConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.AbstractEntity;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "report", indexes = {
        @Index(name = "idx_report_subject_id", columnList = "subject_id"),
        @Index(name = "idx_report_created_by", columnList = "created_by"),
        @Index(name = "idx_report_modified_by", columnList = "modified_by"),
        @Index(name = "idx_report_repository", columnList = "initiating_repository")
})
public class Report extends AbstractEntity {

    @Column(name = "path_to_report", nullable = false, length = ReportConsts.PATH_LENGTH)
    private String pathToReport;

    @Column(name = "tag", nullable = false, length = TagConsts.TAG_LENGTH)
    private String tag;

    @Column(name = "average_similarity", nullable = false)
    private Double averageSimilarity;

    @Column(name = "scanned_repositories", nullable = false)
    private Long scannedRepositories;

    @Column(name = "is_student_report", nullable = false)
    private Boolean isStudentReport;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "report_repository",
            joinColumns = @JoinColumn(name = "report_id"),
            inverseJoinColumns = @JoinColumn(name = "repository_id"),
            indexes = {
                    @Index(name = "idx_report_repository_report_id", columnList = "report_id"),
                    @Index(name = "idx_report_repository_repository_id", columnList = "repository_id")
            }
    )
    private List<Repository> repositoryList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", updatable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiating_repository")
    private Repository initiatingRepository;
}