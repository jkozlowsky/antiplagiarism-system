package pl.lodz.p.it.ssbd2026.ssbd01.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.javers.core.metamodel.annotation.ShallowReference;

@ShallowReference
@Entity
@Table(name = "admin")
public class Admin extends AccessLevel {

}
