package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.UpdateRulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RaportLevelDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.raportLevel.RaportLevelDictNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.rulePreset.RulePresetNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RaportLevelRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RuleRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdateRulePresetServiceTest {

    @Mock
    private RuleRepository ruleRepository;

    @Mock
    private RaportLevelRepository raportLevelRepository;

    @InjectMocks
    private RuleService ruleService;

    @Test
    @DisplayName("Powinno pomyślnie zaktualizować regułę")
    public void shouldUpdateRulePresetSuccessfully() {
        UUID ruleId = UUID.randomUUID();

        RulePreset existingRule = new RulePreset();
        ReflectionTestUtils.setField(existingRule, "id", ruleId);
        existingRule.setStudentTicketCount(5);
        existingRule.setMinimumTokensMatch(10);
        existingRule.setEnableNormalization(false);

        RaportLevelDict newRaportLevel = new RaportLevelDict();
        newRaportLevel.setName("NEW_LEVEL");

        UpdateRulePresetDTO dto = new UpdateRulePresetDTO("NEW_LEVEL", 15, 20, true);

        when(ruleRepository.findById(ruleId)).thenReturn(Optional.of(existingRule));
        when(raportLevelRepository.findByName("NEW_LEVEL")).thenReturn(Optional.of(newRaportLevel));

        ruleService.updateRulePreset(ruleId, dto);

        assertAll(
                () -> assertEquals(15, existingRule.getStudentTicketCount()),
                () -> assertEquals(20, existingRule.getMinimumTokensMatch()),
                () -> assertTrue(existingRule.getEnableNormalization()),
                () -> assertEquals(newRaportLevel, existingRule.getRaportLevel())
        );
        verify(ruleRepository, times(1)).saveAndFlush(existingRule);
    }

    @Test
    @DisplayName("Powinno rzucić RulePresetNotFoundException, gdy reguła nie istnieje")
    public void shouldThrowExceptionWhenRuleNotFound() {
        UUID ruleId = UUID.randomUUID();
        UpdateRulePresetDTO dto = new UpdateRulePresetDTO("FULL_VIEW", 10, 10, true);

        when(ruleRepository.findById(ruleId)).thenReturn(Optional.empty());

        RulePresetNotFoundException exception = assertThrows(
                RulePresetNotFoundException.class,
                () -> ruleService.updateRulePreset(ruleId, dto)
        );
        assertEquals("error.rule.preset.not.found", exception.getReason());
        verify(ruleRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Powinno rzucić RaportLevelDictNotFoundException, gdy poziom raportu nie istnieje")
    public void shouldThrowExceptionWhenRaportLevelNotFound() {
        UUID ruleId = UUID.randomUUID();
        RulePreset existingRule = new RulePreset();
        UpdateRulePresetDTO dto = new UpdateRulePresetDTO("NON_EXISTENT_LEVEL", 10, 10, true);

        when(ruleRepository.findById(ruleId)).thenReturn(Optional.of(existingRule));
        when(raportLevelRepository.findByName("NON_EXISTENT_LEVEL")).thenReturn(Optional.empty());

        RaportLevelDictNotFoundException exception = assertThrows(
                RaportLevelDictNotFoundException.class,
                () -> ruleService.updateRulePreset(ruleId, dto)
        );
        assertEquals("error.reportLevel.dict.not.found", exception.getReason());
        verify(ruleRepository, never()).saveAndFlush(any());
    }
}