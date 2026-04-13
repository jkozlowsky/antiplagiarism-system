package pl.lodz.p.it.ssbd2026.ssbd01.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * dictionary table that can contain one of these values:
 * FULL_VIEW,
 * ONLY_PERCENTAGES,
 * ONLY_HIGHEST_PERCENT,
 * NOTHING
 **/
@Entity
@Table(name = "raport_level_dict")
@Getter
@Setter
public class RaportLevelDict{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "name", nullable = false, unique = true)
    private String name;
}
