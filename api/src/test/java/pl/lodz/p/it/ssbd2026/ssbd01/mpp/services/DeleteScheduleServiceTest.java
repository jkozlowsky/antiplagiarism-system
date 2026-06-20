package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Schedule;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.NoPermissionToEditScheduleException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ScheduleNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.ScheduleRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectAssignmentRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeleteScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private SubjectAssignmentRepository subjectAssignmentRepository;

    @Mock
    private VersionHashService versionHashService;

    @InjectMocks
    private ScheduleService scheduleService;

    @Test
    @DisplayName("Powinno pomyślnie usunąć harmonogram, gdy użytkownik ma uprawnienia i wersja się zgadza")
    public void shouldDeleteScheduleSuccessfully() {
        UUID scheduleId = UUID.randomUUID();
        String userLogin = "teacher1";
        String validHash = "valid-version-hash";

        Subject subject = new Subject();
        RulePreset rulePreset = new RulePreset();
        rulePreset.setSubject(subject);

        Schedule schedule = new Schedule();
        ReflectionTestUtils.setField(schedule, "version", 1);
        schedule.setRulePreset(rulePreset);
        schedule.setModifiableUntil(LocalDateTime.now().plusDays(1));

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(versionHashService.hashObjectVersion(scheduleId, schedule.getVersion())).thenReturn(validHash);
        when(subjectAssignmentRepository.hasEditPermissions(subject, userLogin)).thenReturn(true);

        scheduleService.deleteSchedule(scheduleId, userLogin, validHash);

        verify(scheduleRepository, times(1)).delete(schedule);
    }

    @Test
    @DisplayName("Powinno rzucić ConcurrentUpdateException, gdy nagłówek wersji nie pasuje")
    public void shouldThrowConcurrentUpdateExceptionWhenVersionMismatch() {
        UUID scheduleId = UUID.randomUUID();
        String userLogin = "teacher1";
        String wrongHash = "wrong-version-hash";

        Subject subject = new Subject();
        RulePreset rulePreset = new RulePreset();
        rulePreset.setSubject(subject);

        Schedule schedule = new Schedule();
        ReflectionTestUtils.setField(schedule, "version", 1);
        schedule.setRulePreset(rulePreset);
        schedule.setModifiableUntil(LocalDateTime.now().plusDays(1));

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(versionHashService.hashObjectVersion(scheduleId, schedule.getVersion())).thenReturn("actual-valid-hash");

        assertThrows(
                ConcurrentUpdateException.class,
                () -> scheduleService.deleteSchedule(scheduleId, userLogin, wrongHash)
        );

        verify(scheduleRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Powinno rzucić ScheduleNotFoundException, gdy harmonogram nie istnieje")
    public void shouldThrowExceptionWhenScheduleNotFound() {
        UUID scheduleId = UUID.randomUUID();
        String userLogin = "teacher1";

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());

        ScheduleNotFoundException exception = assertThrows(
                ScheduleNotFoundException.class,
                () -> scheduleService.deleteSchedule(scheduleId, userLogin, "any-hash")
        );

        assertEquals("schedule.not.found.message", exception.getReason());
        verify(scheduleRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Powinno rzucić NoPermissionToEditScheduleException, gdy nauczyciel nie ma praw edycji przedmiotu")
    public void shouldThrowExceptionWhenNoPermissions() {
        UUID scheduleId = UUID.randomUUID();
        String userLogin = "teacher1";
        String validHash = "valid-version-hash";

        Subject subject = new Subject();
        RulePreset rulePreset = new RulePreset();
        rulePreset.setSubject(subject);

        Schedule schedule = new Schedule();
        ReflectionTestUtils.setField(schedule, "version", 1);
        schedule.setRulePreset(rulePreset);
        schedule.setModifiableUntil(LocalDateTime.now().plusDays(1));

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(versionHashService.hashObjectVersion(scheduleId, schedule.getVersion())).thenReturn(validHash);
        when(subjectAssignmentRepository.hasEditPermissions(subject, userLogin)).thenReturn(false);

        NoPermissionToEditScheduleException exception = assertThrows(
                NoPermissionToEditScheduleException.class,
                () -> scheduleService.deleteSchedule(scheduleId, userLogin, validHash)
        );

        assertEquals("schedule.cannot.edit", exception.getReason());
        verify(scheduleRepository, never()).delete(any());
    }
}