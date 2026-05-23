package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.UpdateScheduleDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Schedule;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.NoPermissionToEditScheduleException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ScheduleNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.ScheduleRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectAssignmentRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceUpdateTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private SubjectAssignmentRepository subjectAssignmentRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    private Schedule mockSchedule;
    private Subject mockSubject;
    private UpdateScheduleDTO updateScheduleDTO;
    private final UUID scheduleId = UUID.randomUUID();
    private final String teacherLogin = "scheduleupdatetester";

    @BeforeEach
    void setUp() {
        mockSubject = new Subject();

        RulePreset mockRulePreset = new RulePreset();
        mockRulePreset.setSubject(mockSubject);

        mockSchedule = new Schedule();
        mockSchedule.setRulePreset(mockRulePreset);
        mockSchedule.setTag("oldTag");

        updateScheduleDTO = new UpdateScheduleDTO();
        updateScheduleDTO.setScheduleDateTime(LocalDateTime.now().plusDays(5));
        updateScheduleDTO.setTag("newTag");

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn(teacherLogin);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Update schedule when user has permision and all provided data is valid")
    void POS01_updateSchedule() {
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(mockSchedule));
        when(subjectAssignmentRepository.existsBySubjectAndTeacherAccountLoginAndHasEditPermissionsTrue(mockSubject, teacherLogin))
                .thenReturn(true);

        scheduleService.updateSchedule(scheduleId, updateScheduleDTO);

        assertEquals("newTag", mockSchedule.getTag());
        assertEquals(updateScheduleDTO.getScheduleDateTime(), mockSchedule.getScheduleDateTime());
        verify(scheduleRepository).save(mockSchedule);
    }

    @Test
    @DisplayName("Verify if, while trying to change a schedule without permissions it would be not possible and error would be thrown.")
    void NEG01_updateSchedule() {
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(mockSchedule));
        when(subjectAssignmentRepository.existsBySubjectAndTeacherAccountLoginAndHasEditPermissionsTrue(mockSubject, teacherLogin))
                .thenReturn(false);

        assertThrows(NoPermissionToEditScheduleException.class,
                () -> scheduleService.updateSchedule(scheduleId, updateScheduleDTO));

        verify(scheduleRepository, never()).save(any(Schedule.class));
    }

    @Test
    @DisplayName("Verify if, not existing subject is validated properly.")
    void NEG02_updateSchedule() {
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());

        assertThrows(ScheduleNotFoundException.class,
                () -> scheduleService.updateSchedule(scheduleId, updateScheduleDTO));

        verify(subjectAssignmentRepository, never()).existsBySubjectAndTeacherAccountLoginAndHasEditPermissionsTrue(any(), any());
        verify(scheduleRepository, never()).save(any());
    }
}