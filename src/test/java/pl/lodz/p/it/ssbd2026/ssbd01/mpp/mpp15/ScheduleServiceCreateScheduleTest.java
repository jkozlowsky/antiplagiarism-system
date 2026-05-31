package pl.lodz.p.it.ssbd2026.ssbd01.mpp.mpp15;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.ScheduleMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.schedule.ScheduleDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Schedule;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.NoPermissionForThisSubjectException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.ScheduleRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectAssignmentRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.ScheduleService;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScheduleServiceCreateScheduleTest {
    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private SubjectAssignmentRepository subjectAssignmentRepository;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private ScheduleMapper scheduleMapper;
    @InjectMocks
    private ScheduleService scheduleService;

    private Subject mockSubject;
    private RulePreset mockRulePreset;
    private UUID mockSubjectId = UUID.randomUUID();
    private ScheduleDTO scheduleDTO;
    private static final String TEST_TEACHER_LOGIN = "test_teacher";
    private final Principal principal = Mockito.mock(Principal.class);

    @BeforeEach
    public void setUp() {
        mockSubject = new Subject();
        mockRulePreset = new RulePreset();
        mockSubjectId = UUID.randomUUID();
        mockRulePreset.setSubject(mockSubject);
        mockSubject.setRulePreset(mockRulePreset);
        scheduleDTO = new ScheduleDTO(UUID.randomUUID(), LocalDateTime.now().plusMinutes(1), "zad1");
    }

    @Test
    public void POS01_CreateSchedule_Successfully() {
        Schedule testSchedule = new Schedule();
        testSchedule.setScheduleDateTime(scheduleDTO.scheduleDateTime());
        testSchedule.setTag(scheduleDTO.tag());

        when(principal.getName()).thenReturn(TEST_TEACHER_LOGIN);
        when(subjectRepository.findById(mockSubjectId)).thenReturn(Optional.of(mockSubject));
        when(subjectAssignmentRepository.hasEditPermissions(mockSubject, TEST_TEACHER_LOGIN))
                .thenReturn(true);
        when(scheduleMapper.toSchedule(Mockito.any(ScheduleDTO.class))).thenReturn(testSchedule);
        when(scheduleRepository.saveAndFlush(Mockito.any(Schedule.class))).thenReturn(testSchedule);

        Schedule result = scheduleService.createSchedule(mockSubjectId, scheduleMapper.toSchedule(scheduleDTO), principal.getName());

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("zad1", result.getTag()),
                () -> assertEquals(scheduleDTO.scheduleDateTime(), result.getScheduleDateTime()),
                () -> assertEquals(mockRulePreset, result.getRulePreset()),
                () -> verify(scheduleRepository, times(1)).saveAndFlush(Mockito.any(Schedule.class))
        );
    }

    @Test
    public void NEG01_CreateSchedule_WhenSubjectNotFound() {
        UUID invalidSubjectId = UUID.randomUUID();
        when(subjectRepository.findById(invalidSubjectId)).thenReturn(Optional.empty());
        when(principal.getName()).thenReturn(TEST_TEACHER_LOGIN);

        assertThrows(SubjectNotFoundException.class,
                () -> scheduleService.createSchedule(invalidSubjectId, scheduleMapper.toSchedule(scheduleDTO), principal.getName()));

        verify(subjectRepository, times(1)).findById(invalidSubjectId);
        verify(scheduleRepository, never()).saveAndFlush(Mockito.any(Schedule.class));
        verify(subjectAssignmentRepository, never())
                .hasEditPermissions(Mockito.any(), Mockito.any());
    }

    @Test
    public void NEG02_CreateSchedule_NoPermissionToEditSubject() {
        Schedule testSchedule = new Schedule();
        testSchedule.setScheduleDateTime(scheduleDTO.scheduleDateTime());
        testSchedule.setTag(scheduleDTO.tag());

        when(principal.getName()).thenReturn(TEST_TEACHER_LOGIN);
        when(subjectRepository.findById(mockSubjectId)).thenReturn(Optional.of(mockSubject));
        when(subjectAssignmentRepository.hasEditPermissions(mockSubject, TEST_TEACHER_LOGIN))
                .thenReturn(false);

        assertThrows(NoPermissionForThisSubjectException.class,
                () -> scheduleService.createSchedule(mockSubjectId, testSchedule, principal.getName()));

        verify(scheduleMapper, never()).toSchedule(any(ScheduleDTO.class));
        verify(scheduleRepository, never()).saveAndFlush(any());
    }
}
