package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.RuleMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.RulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.rulePreset.RulePresetNoAccessException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.rulePreset.RulePresetNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RuleRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectAssignmentRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@MethodCallLogged
@TransactionLogged
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
@Retryable(retryFor = {JpaSystemException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
public class RuleService {

    private final RuleRepository ruleRepository;
    private final RuleMapper ruleMapper;

    private final VersionHashService versionHashService;
    private final SubjectAssignmentRepository subjectAssignmentRepository;

    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public List<RulePresetDTO> getRulePresetsTemplates() {
        return ruleRepository.findAllByIsPreset(true).stream()
                .map(ruleMapper::toRuleDTO)
                .collect(Collectors.toList());
    }

    public void createRulePreset(UUID subjectId, Object ruleDto) {
        throw new UnsupportedOperationException("Not implemented yet");
    }


    public void updateRulePreset(UUID ruleId, Object ruleDto) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @MethodCallLogged
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")
    public void deleteRulePreset(UUID ruleId, String versionHash, String currentUsername) {

        RulePreset rulePreset = ruleRepository.findById(ruleId)
                .orElseThrow(RulePresetNotFoundException::new);

        if (!versionHash.equals(versionHashService.hashObjectVersion(ruleId, rulePreset.getVersion()))) {
            throw new ConcurrentUpdateException();
        }

        Subject subject = rulePreset.getSubject();

        if (subject == null) {
            throw new RulePresetNoAccessException();
        }

        boolean hasPermission = subjectAssignmentRepository.existsBySubjectAndTeacherAccountLoginAndHasEditPermissionsTrue(subject, currentUsername);

        if (!hasPermission) {
            throw new RulePresetNoAccessException();
        }

        subject.setRulePreset(null);
        ruleRepository.delete(rulePreset);
        ruleRepository.flush();
    }
}