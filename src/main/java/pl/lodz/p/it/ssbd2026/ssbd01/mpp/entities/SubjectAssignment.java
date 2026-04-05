package pl.lodz.p.it.ssbd2026.ssbd01.mpp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.entities.AbstractEntity;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.entities.Teacher;

@Getter
@Setter
@Entity
@Table(name = "subject_assignment")
public class SubjectAssignment extends AbstractEntity {

    @Column(name = "has_edit_permissions", nullable = false)
    private Boolean hasEditPermissions;

    @ManyToOne
    @JoinColumn(name = "teacher_id", updatable = false)
    private Teacher teacher;

    @ManyToOne
    @JoinColumn(name = "subject_id", updatable = false)
    private Subject subject;


}