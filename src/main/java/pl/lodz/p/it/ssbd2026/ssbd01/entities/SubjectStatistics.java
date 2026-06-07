package pl.lodz.p.it.ssbd2026.ssbd01.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "subject_statistics",
        indexes = {
                @Index(name = "idx_subject_statistics_created_by", columnList = "created_by"),
                @Index(name = "idx_subject_statistics_modified_by", columnList = "modified_by"),
                @Index(name = "idx_subject_statistics_subject", columnList = "subject_id")
        })
public class SubjectStatistics extends AbstractEntity{

    @Column(name = "average_similarity")
    Double averageSimilarity;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false, unique = true)
    private Subject subject;

}
