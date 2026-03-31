package pl.lodz.p.it.ssbd2026.ssbd01.mok.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.entities.Repository;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Student extends AccessLevel {

    @ManyToMany(mappedBy = "students")
    List<Repository> repositories = new ArrayList<>();
}
