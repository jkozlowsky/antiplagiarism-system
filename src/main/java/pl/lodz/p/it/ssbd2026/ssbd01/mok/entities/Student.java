package pl.lodz.p.it.ssbd2026.ssbd01.mok.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.entities.Repository;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Student extends AccessLevel {

    @ManyToMany(mappedBy = "studentList")
    List<Repository> repositories = new ArrayList<>();
}
