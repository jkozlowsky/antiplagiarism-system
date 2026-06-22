package pl.lodz.p.it.ssbd2026.ssbd01.mpp.finentitymoded;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.ScheduleMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.schedule.ScheduleDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.schedule.UpdateScheduleDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Schedule;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ScheduleModificationDeadlineExceededException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.ScheduleRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectAssignmentRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.ScheduleService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleModificationTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private SubjectAssignmentRepository subjectAssignmentRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private ScheduleMapper scheduleMapper;

    @Mock
    private VersionHashService versionHashService;

    @InjectMocks
    private ScheduleService scheduleService;

    private Schedule schedule;
    private UUID scheduleId;
    private String userLogin;
    private String versionHash;

    @BeforeEach
    void setUp() {
        scheduleId = UUID.randomUUID();
        userLogin = "test_teacher";
        versionHash = "test-hash";

        Subject subject = new Subject();
        RulePreset rulePreset = new RulePreset();
        rulePreset.setSubject(subject);

        schedule = new Schedule();
        schedule.setRulePreset(rulePreset);
    }

    @Test
    void POS01_createSchedule_whenNoDeadlineSet_shouldSetModifiableUntilToScheduleDateTime() {
        LocalDateTime scheduleDateTime = LocalDateTime.now().plusDays(3);
        schedule.setScheduleDateTime(scheduleDateTime);

        Subject subject = new Subject();
        subject.setRulePreset(new RulePreset());
        UUID subjectId = UUID.randomUUID();

        ScheduleDTO mockDto = new ScheduleDTO(scheduleId, scheduleDateTime, "tag", versionHash, false, false);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(subjectAssignmentRepository.hasEditPermissions(any(), eq(userLogin))).thenReturn(true);
        when(scheduleRepository.saveAndFlush(any())).thenAnswer(inv -> {
            Schedule saved = inv.getArgument(0);
            assertEquals(scheduleDateTime, saved.getModifiableUntil());
            return saved;
        });
        when(scheduleMapper.toScheduleDTO(any())).thenReturn(mockDto);
        when(versionHashService.hashObjectVersion(any(), any())).thenReturn(versionHash);

        assertDoesNotThrow(() -> scheduleService.createSchedule(subjectId, schedule, userLogin));
    }

    @Test
    void POS02_createSchedule_whenModifiableUntilAlreadySet_shouldOverwriteWithScheduleDateTime() {
        LocalDateTime scheduleDateTime = LocalDateTime.now().plusDays(3);
        LocalDateTime existingDeadline = LocalDateTime.now().plusDays(1);
        schedule.setScheduleDateTime(scheduleDateTime);
        schedule.setModifiableUntil(existingDeadline);

        Subject subject = new Subject();
        subject.setRulePreset(new RulePreset());
        UUID subjectId = UUID.randomUUID();

        ScheduleDTO mockDto = new ScheduleDTO(scheduleId, scheduleDateTime, "tag", versionHash, false, false);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(subjectAssignmentRepository.hasEditPermissions(any(), eq(userLogin))).thenReturn(true);
        when(scheduleRepository.saveAndFlush(any())).thenAnswer(inv -> {
            Schedule saved = inv.getArgument(0);
            assertEquals(scheduleDateTime, saved.getModifiableUntil());
            return saved;
        });
        when(scheduleMapper.toScheduleDTO(any())).thenReturn(mockDto);
        when(versionHashService.hashObjectVersion(any(), any())).thenReturn(versionHash);

        assertDoesNotThrow(() -> scheduleService.createSchedule(subjectId, schedule, userLogin));
    }

    @Test
    void POS03_updateSchedule_whenModifiableUntilInFuture_shouldNotThrow() {
        schedule.setModifiableUntil(LocalDateTime.now().plusDays(1));

        UpdateScheduleDTO dto = new UpdateScheduleDTO();
        dto.setScheduleDateTime(LocalDateTime.now().plusDays(5));
        dto.setTag("new-tag");
        dto.setVersionHash(versionHash);

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(versionHashService.hashObjectVersion(any(), any())).thenReturn(versionHash);
        when(subjectAssignmentRepository.hasEditPermissions(any(), eq(userLogin))).thenReturn(true);
        when(scheduleRepository.save(any())).thenReturn(schedule);

        assertDoesNotThrow(() -> scheduleService.updateSchedule(scheduleId, dto, userLogin));
    }

    @Test
    void NEG01_updateSchedule_whenModifiableUntilInPast_shouldThrowScheduleModificationDeadlineExceededException() {
        schedule.setModifiableUntil(LocalDateTime.now().minusSeconds(1));

        UpdateScheduleDTO dto = new UpdateScheduleDTO();
        dto.setScheduleDateTime(LocalDateTime.now().plusDays(5));
        dto.setTag("tag");
        dto.setVersionHash(versionHash);

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

        assertThrows(ScheduleModificationDeadlineExceededException.class,
                () -> scheduleService.updateSchedule(scheduleId, dto, userLogin));
    }

    @Test
    void NEG02_updateSchedule_whenModifiableUntilJustExpired_shouldThrowScheduleModificationDeadlineExceededException() {
        schedule.setModifiableUntil(LocalDateTime.now().minusNanos(1));

        UpdateScheduleDTO dto = new UpdateScheduleDTO();
        dto.setScheduleDateTime(LocalDateTime.now().plusDays(5));
        dto.setTag("tag");
        dto.setVersionHash(versionHash);

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

        assertThrows(ScheduleModificationDeadlineExceededException.class,
                () -> scheduleService.updateSchedule(scheduleId, dto, userLogin));
    }

    @Test
    void POS04_deleteSchedule_whenModifiableUntilInFuture_shouldNotThrow() {
        schedule.setModifiableUntil(LocalDateTime.now().plusDays(1));

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(versionHashService.hashObjectVersion(any(), any())).thenReturn(versionHash);
        when(subjectAssignmentRepository.hasEditPermissions(any(), eq(userLogin))).thenReturn(true);

        assertDoesNotThrow(() -> scheduleService.deleteSchedule(scheduleId, userLogin, versionHash));
    }

    @Test
    void NEG03_deleteSchedule_whenModifiableUntilInPast_shouldThrowScheduleModificationDeadlineExceededException() {
        schedule.setModifiableUntil(LocalDateTime.now().minusSeconds(1));

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

        assertThrows(ScheduleModificationDeadlineExceededException.class,
                () -> scheduleService.deleteSchedule(scheduleId, userLogin, versionHash));
    }

    @Test
    void NEG04_deleteSchedule_whenDeadlineJustPassed_shouldBlockRemoval() {
        schedule.setModifiableUntil(LocalDateTime.now().minusNanos(1));

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

        assertThrows(ScheduleModificationDeadlineExceededException.class,
                () -> scheduleService.deleteSchedule(scheduleId, userLogin, versionHash));
    }
}