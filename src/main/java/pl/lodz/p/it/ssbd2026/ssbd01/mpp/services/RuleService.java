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
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.CreateRulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.RulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RaportLevelDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.raportLevel.RaportLevelDictNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.rulePreset.RulePresetInvalidPartametersException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.rulePreset.RulePresetTemplateNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.subject.SubjectNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RaportLevelDictRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RuleRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectRepository;

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
    private final SubjectRepository subjectRepository;
    private final RaportLevelDictRepository raportLevelDictRepository;
    private final RuleMapper ruleMapper;

    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mppTransactionManager")
    public List<RulePresetDTO> getRulePresetsTemplates() {
        return ruleRepository.findAllByIsPreset(true).stream()
                .map(ruleMapper::toRuleDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mppTransactionManager")
    public void createRulePreset(UUID subjectId, CreateRulePresetDTO dto) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(SubjectNotFoundException::new);

        RulePreset newPreset = new RulePreset();
        newPreset.setIsPreset(false);
        newPreset.setSubject(subject);

        if (dto.presetId() != null) {
            RulePreset template = ruleRepository.findById(dto.presetId())
                    .orElseThrow(RulePresetTemplateNotFoundException::new);

            newPreset.setRaportLevel(template.getRaportLevel());
            newPreset.setStudentTicketCount(template.getStudentTicketCount());
            newPreset.setMinimumTokensMatch(template.getMinimumTokensMatch());
            newPreset.setEnableNormalization(template.getEnableNormalization());
        }
        else {
            if (dto.raportLevelName() == null || dto.studentTicketCount() == null ||
                    dto.minimumTokensMatch() == null || dto.enableNormalization() == null) {
                throw new RulePresetInvalidPartametersException();
            }

            RaportLevelDict raportLevel = raportLevelDictRepository.findByName(dto.raportLevelName());
            if (raportLevel == null) {
                throw new RaportLevelDictNotFoundException();
            }

            newPreset.setRaportLevel(raportLevel);
            newPreset.setStudentTicketCount(dto.studentTicketCount());
            newPreset.setMinimumTokensMatch(dto.minimumTokensMatch());
            newPreset.setEnableNormalization(dto.enableNormalization());
        }

        ruleRepository.save(newPreset);
    }

    public void updateRulePreset(UUID ruleId, Object ruleDto) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void deleteRulePreset(UUID ruleId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}