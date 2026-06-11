package pl.lodz.p.it.ssbd2026.ssbd01.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.javers.core.metamodel.annotation.ShallowReference;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.SubjectAssignment;

import java.util.ArrayList;
import java.util.List;

@ShallowReference
@Entity
@Getter
@Setter
@Table(name = "teacher")
public class Teacher extends AccessLevel {
    @OneToMany(mappedBy = "teacher")
    private List<SubjectAssignment> subjectAssignmentList = new ArrayList<>();
}
