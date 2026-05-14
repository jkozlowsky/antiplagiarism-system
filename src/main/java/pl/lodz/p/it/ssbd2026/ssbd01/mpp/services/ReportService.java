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
public class ReportService  {

    public void generateReportOnDemand(UUID subjectId, String tag) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    
    public void getAllReports(UUID subjectId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    
    public void getReportDetails(UUID reportId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    
    public void deleteReport(UUID reportId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    
    public void getStudentOwnReports(UUID studentId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    
    public void generateReportBySchedule() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}