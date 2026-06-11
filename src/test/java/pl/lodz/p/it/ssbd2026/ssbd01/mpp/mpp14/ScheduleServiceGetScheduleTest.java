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
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.ScheduleMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.projections.ScheduleProjectionListDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.schedule.ScheduleDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Schedule;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.SubjectAssignment;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.NoPermissionForThisSubjectException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.ScheduleRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectAssignmentRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.ScheduleService;

import java.time.LocalDateTime;
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

    @Mock
    private ScheduleMapper scheduleMapper;

    @Mock
    private VersionHashService versionHashService;

    @InjectMocks
    private ScheduleService scheduleService;

    private UUID subjectId;

    @BeforeEach
    public void setUp() {
        subjectId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("teacher1");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void POS01_shouldReturnSchedulesWhenTeacherIsAssignedToSubject() {
        UUID scheduleId = UUID.randomUUID();
        LocalDateTime dateTime = LocalDateTime.now().plusDays(1);
        String tag = "v1.0";
        Integer version = 1;

        ScheduleProjectionListDTO projectedSchedule = new ScheduleProjectionListDTO(scheduleId, dateTime, tag, version, false, false);

        ScheduleDTO mappedDTO = new ScheduleDTO(scheduleId, dateTime, tag,
                "valid-hash-123", false, false);
        String expectedHash = "valid-hash-123";

        when(subjectAssignmentRepository.findByTeacherLoginAndSubjectId("teacher1", subjectId))
                .thenReturn(Optional.of(new SubjectAssignment()));

        when(scheduleRepository.findAllProjectedBySubjectId(subjectId)).thenReturn(List.of(projectedSchedule));

        when(versionHashService.hashObjectVersion(scheduleId, version)).thenReturn(expectedHash);


        List<ScheduleDTO> schedules = scheduleService.getSchedules(subjectId);

        assertAll(
                () -> assertNotNull(schedules),
                () -> assertEquals(1, schedules.size()),
                () -> assertEquals(expectedHash, schedules.getFirst().versionHash()),
                () -> verify(scheduleRepository, times(1)).findAllProjectedBySubjectId(subjectId)
        );
    }

    @Test
    public void POS02_ShouldReturnEmptyListWhenNoSchedules() {
        when(subjectAssignmentRepository.findByTeacherLoginAndSubjectId("teacher1", subjectId))
                .thenReturn(Optional.of(new SubjectAssignment()));

        when(scheduleRepository.findAllProjectedBySubjectId(subjectId)).thenReturn(List.of());

        List<ScheduleDTO> schedules = scheduleService.getSchedules(subjectId);

        assertAll(
                () -> assertNotNull(schedules),
                () -> assertTrue(schedules.isEmpty()),
                () -> verify(scheduleRepository, times(1)).findAllProjectedBySubjectId(subjectId)
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