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
@Table(name = "repository")
public class Repository extends AbstractEntity {

    @Column(name = "repository_name", nullable = false, length = RepositoryContsts.REPOSITORY_NAME_LENGTH)
    private String repositoryName;

    @Column(name = "ticket_count", nullable = false)
    private String ticketCount;

    @ManyToOne
    @JoinColumn(name = "subject_id", updatable = false)
    private Subject subject;

    @ManyToMany
    @JoinTable(
            name = "repository_student",
            joinColumns = @JoinColumn(name = "repository_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<Student> studentList = new ArrayList<>();

    @ManyToMany(mappedBy = "repositoryList")
    private List<Report> reportList;
}