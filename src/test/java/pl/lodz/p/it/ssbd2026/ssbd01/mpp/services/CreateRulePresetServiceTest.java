package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.CreateRulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RaportLevelDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.subject.SubjectNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RaportLevelDictRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RuleRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateRulePresetServiceTest {

    @Mock private RuleRepository ruleRepository;
    @Mock private SubjectRepository subjectRepository;
    @Mock private RaportLevelDictRepository raportLevelDictRepository;

    @InjectMocks
    private RuleService ruleService;

    @Test
    @DisplayName("Powinno pomyślnie utworzyć nowy preset dla przedmiotu")
    public void shouldCreateRulePresetSuccessfully() {
        UUID subjectId = UUID.randomUUID();
        CreateRulePresetDTO dto = new CreateRulePresetDTO(10, "FULL_VIEW");

        Subject subject = new Subject();
        RaportLevelDict raportLevel = new RaportLevelDict();

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(raportLevelDictRepository.findByName("FULL_VIEW")).thenReturn(raportLevel);

        ruleService.createRulePreset(subjectId, dto);

        verify(ruleRepository, times(1)).save(any(RulePreset.class));
    }

    @Test
    @DisplayName("Powinno rzucić SubjectNotFoundException, gdy przedmiot nie istnieje")
    public void shouldThrowExceptionWhenSubjectNotFound() {
        UUID subjectId = UUID.randomUUID();
        CreateRulePresetDTO dto = new CreateRulePresetDTO(10, "FULL_VIEW");
        when(subjectRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(SubjectNotFoundException.class, () -> ruleService.createRulePreset(subjectId, dto));
    }

    @Test
    @DisplayName("Powinno rzucić RuntimeException, gdy poziom raportu nie istnieje")
    public void shouldThrowExceptionWhenRaportLevelMissing() {
        UUID subjectId = UUID.randomUUID();
        CreateRulePresetDTO dto = new CreateRulePresetDTO(10, "INVALID_LEVEL");
        when(subjectRepository.findById(any())).thenReturn(Optional.of(new Subject()));
        when(raportLevelDictRepository.findByName("INVALID_LEVEL")).thenReturn(null);

        assertThrows(RuntimeException.class, () -> ruleService.createRulePreset(subjectId, dto));
    }
}