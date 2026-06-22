package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.LanguageDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.SubjectDescriptionTranslation;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.LanguageDictNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.OriginalLanguageNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.LanguageDictRepositoryMPP;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectDescriptionTranslationRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@MethodCallLogged
@TransactionLogged
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
@Retryable(retryFor = {JpaSystemException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
public class SubjectDescriptionTranslationService {

    private final SubjectRepository subjectRepository;
    private final LanguageDictRepositoryMPP languageDictRepository;
    private final SubjectDescriptionTranslationRepository subjectDescriptionTranslationRepository;
    private final GoogleTranslationService googleTranslationService;

    @MethodCallLogged
    @PreAuthorize("hasAnyAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER, T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")
    public String translateSubjectDescription(UUID subjectId, String language) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow(SubjectNotFoundException::new);
        LanguageDict languageDict = languageDictRepository.findByLanguage(language).orElseThrow(LanguageDictNotFoundException::new);

        SubjectDescriptionTranslation subjectDescriptionTranslation = subjectDescriptionTranslationRepository
                .findBySubjectIdAndLanguageDictId(subjectId, languageDict.getId())
                .orElseGet(()->getTranslation(subject, languageDict));

        return subjectDescriptionTranslationRepository.saveAndFlush(subjectDescriptionTranslation).getTranslatedDescription();
    }

    private SubjectDescriptionTranslation getTranslation(Subject subject, LanguageDict language){
        SubjectDescriptionTranslation originalTranslation = subjectDescriptionTranslationRepository
                .findBySubjectIdAndIsOriginalTrue(subject.getId())
                .orElseThrow(OriginalLanguageNotFoundException::new);

        SubjectDescriptionTranslation newTranslation = new SubjectDescriptionTranslation();
        newTranslation.setLanguageDict(language);
        newTranslation.setSubject(subject);
        newTranslation.setIsOriginal(false);

        newTranslation.setTranslatedDescription(googleTranslationService.translateText(
                originalTranslation.getTranslatedDescription(),
                originalTranslation.getLanguageDict().getLanguage(),
                newTranslation.getLanguageDict().getLanguage()
                ));

        return subjectDescriptionTranslationRepository.saveAndFlush(newTranslation);
    }
}
