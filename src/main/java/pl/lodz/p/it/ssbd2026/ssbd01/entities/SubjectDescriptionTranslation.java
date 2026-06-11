package pl.lodz.p.it.ssbd2026.ssbd01.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "subject_description_translation",
        indexes = {
                @Index(name = "idx_subject_description_translation_created_by", columnList = "created_by"),
                @Index(name = "idx_subject_description_translation_modified_by", columnList = "modified_by"),
                @Index(name = "idx_subject_description_translation_subject", columnList = "subject_id"),
                @Index(name = "idx_subject_description_translation_language_dict", columnList = "language_dict_id")
        }
)
public class SubjectDescriptionTranslation extends AbstractEntity {

    @Column(name = "translated_description", nullable = false)
    private String translatedDescription;

    @Column(name = "is_original", nullable = false)
    private Boolean isOriginal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_dict_id", nullable = false)
    private LanguageDict languageDict;

}
