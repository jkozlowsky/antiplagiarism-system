package pl.lodz.p.it.ssbd2026.ssbd01.mpp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.RepositoryContsts;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.entities.AbstractEntity;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.entities.Student;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "repository", indexes = {
        @Index(name = "idx_repository_subject_id", columnList = "subject_id"),
        @Index(name = "idx_repository_created_by", columnList = "created_by"),
        @Index(name = "idx_repository_modified_by", columnList = "modified_by")
})
public class Repository extends AbstractEntity {

    @Column(name = "repository_name", nullable = false, length = RepositoryContsts.REPOSITORY_NAME_LENGTH)
    private String repositoryName;

    @Column(name = "ticket_count", nullable = false)
    private String ticketCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", updatable = false)
    private Subject subject;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "repository_student",
            joinColumns = @JoinColumn(name = "repository_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id"),
            indexes = {
                    @Index(name = "idx_repository_student_repository_id", columnList = "repository_id"),
                    @Index(name = "idx_repository_student_student_id", columnList = "student_id")
            }
    )
    private List<Student> studentList = new ArrayList<>();

    @ManyToMany(mappedBy = "repositoryList")
    private List<Report> reportList;
}