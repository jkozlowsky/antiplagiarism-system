package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.schedule.UpdateScheduleDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Schedule;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceUpdateTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private SubjectAssignmentRepository subjectAssignmentRepository;

    @Mock
    private VersionHashService versionHashService;

    @InjectMocks
    private ScheduleService scheduleService;

    private Schedule mockSchedule;
    private Subject mockSubject;
    private UpdateScheduleDTO updateScheduleDTO;
    private final UUID scheduleId = UUID.randomUUID();
    private final String login = "scheduleupdatetester";

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
        updateScheduleDTO.setVersionHash("validHash");
    }

    @Test
    @DisplayName("Update schedule when user has permision and all provided data is valid")
    void POS01_updateSchedule() {
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(mockSchedule));
        when(subjectAssignmentRepository.hasEditPermissions(mockSubject, login))
                .thenReturn(true);
        when(versionHashService.hashObjectVersion(any(), any())).thenReturn("validHash");

        scheduleService.updateSchedule(scheduleId, updateScheduleDTO, login);

        assertEquals("newTag", mockSchedule.getTag());
        assertEquals(updateScheduleDTO.getScheduleDateTime(), mockSchedule.getScheduleDateTime());
        verify(scheduleRepository).save(mockSchedule);
    }

    @Test
    @DisplayName("Verify if, while trying to change a schedule without permissions it would be not possible and error would be thrown.")
    void NEG01_updateSchedule() {
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(mockSchedule));
        when(subjectAssignmentRepository.hasEditPermissions(mockSubject, login))
                .thenReturn(false);
        when(versionHashService.hashObjectVersion(any(), any())).thenReturn("validHash");

        assertThrows(NoPermissionToEditScheduleException.class,
                () -> scheduleService.updateSchedule(scheduleId, updateScheduleDTO, login));

        verify(scheduleRepository, never()).save(any(Schedule.class));
    }

    @Test
    @DisplayName("Verify if, not existing subject is validated properly.")
    void NEG02_updateSchedule() {
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());

        assertThrows(ScheduleNotFoundException.class,
                () -> scheduleService.updateSchedule(scheduleId, updateScheduleDTO, login));

        verify(subjectAssignmentRepository, never()).hasEditPermissions(any(), any());
        verify(scheduleRepository, never()).save(any());
    }
}