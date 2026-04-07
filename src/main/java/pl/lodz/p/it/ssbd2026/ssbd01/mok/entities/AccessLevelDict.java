package pl.lodz.p.it.ssbd2026.ssbd01.mok.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
/**
 * STUDENT,
 * TEACHER,
 * ADMINISTRATOR
 **/
@Entity
@Table(name = "access_level_dict")
@Getter
@Setter
public class AccessLevelDict extends AbstractEntity {

    @Column(name = "access_level_name", nullable = false, unique = true)
    private String accessLevelName;
}