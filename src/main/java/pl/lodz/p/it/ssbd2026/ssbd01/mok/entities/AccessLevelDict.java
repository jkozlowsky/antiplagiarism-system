package pl.lodz.p.it.ssbd2026.ssbd01.mok.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * STUDENT,
 * TEACHER,
 * ADMINISTRATOR
 **/
@Entity
@Table(name = "access_level_dict")
@Getter
@Setter
public class AccessLevelDict {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "access_level_name", nullable = false, unique = true)
    private String accessLevelName;
}