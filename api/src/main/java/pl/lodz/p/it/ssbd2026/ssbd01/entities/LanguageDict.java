package pl.lodz.p.it.ssbd2026.ssbd01.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.javers.core.metamodel.annotation.ShallowReference;

import java.util.UUID;

/**
 * dictionary table that can contain one of these values:
 * PL,
 * EN,
 * UK
 **/
@ShallowReference
@Entity
@Table(name = "language_dict")
@Getter
@Setter
public class LanguageDict{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "language", nullable = false, unique = true)
    private String language;
}
