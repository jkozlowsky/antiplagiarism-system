package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.ScheduleMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.schedule.ScheduleDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Schedule;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.SubjectAssignment;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.*;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.schedule.UpdateScheduleDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.rulePreset.RulePresetNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.ScheduleRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectAssignmentRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@MethodCallLogged
@TransactionLogged
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
@Retryable(retryFor = {JpaSystemException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
public class ScheduleService  {

    private final ScheduleRepository scheduleRepository;
    private final SubjectAssignmentRepository subjectAssignmentRepository;
    private final SubjectRepository subjectRepository;
    private final ScheduleMapper scheduleMapper;
    private final VersionHashService versionHashService;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
    @Retryable(retryFor = {JpaSystemException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public List<ScheduleDTO> getSchedules(UUID subjectId) {
        String teacherLogin = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        Optional<SubjectAssignment> subjectAssignment = subjectAssignmentRepository.findByTeacherLoginAndSubjectId(teacherLogin, subjectId);

        if (subjectAssignment.isEmpty()) {
            throw new NoPermissionForThisSubjectException();
        }

        return scheduleRepository.findAllBySubjectId(subjectId).stream()
                .map(schedule -> {
                    ScheduleDTO dto = scheduleMapper.toScheduleDTO(schedule);
                    return new ScheduleDTO(
                            dto.id(),
                            dto.scheduleDateTime(),
                            dto.tag(),
                            versionHashService.hashObjectVersion(schedule.getId(), schedule.getVersion()),
                            dto.isExecuted(),
                            dto.hasFailed()
                    );
                })
                .toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")
    @Retryable(retryFor = {JpaSystemException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public ScheduleDTO createSchedule(UUID subjectId, Schedule schedule, String teacherLogin) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow(SubjectNotFoundException::new);

        if (!subjectAssignmentRepository.hasEditPermissions(subject, teacherLogin)) {
            throw new NoPermissionForThisSubjectException();
        }

        if (subject.getRulePreset() == null) {
            throw new RulePresetNotSetForSubjectException("error.rulePresetNotSetForSubject");
        }

        schedule.setRulePreset(subject.getRulePreset());
        Schedule savedSchedule = scheduleRepository.saveAndFlush(schedule);

        ScheduleDTO dto = scheduleMapper.toScheduleDTO(savedSchedule);

        return new ScheduleDTO(
                dto.id(),
                dto.scheduleDateTime(),
                dto.tag(),
                versionHashService.hashObjectVersion(savedSchedule.getId(), savedSchedule.getVersion()),
                dto.isExecuted(),
                dto.hasFailed()
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public void updateSchedule(UUID scheduleId, UpdateScheduleDTO scheduleDto, String userLogin) {
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(ScheduleNotFoundException::new);

        if (!scheduleDto.getVersionHash().equals(versionHashService.hashObjectVersion(scheduleId, schedule.getVersion()))) {
            throw new ConcurrentUpdateException();
        }

        Subject subject = schedule.getRulePreset().getSubject();
        boolean hasPermission = subjectAssignmentRepository.hasEditPermissions(subject, userLogin);

        if (!hasPermission) {
            throw new NoPermissionToEditScheduleException();
        }

        schedule.setScheduleDateTime(scheduleDto.getScheduleDateTime());
        schedule.setTag(scheduleDto.getTag());

        scheduleRepository.save(schedule);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mppTransactionManager")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public void deleteSchedule(UUID scheduleId, String userLogin, String versionHash) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(ScheduleNotFoundException::new);

        if (!versionHash.equals(versionHashService.hashObjectVersion(scheduleId, schedule.getVersion()))) {
            throw new ConcurrentUpdateException();
        }

        Subject subject = schedule.getRulePreset().getSubject();
        boolean hasPermission = subjectAssignmentRepository.hasEditPermissions(subject, userLogin);

        if (!hasPermission) {
            throw new NoPermissionToEditScheduleException();
        }

        scheduleRepository.delete(schedule);
    }
}