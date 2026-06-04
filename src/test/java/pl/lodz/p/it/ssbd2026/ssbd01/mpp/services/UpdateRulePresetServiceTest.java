package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.UpdateRulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.ReportLevelDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.raportLevel.RaportLevelDictNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.rulePreset.RulePresetNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RaportLevelDictRepository;
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
    private RaportLevelDictRepository raportLevelDictRepository;

    @Mock
    private VersionHashService versionHashService;

    @InjectMocks
    private RuleService ruleService;

    @Test
    @DisplayName("Powinno pomyślnie zaktualizować regułę")
    public void shouldUpdateRulePresetSuccessfully() {
        UUID ruleId = UUID.randomUUID();
        String versionHash = "valid-hash";

        RulePreset existingRule = new RulePreset();
        ReflectionTestUtils.setField(existingRule, "id", ruleId);
        ReflectionTestUtils.setField(existingRule, "version", 1);
        existingRule.setStudentTicketCount(5);
        existingRule.setMinimumTokensMatch(10);
        existingRule.setEnableNormalization(false);

        ReportLevelDict newRaportLevel = new ReportLevelDict();
        newRaportLevel.setName("NEW_LEVEL");

        UpdateRulePresetDTO dto = new UpdateRulePresetDTO("NEW_LEVEL", 15, 20, true);

        when(ruleRepository.findById(ruleId)).thenReturn(Optional.of(existingRule));
        when(versionHashService.hashObjectVersion(ruleId, existingRule.getVersion())).thenReturn(versionHash);
        when(raportLevelDictRepository.findByName("NEW_LEVEL")).thenReturn(Optional.of(newRaportLevel));

        ruleService.updateRulePreset(ruleId, dto, versionHash);

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
        String versionHash = "any-hash";
        UpdateRulePresetDTO dto = new UpdateRulePresetDTO("FULL_VIEW", 10, 10, true);

        when(ruleRepository.findById(ruleId)).thenReturn(Optional.empty());

        RulePresetNotFoundException exception = assertThrows(
                RulePresetNotFoundException.class,
                () -> ruleService.updateRulePreset(ruleId, dto, versionHash)
        );

        assertEquals("error.rule.preset.not.found", exception.getReason());
        verify(ruleRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Powinno rzucić RaportLevelDictNotFoundException, gdy poziom raportu nie istnieje")
    public void shouldThrowExceptionWhenRaportLevelNotFound() {
        UUID ruleId = UUID.randomUUID();
        String versionHash = "valid-hash";

        RulePreset existingRule = new RulePreset();
        ReflectionTestUtils.setField(existingRule, "id", ruleId);
        ReflectionTestUtils.setField(existingRule, "version", 1);

        UpdateRulePresetDTO dto = new UpdateRulePresetDTO("NON_EXISTENT_LEVEL", 10, 10, true);

        when(ruleRepository.findById(ruleId)).thenReturn(Optional.of(existingRule));
        when(versionHashService.hashObjectVersion(ruleId, existingRule.getVersion())).thenReturn(versionHash);
        when(raportLevelDictRepository.findByName("NON_EXISTENT_LEVEL")).thenReturn(Optional.empty());

        RaportLevelDictNotFoundException exception = assertThrows(
                RaportLevelDictNotFoundException.class,
                () -> ruleService.updateRulePreset(ruleId, dto, versionHash)
        );

        assertEquals("error.reportLevel.dict.not.found", exception.getReason());
        verify(ruleRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Powinno rzucić ConcurrentUpdateException, gdy hash wersji się nie zgadza")
    public void shouldThrowExceptionWhenVersionHashMismatch() {
        UUID ruleId = UUID.randomUUID();
        String providedHash = "invalid-hash";
        String actualHash = "valid-hash";

        RulePreset existingRule = new RulePreset();
        ReflectionTestUtils.setField(existingRule, "id", ruleId);
        ReflectionTestUtils.setField(existingRule, "version", 1);

        UpdateRulePresetDTO dto = new UpdateRulePresetDTO("FULL_VIEW", 10, 10, true);

        when(ruleRepository.findById(ruleId)).thenReturn(Optional.of(existingRule));
        when(versionHashService.hashObjectVersion(ruleId, existingRule.getVersion())).thenReturn(actualHash);

        assertThrows(
                ConcurrentUpdateException.class,
                () -> ruleService.updateRulePreset(ruleId, dto, providedHash)
        );

        verify(ruleRepository, never()).saveAndFlush(any());
    }
}