package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Schedule;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.NoPermissionToEditScheduleException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ScheduleNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.ScheduleRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectAssignmentRepository;

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

    @InjectMocks
    private ScheduleService scheduleService;

    @Test
    @DisplayName("Powinno pomyślnie usunąć harmonogram, gdy użytkownik ma uprawnienia")
    public void shouldDeleteScheduleSuccessfully() {
        UUID scheduleId = UUID.randomUUID();
        String userLogin = "teacher1";

        Subject subject = new Subject();
        RulePreset rulePreset = new RulePreset();
        rulePreset.setSubject(subject);

        Schedule schedule = new Schedule();
        schedule.setRulePreset(rulePreset);

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(subjectAssignmentRepository.existsBySubjectAndTeacherAccountLoginAndHasEditPermissionsTrue(subject, userLogin))
                .thenReturn(true);

        scheduleService.deleteSchedule(scheduleId, userLogin);

        verify(scheduleRepository, times(1)).delete(schedule);
    }

    @Test
    @DisplayName("Powinno rzucić ScheduleNotFoundException, gdy harmonogram nie istnieje")
    public void shouldThrowExceptionWhenScheduleNotFound() {
        UUID scheduleId = UUID.randomUUID();
        String userLogin = "teacher1";

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());

        ScheduleNotFoundException exception = assertThrows(
                ScheduleNotFoundException.class,
                () -> scheduleService.deleteSchedule(scheduleId, userLogin)
        );

        assertEquals("schedule.not.found.message", exception.getReason());
        verify(scheduleRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Powinno rzucić NoPermissionToEditScheduleException, gdy nauczyciel nie ma praw edycji przedmiotu")
    public void shouldThrowExceptionWhenNoPermissions() {
        UUID scheduleId = UUID.randomUUID();
        String userLogin = "teacher1";

        Subject subject = new Subject();
        RulePreset rulePreset = new RulePreset();
        rulePreset.setSubject(subject);

        Schedule schedule = new Schedule();
        schedule.setRulePreset(rulePreset);

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(subjectAssignmentRepository.existsBySubjectAndTeacherAccountLoginAndHasEditPermissionsTrue(subject, userLogin))
                .thenReturn(false);

        NoPermissionToEditScheduleException exception = assertThrows(
                NoPermissionToEditScheduleException.class,
                () -> scheduleService.deleteSchedule(scheduleId, userLogin)
        );

        assertEquals("schedule.cannot.edit", exception.getReason());
        verify(scheduleRepository, never()).delete(any());
    }
}