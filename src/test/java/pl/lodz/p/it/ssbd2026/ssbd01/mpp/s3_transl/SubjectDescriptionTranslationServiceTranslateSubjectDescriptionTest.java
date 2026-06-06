package pl.lodz.p.it.ssbd2026.ssbd01.mpp.s3_transl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.LanguageDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.SubjectDescriptionTranslation;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.LanguageDictNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.OriginalLanguageNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.LanguageDictRepositoryMPP;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectDescriptionTranslationRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.GoogleTranslationService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.SubjectDescriptionTranslationService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubjectDescriptionTranslationServiceTranslateSubjectDescriptionTest {
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private LanguageDictRepositoryMPP languageDictRepository;
    @Mock
    private SubjectDescriptionTranslationRepository subjectDescriptionTranslationRepository;
    @Mock
    private GoogleTranslationService googleTranslationService;
    @InjectMocks
    private SubjectDescriptionTranslationService subjectDescriptionTranslationService;

    private UUID subjectId;
    private UUID langDictId;
    private UUID subjectDescTranslationId;
    private Subject subject;
    private LanguageDict languageDict;
    private SubjectDescriptionTranslation subjectDescriptionTranslation;

    private final String DESCRIPTION_EXAMPLE = "Przykładowy opis";
    private final String TRANSLATION_EXAMPLE = "Translated by Google";

    @BeforeEach
    void setUp() {
        subjectId = UUID.randomUUID();
        langDictId = UUID.randomUUID();
        subjectDescTranslationId = UUID.randomUUID();

        subject = new Subject();
        languageDict = new LanguageDict();
        subjectDescriptionTranslation = new SubjectDescriptionTranslation();

        ReflectionTestUtils.setField(subject, "id", subjectId);
        ReflectionTestUtils.setField(languageDict, "id", langDictId);
        ReflectionTestUtils.setField(subjectDescriptionTranslation, "id", subjectDescTranslationId);
    }

    @Test
    public void POS01_translateSubjectDescription_ShouldReturnExistingTranslation_WhenTranslationExistsInDB() {
        String language = "pl";
        languageDict.setLanguage(language);
        subjectDescriptionTranslation.setTranslatedDescription(DESCRIPTION_EXAMPLE);
        subjectDescriptionTranslation.setLanguageDict(languageDict);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(languageDictRepository.findByLanguage(language)).thenReturn(Optional.of(languageDict));
        when(subjectDescriptionTranslationRepository.findBySubjectIdAndLanguageDictId(eq(subjectId), any()))
                .thenReturn(Optional.of(subjectDescriptionTranslation));
        when(subjectDescriptionTranslationRepository.saveAndFlush(any(SubjectDescriptionTranslation.class)))
                .thenReturn(subjectDescriptionTranslation);

        String result = subjectDescriptionTranslationService.translateSubjectDescription(subjectId, language);

        assertEquals(DESCRIPTION_EXAMPLE, result);

        verify(googleTranslationService, never()).translateText(anyString(), anyString(), anyString());
        verify(subjectDescriptionTranslationRepository, never()).findBySubjectIdAndIsOriginalTrue(any());
    }

    @Test
    public void POS02_translateSubjectDescription_ShouldFetchFromGoogle_WhenTranslationDoesNotExistInDB() {
        String targetLanguage = "en";
        String originalLanguage = "pl";
        languageDict.setLanguage(targetLanguage);

        LanguageDict originalLangDict = new LanguageDict();
        originalLangDict.setLanguage(originalLanguage);

        SubjectDescriptionTranslation originalTranslation = new SubjectDescriptionTranslation();
        originalTranslation.setTranslatedDescription(DESCRIPTION_EXAMPLE);
        originalTranslation.setLanguageDict(originalLangDict);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(languageDictRepository.findByLanguage(targetLanguage)).thenReturn(Optional.of(languageDict));
        when(subjectDescriptionTranslationRepository.findBySubjectIdAndLanguageDictId(eq(subjectId), any()))
                .thenReturn(Optional.empty());
        when(subjectDescriptionTranslationRepository.findBySubjectIdAndIsOriginalTrue(subjectId))
                .thenReturn(Optional.of(originalTranslation));
        when(googleTranslationService.translateText(anyString(), anyString(), anyString()))
                .thenReturn(TRANSLATION_EXAMPLE);
        when(subjectDescriptionTranslationRepository.saveAndFlush(any(SubjectDescriptionTranslation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String result = subjectDescriptionTranslationService.translateSubjectDescription(subjectId, targetLanguage);

        assertEquals(TRANSLATION_EXAMPLE, result);
        verify(googleTranslationService, times(1)).translateText(anyString(), anyString(), anyString());
        verify(subjectDescriptionTranslationRepository, times(2)).saveAndFlush(any(SubjectDescriptionTranslation.class));
    }

    @Test
    public void NEG01_translateSubjectDescription_ShouldThrowSubjectNotFoundException_WhenSubjectDoesNotExist() {
        String language = "pl";
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        assertThrows(SubjectNotFoundException.class, () ->
                subjectDescriptionTranslationService.translateSubjectDescription(subjectId, language)
        );

        verifyNoInteractions(languageDictRepository);
        verifyNoInteractions(subjectDescriptionTranslationRepository);
        verifyNoInteractions(googleTranslationService);
    }

    @Test
    public void NEG02_translateSubjectDescription_ShouldThrowLanguageDictNotFoundException_WhenLanguageDoesNotExist() {
        String language = "pl";
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(languageDictRepository.findByLanguage(language)).thenReturn(Optional.empty());

        assertThrows(LanguageDictNotFoundException.class, () ->
                subjectDescriptionTranslationService.translateSubjectDescription(subjectId, language)
        );

        verifyNoInteractions(subjectDescriptionTranslationRepository);
        verifyNoInteractions(googleTranslationService);
    }

    @Test
    public void NEG03_translateSubjectDescription_ShouldThrowOriginalLanguageNotFoundException_WhenOriginalTranslationMissing() {
        String language = "pl";
        languageDict.setLanguage(language);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(languageDictRepository.findByLanguage(language)).thenReturn(Optional.of(languageDict));
        when(subjectDescriptionTranslationRepository.findBySubjectIdAndLanguageDictId(eq(subjectId), any()))
                .thenReturn(Optional.empty());
        when(subjectDescriptionTranslationRepository.findBySubjectIdAndIsOriginalTrue(subjectId))
                .thenReturn(Optional.empty());

        assertThrows(OriginalLanguageNotFoundException.class, () ->
                subjectDescriptionTranslationService.translateSubjectDescription(subjectId, language)
        );

        verifyNoInteractions(googleTranslationService);
    }
}
