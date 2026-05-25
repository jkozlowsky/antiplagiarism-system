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
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.raportLevel.RaportLevelDictNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.rulePreset.RulePresetInvalidPartametersException;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RaportLevelDictRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RuleRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateRulePresetServiceTest {

    @Mock
    private RuleRepository ruleRepository;

    @Mock
    private RaportLevelDictRepository raportLevelDictRepository;

    @InjectMocks
    private RuleService ruleService;

    @Test
    @DisplayName("Powinno pomyślnie utworzyć predefiniowany szablon reguł ze standardowymi wartościami")
    public void shouldCreateRulePresetTemplateSuccessfully() {
        CreateRulePresetDTO dto = new CreateRulePresetDTO(10, "FULL_VIEW", 15, false);
        RaportLevelDict raportLevel = new RaportLevelDict();

        when(raportLevelDictRepository.findByName("FULL_VIEW")).thenReturn(raportLevel);

        ruleService.createRulePreset(dto);

        ArgumentCaptor<RulePreset> captor = ArgumentCaptor.forClass(RulePreset.class);
        verify(ruleRepository, times(1)).save(captor.capture());

        RulePreset savedPreset = captor.getValue();
        assertTrue(savedPreset.getIsPreset());
        assertNull(savedPreset.getSubject());
        assertEquals(10, savedPreset.getStudentTicketCount());
        assertEquals(raportLevel, savedPreset.getRaportLevel());
        assertEquals(15, savedPreset.getMinimumTokensMatch());
        assertFalse(savedPreset.getEnableNormalization());
    }

    @Test
    @DisplayName("Powinno pomyślnie utworzyć szablon z prawidłowymi wartościami brzegowymi (0 ticketów, 1 token)")
    public void shouldCreateRulePresetTemplateWithBoundaryValuesSuccessfully() {
        CreateRulePresetDTO dto = new CreateRulePresetDTO(0, "RESTRICTED_VIEW", 1, true);
        RaportLevelDict raportLevel = new RaportLevelDict();

        when(raportLevelDictRepository.findByName("RESTRICTED_VIEW")).thenReturn(raportLevel);

        ruleService.createRulePreset(dto);

        ArgumentCaptor<RulePreset> captor = ArgumentCaptor.forClass(RulePreset.class);
        verify(ruleRepository, times(1)).save(captor.capture());

        RulePreset savedPreset = captor.getValue();
        assertTrue(savedPreset.getIsPreset());
        assertNull(savedPreset.getSubject());
        assertEquals(0, savedPreset.getStudentTicketCount());
        assertEquals(1, savedPreset.getMinimumTokensMatch());
        assertTrue(savedPreset.getEnableNormalization());
    }

    @Test
    @DisplayName("Powinno rzucić RulePresetInvalidPartametersException, gdy brakuje co najmniej jednego wymaganego parametru")
    public void shouldThrowRulePresetInvalidParametersExceptionWhenMissingParams() {
        CreateRulePresetDTO dtoMissingTicket = new CreateRulePresetDTO(null, "FULL_VIEW", 20, false);
        CreateRulePresetDTO dtoMissingLevel = new CreateRulePresetDTO(10, null, 20, false);
        CreateRulePresetDTO dtoMissingTokens = new CreateRulePresetDTO(10, "FULL_VIEW", null, false);
        CreateRulePresetDTO dtoMissingNormalization = new CreateRulePresetDTO(10, "FULL_VIEW", 20, null);

        assertThrows(RulePresetInvalidPartametersException.class, () -> ruleService.createRulePreset(dtoMissingTicket));
        assertThrows(RulePresetInvalidPartametersException.class, () -> ruleService.createRulePreset(dtoMissingLevel));
        assertThrows(RulePresetInvalidPartametersException.class, () -> ruleService.createRulePreset(dtoMissingTokens));
        assertThrows(RulePresetInvalidPartametersException.class, () -> ruleService.createRulePreset(dtoMissingNormalization));

        verify(ruleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Powinno rzucić RaportLevelDictNotFoundException, gdy podany poziom raportu nie istnieje")
    public void shouldThrowExceptionWhenRaportLevelMissing() {
        CreateRulePresetDTO dto = new CreateRulePresetDTO(10, "INVALID_LEVEL", 20, false);

        when(raportLevelDictRepository.findByName("INVALID_LEVEL")).thenReturn(null);

        assertThrows(RaportLevelDictNotFoundException.class, () -> ruleService.createRulePreset(dto));
        verify(ruleRepository, never()).save(any());
    }
}