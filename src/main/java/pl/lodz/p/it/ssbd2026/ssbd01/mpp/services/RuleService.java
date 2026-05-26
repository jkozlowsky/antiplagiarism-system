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
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.UpdateRulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RaportLevelDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.raportLevel.RaportLevelDictNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.rulePreset.RulePresetInvalidPartametersException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.rulePreset.RulePresetNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RaportLevelDictRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RaportLevelRepository;
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
    private final RaportLevelRepository raportLevelRepository;

    private final VersionHashService versionHashService;
    private final SubjectAssignmentRepository subjectAssignmentRepository;

    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mppTransactionManager")
    public List<RulePresetDTO> getRulePresetsTemplates() {
        return ruleRepository.findAllByIsPreset(true).stream()
                .map(ruleMapper::toRuleDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mppTransactionManager")
    public void createRulePreset(CreateRulePresetDTO dto) {
        RulePreset newPreset = new RulePreset();
        newPreset.setIsPreset(true);
        newPreset.setSubject(null);

        if (dto.raportLevelName() == null || dto.studentTicketCount() == null ||
                dto.minimumTokensMatch() == null || dto.enableNormalization() == null) {
            throw new RulePresetInvalidPartametersException();
        }

        RaportLevelDict raportLevel = raportLevelDictRepository.findByName(dto.raportLevelName())
                .orElseThrow(RaportLevelDictNotFoundException::new);

        newPreset.setRaportLevel(raportLevel);
        newPreset.setStudentTicketCount(dto.studentTicketCount());
        newPreset.setMinimumTokensMatch(dto.minimumTokensMatch());
        newPreset.setEnableNormalization(dto.enableNormalization());

        ruleRepository.save(newPreset);
    }

    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")
    public void updateRulePreset(UUID ruleId, UpdateRulePresetDTO ruleDto) {
        RulePreset rulePreset = ruleRepository.findById(ruleId)
                .orElseThrow(RulePresetNotFoundException::new);

        RaportLevelDict raportLevel = raportLevelRepository.findByName(ruleDto.raportLevelName())
                .orElseThrow(RaportLevelDictNotFoundException::new);

        rulePreset.setStudentTicketCount(ruleDto.studentTicketCount());
        rulePreset.setMinimumTokensMatch(ruleDto.minimumTokensMatch());
        rulePreset.setEnableNormalization(ruleDto.enableNormalization());
        rulePreset.setRaportLevel(raportLevel);

        ruleRepository.saveAndFlush(rulePreset);
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