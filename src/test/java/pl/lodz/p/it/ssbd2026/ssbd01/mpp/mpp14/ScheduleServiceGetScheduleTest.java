package pl.lodz.p.it.ssbd2026.ssbd01.mpp.mpp14;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Schedule;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.SubjectAssignment;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.NoPermissionForThisSubjectException;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.ScheduleRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectAssignmentRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.ScheduleService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScheduleServiceGetScheduleTest {
    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private SubjectAssignmentRepository subjectAssignmentRepository;
    @InjectMocks
    private ScheduleService scheduleService;

    private UUID subjectId;

    @BeforeEach
    public void setUp() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("teacher1");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void POS01_shouldReturnSchedulesWhenTeacherIsAssignedToSubject() {
        when(subjectAssignmentRepository.findByTeacherLoginAndSubjectId("teacher1", subjectId))
                .thenReturn(Optional.of(new SubjectAssignment()));
        when(scheduleRepository.findAllBySubjectId(subjectId)).thenReturn(List.of(new Schedule()));

        List<Schedule> schedules = scheduleService.getSchedules(subjectId);

        assertAll(
                () -> assertNotNull(schedules),
                () -> assertEquals(1, schedules.size()),
                () -> verify(scheduleRepository, times(1)).findAllBySubjectId(subjectId)
        );
    }

    @Test
    public void POS02_ShouldReturnEmptyListWhenNoSchedules() {
        when(subjectAssignmentRepository.findByTeacherLoginAndSubjectId("teacher1", subjectId))
                .thenReturn(Optional.of(new SubjectAssignment()));
        when(scheduleRepository.findAllBySubjectId(subjectId)).thenReturn(List.of());

        List<Schedule> schedules = scheduleService.getSchedules(subjectId);

        assertAll(
                () -> assertNotNull(schedules),
                () -> assertTrue(schedules.isEmpty()),
                () -> verify(scheduleRepository, times(1)).findAllBySubjectId(subjectId)
        );
    }

    @Test
    public void NEG01_ShouldThrowExceptionWhenTeacherIsNotAssignedToAnySubject() {
        when(subjectAssignmentRepository.findByTeacherLoginAndSubjectId("teacher1", subjectId))
                .thenReturn(Optional.empty());

        assertAll(
                () -> assertThrows(NoPermissionForThisSubjectException.class,
                        () -> scheduleService.getSchedules(subjectId)),
                () -> verify(scheduleRepository, never()).findAllBySubjectId(any())
        );
    }
}
