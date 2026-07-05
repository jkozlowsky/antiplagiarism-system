package pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.common.AbstractRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.SubjectDescriptionTranslation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@JaversSpringDataAuditable
@Repository
@Transactional(propagation = Propagation.MANDATORY, readOnly = true, transactionManager = "mppTransactionManager")
public interface SubjectDescriptionTranslationRepository extends AbstractRepository<SubjectDescriptionTranslation> {
    @Query("select sdt from SubjectDescriptionTranslation sdt where sdt.subject.id = :subjectId and sdt.languageDict.id = :langId")
    Optional<SubjectDescriptionTranslation> findBySubjectIdAndLanguageDictionaryId(
            @Param("subjectId") UUID subjectId, @Param("langId") UUID langId
    );

    Optional<SubjectDescriptionTranslation> findBySubjectIdAndLanguageDictId(UUID subjectId, UUID languageDictId);

    Optional<SubjectDescriptionTranslation> findBySubjectIdAndIsOriginalTrue(UUID subjectId);

    List<SubjectDescriptionTranslation> findBySubjectIdInAndIsOriginalTrue(List<UUID> subjectIds);

    void deleteAllBySubjectId(UUID subjectId);

}
