package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.CreateRulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RaportLevelDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.subject.SubjectNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RaportLevelDictRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RuleRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectRepository;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@MethodCallLogged
@TransactionLogged
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
@Retryable(retryFor = {JpaSystemException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
public class RuleService {

    private final RuleRepository ruleRepository;
    private final SubjectRepository subjectRepository;
    private final RaportLevelDictRepository raportLevelDictRepository;

    public void getRules(UUID subjectId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mppTransactionManager")
    public void createRulePreset(UUID subjectId, CreateRulePresetDTO dto) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(SubjectNotFoundException::new);

        RaportLevelDict raportLevel = raportLevelDictRepository.findByName(dto.raportLevelName());
        if (raportLevel == null) {
            throw new SubjectNotFoundException();
        }

        RulePreset newPreset = new RulePreset();
        newPreset.setIsPreset(false);
        newPreset.setSubject(subject);
        newPreset.setRaportLevel(raportLevel);
        newPreset.setStudentTicketCount(dto.studentTicketCount());

        ruleRepository.save(newPreset);
    }


    public void updateRule(UUID ruleId, Object ruleDto) {
        throw new UnsupportedOperationException("Not implemented yet");
    }


    public void deleteRule(UUID ruleId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}