package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.TransactionLogged;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@MethodCallLogged
@TransactionLogged
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
@Retryable(retryFor = {JpaSystemException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
public class ScheduleService  {

    
    public void getSchedules(UUID subjectId) {
        throw new UnsupportedOperationException("Not implemented yet");
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