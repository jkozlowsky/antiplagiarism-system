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
public class SubjectService {

    public void syncSubjectWithGitea(UUID subjectId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void getAllSubjects() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    public void getSubjectDetails(UUID subjectId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void createSubject(Object subjectDto) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void updateSubject(UUID subjectId, Object subjectDto) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    public void deleteSubject(UUID subjectId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    public void calculateAggregatedValues(UUID subjectId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    public void changeSubjectManager(UUID subjectId, UUID newManagerId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}