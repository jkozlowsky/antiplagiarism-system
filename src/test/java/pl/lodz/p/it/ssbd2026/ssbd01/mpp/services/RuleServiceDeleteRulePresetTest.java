package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.rulePreset.RulePresetNoAccessException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.rulePreset.RulePresetNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RuleRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RuleServiceDeleteRulePresetTest {

    @Mock
    private RuleRepository ruleRepository;

    @Mock
    private VersionHashService versionHashService;

    @InjectMocks
    private RuleService ruleService;

    private RulePreset rulePreset;
    private UUID ruleId;
    private final String validHash = "valid-hash-123";

    @BeforeEach
    void setUp() {
        ruleId = UUID.randomUUID();

        rulePreset = new RulePreset();
        ReflectionTestUtils.setField(rulePreset, "id", ruleId);
        rulePreset.setIsPreset(true);
        rulePreset.setSubject(null);
        ReflectionTestUtils.setField(rulePreset, "version", 1);
    }

    @Test
    @DisplayName("POS01: Powinien pomyślnie usunąć globalny szablon reguł")
    void POS01_deleteRulePreset_ShouldDeleteSuccessfully() {
        when(ruleRepository.findById(ruleId)).thenReturn(Optional.of(rulePreset));
        when(versionHashService.hashObjectVersion(ruleId, rulePreset.getVersion())).thenReturn(validHash);

        ruleService.deleteRulePreset(ruleId, validHash);

        verify(ruleRepository, times(1)).delete(rulePreset);
        verify(ruleRepository, times(1)).flush();
    }

    @Test
    @DisplayName("NEG01: Powinien rzucić wyjątek, gdy szablon nie istnieje")
    void NEG01_deleteRulePreset_ShouldThrowWhenNotFound() {
        when(ruleRepository.findById(ruleId)).thenReturn(Optional.empty());

        assertThrows(RulePresetNotFoundException.class, () ->
                ruleService.deleteRulePreset(ruleId, validHash)
        );

        verify(ruleRepository, never()).delete(any());
    }

    @Test
    @DisplayName("NEG02: Powinien rzucić wyjątek przy niezgodnym nagłówku ETag")
    void NEG02_deleteRulePreset_ShouldThrowOnConcurrentUpdate() {
        when(ruleRepository.findById(ruleId)).thenReturn(Optional.of(rulePreset));
        when(versionHashService.hashObjectVersion(ruleId, rulePreset.getVersion())).thenReturn("invalid-hash");

        assertThrows(ConcurrentUpdateException.class, () ->
                ruleService.deleteRulePreset(ruleId, validHash)
        );

        verify(ruleRepository, never()).delete(any());
    }

    @Test
    @DisplayName("NEG03: Powinien rzucić wyjątek, gdy próbuje usunąć reguły przypisane do przedmiotu (isPreset = false)")
    void NEG03_deleteRulePreset_ShouldThrowWhenNotATemplate() {
        rulePreset.setIsPreset(false);

        when(ruleRepository.findById(ruleId)).thenReturn(Optional.of(rulePreset));
        when(versionHashService.hashObjectVersion(ruleId, rulePreset.getVersion())).thenReturn(validHash);

        assertThrows(RulePresetNoAccessException.class, () ->
                ruleService.deleteRulePreset(ruleId, validHash)
        );

        verify(ruleRepository, never()).delete(any());
    }
}