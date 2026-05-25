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
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.rulePreset.RulePresetNoAccessException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.rulePreset.RulePresetNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RuleRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectAssignmentRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RuleServiceDeleteRulePresetTest {

    @Mock
    private RuleRepository ruleRepository;

    @Mock
    private VersionHashService versionHashService;

    @Mock
    private SubjectAssignmentRepository subjectAssignmentRepository;

    @InjectMocks
    private RuleService ruleService;

    private RulePreset rulePreset;
    private Subject subject;
    private UUID ruleId;
    private final String currentUsername = "teacher1";
    private final String validHash = "valid-hash-123";

    @BeforeEach
    void setUp() {
        ruleId = UUID.randomUUID();
        subject = new Subject();
        ReflectionTestUtils.setField(subject, "id", UUID.randomUUID());

        rulePreset = new RulePreset();
        ReflectionTestUtils.setField(rulePreset, "id", ruleId);
        rulePreset.setSubject(subject);
        ReflectionTestUtils.setField(rulePreset, "version", 1);
    }

    @Test
    @DisplayName("POS01: Powinien pomyślnie usunąć zestaw reguł, jeśli spełnione są wszystkie warunki")
    void POS01_deleteRulePreset_ShouldDeleteSuccessfully() {
        when(ruleRepository.findById(ruleId)).thenReturn(Optional.of(rulePreset));
        when(versionHashService.hashObjectVersion(ruleId, rulePreset.getVersion())).thenReturn(validHash);
        when(subjectAssignmentRepository.existsBySubjectAndTeacherAccountLoginAndHasEditPermissionsTrue(subject, currentUsername)).thenReturn(true);

        ruleService.deleteRulePreset(ruleId, validHash, currentUsername);

        verify(ruleRepository, times(1)).delete(rulePreset);
        verify(ruleRepository, times(1)).flush();
    }

    @Test
    @DisplayName("NEG01: Powinien rzucić wyjątek RulePresetNotFoundException, gdy preset nie istnieje")
    void NEG01_deleteRulePreset_ShouldThrowWhenNotFound() {
        when(ruleRepository.findById(ruleId)).thenReturn(Optional.empty());

        assertThrows(RulePresetNotFoundException.class, () ->
                ruleService.deleteRulePreset(ruleId, validHash, currentUsername)
        );

        verify(ruleRepository, never()).delete(any());
    }

    @Test
    @DisplayName("NEG02: Powinien rzucić wyjątek ConcurrentUpdateException przy niezgodnym nagłówku ETag (blokada optymistyczna)")
    void NEG02_deleteRulePreset_ShouldThrowOnConcurrentUpdate() {
        when(ruleRepository.findById(ruleId)).thenReturn(Optional.of(rulePreset));
        when(versionHashService.hashObjectVersion(ruleId, rulePreset.getVersion())).thenReturn("invalid-hash");

        assertThrows(ConcurrentUpdateException.class, () ->
                ruleService.deleteRulePreset(ruleId, validHash, currentUsername)
        );

        verify(ruleRepository, never()).delete(any());
    }

    @Test
    @DisplayName("NEG03: Powinien rzucić wyjątek RulePresetNoAccessException, gdy preset to szablon bez przedmiotu")
    void NEG03_deleteRulePreset_ShouldThrowWhenSubjectIsNull() {
        rulePreset.setSubject(null);

        when(ruleRepository.findById(ruleId)).thenReturn(Optional.of(rulePreset));
        when(versionHashService.hashObjectVersion(ruleId, rulePreset.getVersion())).thenReturn(validHash);

        assertThrows(RulePresetNoAccessException.class, () ->
                ruleService.deleteRulePreset(ruleId, validHash, currentUsername)
        );

        verify(ruleRepository, never()).delete(any());
    }

    @Test
    @DisplayName("NEG04: Powinien rzucić wyjątek RulePresetNoAccessException, gdy użytkownik nie ma uprawnień do edycji przedmiotu")
    void NEG04_deleteRulePreset_ShouldThrowWhenNoEditPermission() {
        when(ruleRepository.findById(ruleId)).thenReturn(Optional.of(rulePreset));
        when(versionHashService.hashObjectVersion(ruleId, rulePreset.getVersion())).thenReturn(validHash);
        when(subjectAssignmentRepository.existsBySubjectAndTeacherAccountLoginAndHasEditPermissionsTrue(subject, currentUsername)).thenReturn(false);

        assertThrows(RulePresetNoAccessException.class, () ->
                ruleService.deleteRulePreset(ruleId, validHash, currentUsername)
        );

        verify(ruleRepository, never()).delete(any());
    }
}