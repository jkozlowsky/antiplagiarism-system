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
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Schedule;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.SubjectAssignment;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.NoPermissionForThisSubjectException;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.ScheduleRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectAssignmentRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectRepository;

import java.util.List;
import java.util.Objects;
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

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
    @Retryable(retryFor = {JpaSystemException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public List<Schedule> getSchedules(UUID subjectId) {
        String teacherLogin = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        List<SubjectAssignment> subjectAssignments = subjectAssignmentRepository.findByTeacherLoginAndSubjectId(teacherLogin, subjectId);
        if (subjectAssignments.isEmpty()) {
            throw new NoPermissionForThisSubjectException();
        }
        return scheduleRepository.findAllBySubjectId(subjectId);
    }

    
    public void createSchedule(UUID subjectId, Object scheduleDto) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    
    public void updateSchedule(UUID scheduleId, Object scheduleDto) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    
    public void deleteSchedule(UUID scheduleId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}