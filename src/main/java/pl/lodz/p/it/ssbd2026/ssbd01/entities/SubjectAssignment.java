package pl.lodz.p.it.ssbd2026.ssbd01.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.AbstractEntity;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Teacher;

@Getter
@Setter
@Entity
@Table(name = "subject_assignment", indexes = {
        @Index(name = "idx_subject_assignment_teacher_id", columnList = "teacher_id"),
        @Index(name = "idx_subject_assignment_subject_id", columnList = "subject_id"),
        @Index(name = "idx_subject_assignment_created_by", columnList = "created_by"),
        @Index(name = "idx_subject_assignment_modified_by", columnList = "modified_by")
})
public class SubjectAssignment extends AbstractEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_role", nullable = false)
    private TeacherSubjectRole subjectRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", updatable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", updatable = false)
    private Subject subject;

}