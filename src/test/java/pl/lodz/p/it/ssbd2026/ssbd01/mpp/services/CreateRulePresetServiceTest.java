package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.CreateRulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RaportLevelDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.raportLevel.RaportLevelDictNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.rulePreset.RulePresetInvalidPartametersException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.rulePreset.RulePresetTemplateNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.subject.SubjectNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RaportLevelDictRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RuleRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateRulePresetServiceTest {

    @Mock
    private RuleRepository ruleRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private RaportLevelDictRepository raportLevelDictRepository;

    @InjectMocks
    private RuleService ruleService;

    @Test
    @DisplayName("Powinno pomyślnie utworzyć preset na podstawie istniejącego szablonu")
    public void shouldCreateRulePresetFromTemplateSuccessfully() {
        UUID subjectId = UUID.randomUUID();
        UUID presetId = UUID.randomUUID();
        CreateRulePresetDTO dto = new CreateRulePresetDTO(null, null, presetId, null, null);

        Subject subject = new Subject();
        RulePreset template = new RulePreset();
        RaportLevelDict templateRaportLevel = new RaportLevelDict();
        template.setRaportLevel(templateRaportLevel);
        template.setStudentTicketCount(20);
        template.setMinimumTokensMatch(25);
        template.setEnableNormalization(true);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(ruleRepository.findById(presetId)).thenReturn(Optional.of(template));

        ruleService.createRulePreset(subjectId, dto);

        ArgumentCaptor<RulePreset> captor = ArgumentCaptor.forClass(RulePreset.class);
        verify(ruleRepository, times(1)).save(captor.capture());

        RulePreset savedPreset = captor.getValue();
        assertFalse(savedPreset.getIsPreset());
        assertEquals(subject, savedPreset.getSubject());
        assertEquals(20, savedPreset.getStudentTicketCount());
        assertEquals(templateRaportLevel, savedPreset.getRaportLevel());
        assertEquals(25, savedPreset.getMinimumTokensMatch());
        assertTrue(savedPreset.getEnableNormalization());
    }

    @Test
    @DisplayName("Powinno pomyślnie utworzyć własny preset dla przedmiotu z niestandardowymi parametrami")
    public void shouldCreateCustomRulePresetSuccessfully() {
        UUID subjectId = UUID.randomUUID();
        CreateRulePresetDTO dto = new CreateRulePresetDTO(10, "FULL_VIEW", null, 15, false);

        Subject subject = new Subject();
        RaportLevelDict raportLevel = new RaportLevelDict();

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(raportLevelDictRepository.findByName("FULL_VIEW")).thenReturn(raportLevel);

        ruleService.createRulePreset(subjectId, dto);

        ArgumentCaptor<RulePreset> captor = ArgumentCaptor.forClass(RulePreset.class);
        verify(ruleRepository, times(1)).save(captor.capture());

        RulePreset savedPreset = captor.getValue();
        assertFalse(savedPreset.getIsPreset());
        assertEquals(subject, savedPreset.getSubject());
        assertEquals(10, savedPreset.getStudentTicketCount());
        assertEquals(raportLevel, savedPreset.getRaportLevel());
        assertEquals(15, savedPreset.getMinimumTokensMatch());
        assertFalse(savedPreset.getEnableNormalization());
    }

    @Test
    @DisplayName("Powinno rzucić SubjectNotFoundException, gdy przedmiot nie istnieje")
    public void shouldThrowSubjectNotFoundExceptionWhenSubjectDoesNotExist() {
        UUID subjectId = UUID.randomUUID();
        CreateRulePresetDTO dto = new CreateRulePresetDTO(10, "FULL_VIEW", null, 20, false);

        when(subjectRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(SubjectNotFoundException.class, () -> ruleService.createRulePreset(subjectId, dto));
        verify(ruleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Powinno rzucić RulePresetTemplateNotFoundException, gdy wybrany szablon nie istnieje")
    public void shouldThrowRulePresetTemplateNotFoundExceptionWhenTemplateDoesNotExist() {
        UUID subjectId = UUID.randomUUID();
        UUID presetId = UUID.randomUUID();
        CreateRulePresetDTO dto = new CreateRulePresetDTO(null, null, presetId, null, null);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(new Subject()));
        when(ruleRepository.findById(presetId)).thenReturn(Optional.empty());

        assertThrows(RulePresetTemplateNotFoundException.class, () -> ruleService.createRulePreset(subjectId, dto));
        verify(ruleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Powinno rzucić RulePresetInvalidPartametersException, gdy brakuje parametrów przy braku identyfikatora szablonu")
    public void shouldThrowRulePresetInvalidParametersExceptionWhenMissingParams() {
        UUID subjectId = UUID.randomUUID();

        CreateRulePresetDTO dtoMissingTicket = new CreateRulePresetDTO(null, "FULL_VIEW", null, 20, false);
        CreateRulePresetDTO dtoMissingLevel = new CreateRulePresetDTO(10, null, null, 20, false);
        CreateRulePresetDTO dtoMissingTokens = new CreateRulePresetDTO(10, "FULL_VIEW", null, null, false);
        CreateRulePresetDTO dtoMissingNormalization = new CreateRulePresetDTO(10, "FULL_VIEW", null, 20, null);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(new Subject()));

        assertThrows(RulePresetInvalidPartametersException.class, () -> ruleService.createRulePreset(subjectId, dtoMissingTicket));
        assertThrows(RulePresetInvalidPartametersException.class, () -> ruleService.createRulePreset(subjectId, dtoMissingLevel));
        assertThrows(RulePresetInvalidPartametersException.class, () -> ruleService.createRulePreset(subjectId, dtoMissingTokens));
        assertThrows(RulePresetInvalidPartametersException.class, () -> ruleService.createRulePreset(subjectId, dtoMissingNormalization));

        verify(ruleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Powinno rzucić SubjectNotFoundException, gdy podany niestandardowy poziom raportu nie istnieje w słowniku")
    public void shouldThrowExceptionWhenRaportLevelMissing() {
        UUID subjectId = UUID.randomUUID();
        CreateRulePresetDTO dto = new CreateRulePresetDTO(10, "INVALID_LEVEL", null, 20, false);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(new Subject()));
        when(raportLevelDictRepository.findByName("INVALID_LEVEL")).thenReturn(null);

        assertThrows(RaportLevelDictNotFoundException.class, () -> ruleService.createRulePreset(subjectId, dto));
        verify(ruleRepository, never()).save(any());
    }
}