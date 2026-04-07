package pl.lodz.p.it.ssbd2026.ssbd01.mpp.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.entities.AbstractEntity;
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
public class RaportLevelDict extends AbstractEntity {

    @Column(name = "name", nullable = false, unique = true)
    private String name;
}
