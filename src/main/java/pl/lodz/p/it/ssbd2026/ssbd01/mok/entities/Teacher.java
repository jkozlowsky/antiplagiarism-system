package pl.lodz.p.it.ssbd2026.ssbd01.mok.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.entities.SubjectAssignment;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Teacher extends AccessLevel {
    @OneToMany(mappedBy = "teacher")
    private List<SubjectAssignment> subjectAssignmentList = new ArrayList<>();
}
