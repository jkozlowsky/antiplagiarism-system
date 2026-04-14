package pl.lodz.p.it.ssbd2026.ssbd01.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Repository;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name="student")
public class Student extends AccessLevel {

    @ManyToMany(mappedBy = "studentList")
    private List<Repository> repositories = new ArrayList<>();
}
