package pl.lodz.p.it.ssbd2026.ssbd01.mok.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * dictionary table that can contain one of these values:
 * PL,
 * EN,
 * UK
 **/
@Entity
@Table(name = "language_dict")
@Getter
@Setter
public class LanguageDict extends AbstractEntity {

    @Column(name = "language", nullable = false, unique = true)
    private String language;
}
