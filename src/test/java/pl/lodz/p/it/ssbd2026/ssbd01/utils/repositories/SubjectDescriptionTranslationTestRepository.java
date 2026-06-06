package pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.SubjectDescriptionTranslation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubjectDescriptionTranslationTestRepository extends BaseTestRepository<SubjectDescriptionTranslation> {
    @Query("select sdt from SubjectDescriptionTranslation sdt where sdt.subject.id = :subjectId and sdt.languageDict.id = :langId")
    Optional<SubjectDescriptionTranslation> findBySubjectIdAndLanguageDictionaryId(
            @Param("subjectId") UUID subjectId, @Param("langId") UUID langId
    );

    Optional<SubjectDescriptionTranslation> findBySubjectIdAndLanguageDictId(UUID subjectId, UUID languageDictId);

    Optional<SubjectDescriptionTranslation> findBySubjectIdAndIsOriginalTrue(UUID subjectId);

    List<SubjectDescriptionTranslation> findBySubjectIdInAndIsOriginalTrue(List<UUID> subjectIds);

    void deleteAllBySubjectId(UUID subjectId);
}
