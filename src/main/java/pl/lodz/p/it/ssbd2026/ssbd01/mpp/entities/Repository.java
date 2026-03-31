package pl.lodz.p.it.ssbd2026.ssbd01.mpp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.entities.AbstractEntity;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.entities.Student;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table
public class Repository extends AbstractEntity {

    @ManyToMany
    @JoinTable(
            name = "repository_student",
            joinColumns = @JoinColumn(name = "repository_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<Student> students = new ArrayList<>();
}