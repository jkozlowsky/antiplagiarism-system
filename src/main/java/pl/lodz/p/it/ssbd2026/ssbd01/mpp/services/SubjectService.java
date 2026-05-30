package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.ssbd2026.ssbd01.common.utils.PostCommitTransactionManager;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.RuleMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.SubjectMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.RulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.SubjectDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.TeacherAssignmentDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.UpdateSubjectDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccountNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel.AccessLevelNotAssignedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.raportLevel.RaportLevelDictNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectAccessDeniedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.rulePreset.RulePresetInvalidPartametersException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.rulePreset.RulePresetTemplateNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.subject.SubjectMustHaveOwnerException;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.*;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;

import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@MethodCallLogged
@TransactionLogged
@Slf4j
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
@Retryable(retryFor = {JpaSystemException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final VersionHashService versionHashService;
    private final PostCommitTransactionManager postCommitTransactionManager;
    private final GiteaService giteaService;
    private final SubjectAssignmentRepository subjectAssignmentRepository;
    private final TeacherRepository teacherRepository;
    private final RaportLevelRepository raportLevelRepository;
    private final RuleRepository ruleRepository;

    private final SubjectMapper subjectMapper;
    private final RuleMapper ruleMapper;


    public void syncSubjectWithGitea(UUID subjectId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @MethodCallLogged
    @PreAuthorize("hasAnyAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN, T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER, T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
    public List<SubjectDTO> getAllSubjects() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String login = authentication.getName();
        var authorities = authentication.getAuthorities();

        boolean isAdmin = hasRole(authorities, AccountConsts.ROLE_ADMIN);
        boolean isTeacher = hasRole(authorities, AccountConsts.ROLE_TEACHER);
        boolean isStudent = hasRole(authorities, AccountConsts.ROLE_STUDENT);

        List<SubjectDTO> result = new ArrayList<>();

        if (isAdmin) {
            List<Subject> subjects = subjectRepository.findAll();
            for (Subject subject : subjects) {
                result.add(subjectMapper.toSubjectDTO(subject, true, true, true, null, null));
            }
        } else if (isTeacher) {
            List<SubjectAssignment> assignments = subjectAssignmentRepository.findAllByTeacherLogin(login);
            for (SubjectAssignment assignment : assignments) {
                Subject subject = assignment.getSubject();
                TeacherSubjectRole role = assignment.getSubjectRole();
                boolean canEdit = (role == TeacherSubjectRole.OWNER || role == TeacherSubjectRole.EDITOR);
                boolean canManageTeachers = (role == TeacherSubjectRole.OWNER);
                result.add(subjectMapper.toSubjectDTO(subject, canEdit, canManageTeachers, true, null, null));
            }
        } else if (isStudent) {
            List<Subject> subjects = subjectRepository.findAllByStudentLogin(login);
            for (Subject subject : subjects) {
                result.add(subjectMapper.toSubjectDTO(subject, false, false, false, null, null));
            }
        } else {
            throw new SubjectAccessDeniedException();
        }

        return result;
    }

    @MethodCallLogged
    @PreAuthorize("hasAnyAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN, T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER, T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
    public SubjectDTO getSubjectDetails(UUID subjectId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String login = authentication.getName();
        var authorities = authentication.getAuthorities();

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(SubjectNotFoundException::new);

        boolean canEdit = false;
        boolean canManageTeachers = false;
        boolean canViewStats = false;

        if (hasRole(authorities, AccountConsts.ROLE_ADMIN)) {
            canEdit = true;
            canManageTeachers = true;
            canViewStats = true;
        } else if (hasRole(authorities, AccountConsts.ROLE_TEACHER)) {
            TeacherSubjectRole role = subjectAssignmentRepository.findByTeacherLoginAndSubjectId(login, subjectId)
                    .stream()
                    .findFirst()
                    .map(SubjectAssignment::getSubjectRole)
                    .orElseThrow(SubjectAccessDeniedException::new);

            canEdit = (role == TeacherSubjectRole.OWNER || role == TeacherSubjectRole.EDITOR);
            canManageTeachers = (role == TeacherSubjectRole.OWNER);
            canViewStats = true;
        } else if (hasRole(authorities, AccountConsts.ROLE_STUDENT)) {
            if (!subjectRepository.isStudentAssignedToSubject(subjectId, login)) {
                throw new SubjectAccessDeniedException();
            }
        } else {
            throw new SubjectAccessDeniedException();
        }

        UUID templateId = null;
        RulePresetDTO manualRules = null;

        if (canViewStats) {
            RulePreset preset = subject.getRulePreset();
            if (preset != null) {
                if (Boolean.TRUE.equals(preset.getIsPreset())) {
                    templateId = preset.getId();
                } else {
                    manualRules = ruleMapper.toRuleDTO(preset);
                }
            }
        }

        SubjectDTO dto = subjectMapper.toSubjectDTO(
                subject,
                canEdit,
                canManageTeachers,
                canViewStats,
                templateId,
                manualRules
        );

        String hash = versionHashService.hashObjectVersion(subject.getId(), subject.getVersion());

        return new SubjectDTO(
                dto.id(), hash, dto.name(), dto.organizationName(), dto.edition(),
                dto.subjectDescription(), dto.giteaURL(), dto.archived(), dto.createdAt(),
                dto.updatedAt(), dto.createdBy(), dto.modifiedBy(), dto.templateId(),
                dto.manualRules(), dto.canEdit(), dto.canManageTeachers(), dto.canViewStats()
        );
    }

    private boolean hasRole(Collection<? extends GrantedAuthority> authorities, String roleName) {
        return authorities.stream().anyMatch(auth -> auth.getAuthority().equals(roleName));
    }

    @MethodCallLogged
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mppTransactionManager")
    public void createSubject(SubjectDTO subjectDto, String creatorLogin) {

        Teacher teacher = teacherRepository.findActiveTeacherByLogin(creatorLogin)
                .orElseThrow(AccessLevelNotAssignedException::new);

        Subject subject = new Subject();
        subject.setName(subjectDto.name());
        subject.setOrganizationName(subjectDto.organizationName());
        subject.setEdition(subjectDto.edition());
        subject.setSubjectDescription(subjectDto.subjectDescription());
        subject.setGiteaURL(subjectDto.giteaURL());
        subject.setArchived(false);

        RulePreset preset = new RulePreset();

        if (subjectDto.templateId() != null) {
            RulePreset template = ruleRepository.findById(subjectDto.templateId())
                    .orElseThrow(RulePresetTemplateNotFoundException::new);

            preset.setRaportLevel(template.getRaportLevel());
            preset.setStudentTicketCount(template.getStudentTicketCount());
            preset.setMinimumTokensMatch(template.getMinimumTokensMatch());
            preset.setEnableNormalization(template.getEnableNormalization());
        }
        else if (subjectDto.manualRules() != null) {
            RaportLevelDict raportLevel = raportLevelRepository.findAll().stream()
                    .filter(rl -> rl.getName().equals(subjectDto.manualRules().raportLevelName()))
                    .findFirst()
                    .orElseThrow(RaportLevelDictNotFoundException::new);

            preset.setRaportLevel(raportLevel);
            preset.setStudentTicketCount(subjectDto.manualRules().studentTicketCount());
            preset.setMinimumTokensMatch(subjectDto.manualRules().minimumTokensMatch());
            preset.setEnableNormalization(subjectDto.manualRules().enableNormalization());
        }
        else {
            // Wymuszenie wartości domyślnych, gdy brak podanych
            RaportLevelDict raportLevel = raportLevelRepository.findAll().stream()
                    .filter(rl -> rl.getName().equals("FULL"))
                    .findFirst()
                    .orElseThrow(RaportLevelDictNotFoundException::new);

            preset.setRaportLevel(raportLevel);
            preset.setStudentTicketCount(10);
            preset.setMinimumTokensMatch(0);
            preset.setEnableNormalization(false);
        }

        preset.setIsPreset(false);
        preset.setSubject(subject);
        preset.setScheduleList(new ArrayList<>());

        subject.setRulePreset(preset);

        // Kaskada samoistnie zapisze RulePreset
        subjectRepository.saveAndFlush(subject);

        SubjectAssignment assignment = new SubjectAssignment();
        assignment.setSubject(subject);
        assignment.setTeacher(teacher);
        assignment.setSubjectRole(TeacherSubjectRole.OWNER);

        subjectAssignmentRepository.saveAndFlush(assignment);
    }

    @MethodCallLogged
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasAnyAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER, T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mppTransactionManager")
    public void updateSubject(UUID subjectId, UpdateSubjectDTO dto, String versionHash) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(SubjectNotFoundException::new);

        // WALIDACJA HASHA
        if (!versionHash.equals(versionHashService.hashObjectVersion(subjectId, subject.getVersion()))) {
            throw new ConcurrentUpdateException();
        }

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        boolean isAdmin = hasRole(authentication.getAuthorities(), AccountConsts.ROLE_ADMIN);

        boolean isOwner = subject.getSubjectAssignmentList().stream()
                .anyMatch(a -> a.getTeacher().getAccount().getLogin().equals(currentUsername) && a.getSubjectRole() == TeacherSubjectRole.OWNER);
        boolean isEditor = subject.getSubjectAssignmentList().stream()
                .anyMatch(a -> a.getTeacher().getAccount().getLogin().equals(currentUsername) && a.getSubjectRole() == TeacherSubjectRole.EDITOR);

        if (!isAdmin && !isOwner && !isEditor) {
            throw new SubjectAccessDeniedException();
        }

        // Zapisywanie z DTO będącego rekordem
        subject.setName(dto.name());
        subject.setSubjectDescription(dto.subjectDescription());
        subject.setGiteaURL(dto.giteaURL());

        if (dto.rules() != null) {
            RaportLevelDict raportLevel = raportLevelRepository.findAll().stream()
                    .filter(rl -> rl.getName().equals(dto.rules().raportLevelName()))
                    .findFirst()
                    .orElseThrow(RaportLevelDictNotFoundException::new);

            RulePreset preset = subject.getRulePreset();
            if (preset == null) {
                preset = new RulePreset();
                preset.setSubject(subject);
                subject.setRulePreset(preset);
            }

            preset.setRaportLevel(raportLevel);
            preset.setStudentTicketCount(dto.rules().studentTicketCount());
            preset.setMinimumTokensMatch(dto.rules().minimumTokensMatch());
            preset.setEnableNormalization(dto.rules().enableNormalization());
            preset.setIsPreset(false);
        }

        if (isAdmin || isOwner) {
            List<SubjectAssignment> currentAssignments = subject.getSubjectAssignmentList();

            currentAssignments.removeIf(assignment ->
                    dto.teachers().stream().noneMatch(t -> t.login().equals(assignment.getTeacher().getAccount().getLogin()))
            );

            for (TeacherAssignmentDTO teacherDto : dto.teachers()) {
                Optional<SubjectAssignment> existing = currentAssignments.stream()
                        .filter(a -> a.getTeacher().getAccount().getLogin().equals(teacherDto.login()))
                        .findFirst();

                if (existing.isPresent()) {
                    existing.get().setSubjectRole(teacherDto.role());
                } else {
                    Teacher teacher = teacherRepository.findActiveTeacherByLogin(teacherDto.login())
                            .orElseThrow(AccessLevelNotAssignedException::new);

                    SubjectAssignment newAssignment = new SubjectAssignment();
                    newAssignment.setSubject(subject);
                    newAssignment.setTeacher(teacher);
                    newAssignment.setSubjectRole(teacherDto.role());
                    currentAssignments.add(newAssignment);
                }
            }

            boolean hasOwner = currentAssignments.stream()
                    .anyMatch(a -> a.getSubjectRole() == TeacherSubjectRole.OWNER);
            if (!hasOwner) {
                throw new SubjectMustHaveOwnerException();
            }
        }

        subjectRepository.saveAndFlush(subject);
    }

    @MethodCallLogged
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mppTransactionManager")
    public void deleteSubject(UUID subjectId, String versionHash, String currentUsername, boolean deleteGiteaOrg) {

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(SubjectNotFoundException::new);

        if (!versionHash.equals(versionHashService.hashObjectVersion(subjectId, subject.getVersion()))) {
            throw new ConcurrentUpdateException();
        }

        boolean hasPermission = subject.getSubjectAssignmentList().stream()
                .anyMatch(assignment ->
                        assignment.getTeacher().getAccount().getLogin().equals(currentUsername) &&
                                assignment.getSubjectRole().equals(TeacherSubjectRole.OWNER)
                );

        if (!hasPermission) {
            throw new SubjectAccessDeniedException();
        }

        List<String> reportPaths = subject.getReportList().stream()
                .map(Report::getPathToReport)
                .filter(Objects::nonNull)
                .toList();

        if (deleteGiteaOrg) {
            try {
                giteaService.deleteGiteaOrganization(subject.getName());
            } catch (Exception e) {
                log.error("Nie udało się usunąć przedmiotu z serwera Gitea: {}", subject.getName(), e);
                throw new RuntimeException("Błąd podczas usuwania przedmiotu z Gitea", e);
            }
        } else {
            log.info("Pominięto usuwanie organizacji na serwerze Gitea dla przedmiotu: {}", subject.getName());
        }

        subjectRepository.delete(subject);
        subjectRepository.flush();

        postCommitTransactionManager.register(() -> {
            for (String path : reportPaths) {
                try {
                    Files.deleteIfExists(Paths.get(path));
                    log.info("Plik raportu został trwale usunięty z dysku: {}", path);
                } catch (IOException e) {
                    log.error("Nie udało się usunąć fizycznego pliku z dysku po usunięciu przedmiotu. Ścieżka: {}", path, e);
                }
            }
        });
    }

    public void calculateAggregatedValues(UUID subjectId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void changeSubjectManager(UUID subjectId, UUID newManagerId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @MethodCallLogged
    @PreAuthorize("hasAnyAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER, T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
    public List<TeacherAssignmentDTO> getSubjectUsers(UUID subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(SubjectNotFoundException::new);

        return subject.getSubjectAssignmentList().stream()
                .map(a -> new TeacherAssignmentDTO(a.getTeacher().getAccount().getLogin(), a.getSubjectRole()))
                .collect(Collectors.toList());
    }
}