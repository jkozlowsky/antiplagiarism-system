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
public class RuleService  {

    
    public void getRules(UUID subjectId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    
    public void createRule(UUID subjectId, Object ruleDto) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    
    public void updateRule(UUID ruleId, Object ruleDto) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    
    public void deleteRule(UUID ruleId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}