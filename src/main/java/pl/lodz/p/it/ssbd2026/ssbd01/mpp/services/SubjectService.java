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
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.RepositoryMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.RuleMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.SubjectMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.*;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.GiteaIntegrationException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectAccessDeniedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel.AccessLevelNotAssignedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.raportLevel.RaportLevelDictNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.rulePreset.RulePresetTemplateNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.subject.SubjectAssignmentNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.subject.SubjectMustHaveOwnerException;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.TransactionLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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
    private final StudentRepository studentRepository;
    private final RepositoryMapper repositoryMapper;


    private final SubjectMapper subjectMapper;
    private final RuleMapper ruleMapper;

    private void internalSyncWithGitea(Subject subject) {
        List<GiteaRepoDTO> giteaRepos = giteaService.getRepositoriesFromOrganization(subject.getOrganizationName());

        for (GiteaRepoDTO giteaRepo : giteaRepos) {
            Repository repository = subject.getRepositoryList().stream()
                    .filter(r -> r.getRepositoryName().equals(giteaRepo.name()))
                    .findFirst()
                    .orElse(null);

            if (repository == null) {
                repository = new Repository();
                repository.setRepositoryName(giteaRepo.name());
                repository.setSubject(subject);
                repository.setStudentList(new ArrayList<>());
                subject.getRepositoryList().add(repository);
            }

            List<GiteaUserDTO> collaborators = giteaService.getRepoCollaborators(subject.getOrganizationName(), giteaRepo.name());

            repository.getStudentList().clear();

            for (GiteaUserDTO collaborator : collaborators) {
                String email = collaborator.email() != null ? collaborator.email() : "";

                Optional<Student> studentOpt = studentRepository.findByAccountEmailIgnoreCase(email);

                if (studentOpt.isPresent()) {
                    Student student = studentOpt.get();
                    if (!repository.getStudentList().contains(student)) {
                        repository.getStudentList().add(student);
                    }
                }
            }
        }
    }

    @MethodCallLogged
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasAnyAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER, T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mppTransactionManager")
    public void syncSubjectWithGitea(UUID subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(SubjectNotFoundException::new);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        boolean isAdmin = hasRole(authentication.getAuthorities(), AccountConsts.ROLE_ADMIN);

        boolean isOwnerOrEditor = subject.getSubjectAssignmentList().stream()
                .anyMatch(a -> a.getTeacher().getAccount().getLogin().equals(currentUsername) &&
                        (a.getSubjectRole() == TeacherSubjectRole.OWNER || a.getSubjectRole() == TeacherSubjectRole.EDITOR));

        if (!isAdmin && !isOwnerOrEditor) {
            throw new SubjectAccessDeniedException();
        }

        internalSyncWithGitea(subject);
        subjectRepository.saveAndFlush(subject);
    }

    @MethodCallLogged
    @PreAuthorize("hasAnyAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER, T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
    public List<SubjectDTO> getTeacherSubjects() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String login = authentication.getName();
        boolean isAdmin = hasRole(authentication.getAuthorities(), AccountConsts.ROLE_ADMIN);

        List<SubjectDTO> result = new ArrayList<>();

        if (isAdmin) {
            List<Subject> subjects = subjectRepository.findAll();
            for (Subject subject : subjects) {
                String hash = versionHashService.hashObjectVersion(subject.getId(), subject.getVersion());
                result.add(subjectMapper.toSubjectDTO(subject, hash, true, true, true, null, null));
            }
        } else {
            List<SubjectAssignment> assignments = subjectAssignmentRepository.findAllByTeacherLogin(login);
            for (SubjectAssignment assignment : assignments) {
                Subject subject = assignment.getSubject();
                TeacherSubjectRole role = assignment.getSubjectRole();
                boolean canEdit = (role == TeacherSubjectRole.OWNER || role == TeacherSubjectRole.EDITOR);
                boolean canManageTeachers = (role == TeacherSubjectRole.OWNER);
                String hash = versionHashService.hashObjectVersion(subject.getId(), subject.getVersion());
                result.add(subjectMapper.toSubjectDTO(subject, hash, canEdit, canManageTeachers, true, null, null));
            }
        }

        return result;
    }

    @MethodCallLogged
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
    public List<SubjectDTO> getStudentSubjects() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String login = authentication.getName();

        List<SubjectDTO> result = new ArrayList<>();
        List<Subject> subjects = subjectRepository.findAllByStudentLogin(login);

        for (Subject subject : subjects) {
            String hash = versionHashService.hashObjectVersion(subject.getId(), subject.getVersion());
            result.add(subjectMapper.toSubjectDTO(subject, hash, false, false, false, null, null));
        }

        return result;
    }

    @MethodCallLogged
    @PreAuthorize("hasAnyAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN, T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
    public SubjectDTO getSubjectDetails(UUID subjectId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String login = authentication.getName();
        var authorities = authentication.getAuthorities();

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(SubjectNotFoundException::new);

        UUID templateId = null;
        RulePresetDTO manualRules = null;
        RulePreset preset = subject.getRulePreset();

        if (preset != null) {
            if (preset.getIsPreset()) {
                templateId = preset.getId();
            } else {
                manualRules = ruleMapper.toRuleDTO(preset);
            }
        }

        String hash = versionHashService.hashObjectVersion(subject.getId(), subject.getVersion());

        if (hasRole(authorities, AccountConsts.ROLE_ADMIN)) {
            return subjectMapper.toSubjectDTO(subject, hash, true, true, true, templateId, manualRules);
        }

        if (hasRole(authorities, AccountConsts.ROLE_TEACHER)) {
            TeacherSubjectRole role = subjectAssignmentRepository.findByTeacherLoginAndSubjectId(login, subjectId)
                    .stream()
                    .findFirst()
                    .map(SubjectAssignment::getSubjectRole)
                    .orElseThrow(SubjectAccessDeniedException::new);

            return subjectMapper.toSubjectDTO(
                    subject,
                    hash,
                    role == TeacherSubjectRole.OWNER || role == TeacherSubjectRole.EDITOR,
                    role == TeacherSubjectRole.OWNER,
                    true,
                    templateId,
                    manualRules
            );
        }

        throw new SubjectAccessDeniedException();
    }


    @MethodCallLogged
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
    public StudentSubjectDetailsDTO getStudentSubjectDetails(UUID subjectId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String login = authentication.getName();

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(SubjectNotFoundException::new);

        if (!subjectRepository.isStudentAssignedToSubject(subjectId, login)) {
            throw new SubjectAccessDeniedException();
        }

        Repository studentRepo = subject.getRepositoryList().stream()
                .filter(repo -> repo.getStudentList().stream()
                        .anyMatch(s -> s.getAccount().getLogin().equals(login)))
                .findFirst()
                .orElse(null);

        String visibilityLevel = "NOTHING";
        if (subject.getRulePreset() != null && subject.getRulePreset().getRaportLevel() != null) {
            visibilityLevel = subject.getRulePreset().getRaportLevel().getName();
        }

        return new StudentSubjectDetailsDTO(
                subject.getId(),
                subject.getName(),
                subject.getOrganizationName(),
                subject.getEdition(),
                subject.getSubjectDescription(),
                subject.getGiteaURL(),
                studentRepo != null ? repositoryMapper.toDto(studentRepo) : null,
                visibilityLevel
        );
    }

    private boolean hasRole(Collection<? extends GrantedAuthority> authorities, String roleName) {
        return authorities.stream().anyMatch(auth -> auth.getAuthority().equals(roleName));
    }


    private ReportLevelDict fetchRaportLevel(String name) {
        return raportLevelRepository.findByName(name)
                .orElseGet(() -> raportLevelRepository.findAll().stream()
                        .findFirst()
                        .orElseThrow(RaportLevelDictNotFoundException::new));
    }

    private Subject createBaseSubject(String name, String orgName, String edition, String desc, String url) {
        Subject subject = new Subject();
        subject.setName(name);
        subject.setOrganizationName(orgName);
        subject.setEdition(edition);
        subject.setSubjectDescription(desc);
        subject.setGiteaURL(url);
        subject.setArchived(false);
        subject.setSubjectAssignmentList(new ArrayList<>());
        return subject;
    }

    private void assignTeachers(Subject subject, Teacher creator, String creatorLogin, List<TeacherAssignmentDTO> teachersDto) {
        SubjectAssignment creatorAssignment = new SubjectAssignment();
        creatorAssignment.setSubject(subject);
        creatorAssignment.setTeacher(creator);
        creatorAssignment.setSubjectRole(TeacherSubjectRole.OWNER);
        subject.getSubjectAssignmentList().add(creatorAssignment);

        if (teachersDto != null) {
            for (TeacherAssignmentDTO tDto : teachersDto) {
                if (tDto.login().equals(creatorLogin)) continue;
                Teacher additionalTeacher = teacherRepository.findActiveTeacherByLogin(tDto.login())
                        .orElseThrow(AccessLevelNotAssignedException::new);
                SubjectAssignment newAssignment = new SubjectAssignment();
                newAssignment.setSubject(subject);
                newAssignment.setTeacher(additionalTeacher);
                newAssignment.setSubjectRole(tDto.role());
                subject.getSubjectAssignmentList().add(newAssignment);
            }
        }
    }


    @MethodCallLogged
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mppTransactionManager")
    public void createSubjectManual(CreateSubjectManualDTO dto, String creatorLogin) {
        Teacher teacher = teacherRepository.findActiveTeacherByLogin(creatorLogin)
                .orElseThrow(AccessLevelNotAssignedException::new);

        String generatedGiteaUrl = "https://team-1.proj-sum.it.p.lodz.pl/git/" + dto.organizationName();
        Subject subject = createBaseSubject(dto.name(), dto.organizationName(), dto.edition(), dto.subjectDescription(), generatedGiteaUrl);

        RulePreset preset = new RulePreset();
        preset.setRaportLevel(fetchRaportLevel(dto.manualRules().raportLevelName()));
        preset.setStudentTicketCount(dto.manualRules().studentTicketCount());
        preset.setMinimumTokensMatch(dto.manualRules().minimumTokensMatch());
        preset.setEnableNormalization(dto.manualRules().enableNormalization());
        preset.setIsPreset(false);
        preset.setSubject(subject);
        subject.setRulePreset(preset);

        assignTeachers(subject, teacher, creatorLogin, dto.teachers());
        internalSyncWithGitea(subject);
        subjectRepository.saveAndFlush(subject);
    }

    @MethodCallLogged
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mppTransactionManager")
    public void createSubjectFromTemplate(CreateSubjectTemplateDTO dto, String creatorLogin) {
        Teacher teacher = teacherRepository.findActiveTeacherByLogin(creatorLogin)
                .orElseThrow(AccessLevelNotAssignedException::new);

        String generatedGiteaUrl = "https://team-1.proj-sum.it.p.lodz.pl/git/" + dto.organizationName();
        Subject subject = createBaseSubject(dto.name(), dto.organizationName(), dto.edition(), dto.subjectDescription(), generatedGiteaUrl);

        RulePreset template = ruleRepository.findById(dto.templateId())
                .orElseThrow(RulePresetTemplateNotFoundException::new);

        RulePreset preset = new RulePreset();
        preset.setRaportLevel(fetchRaportLevel(dto.raportLevelName()));
        preset.setStudentTicketCount(template.getStudentTicketCount());
        preset.setMinimumTokensMatch(template.getMinimumTokensMatch());
        preset.setEnableNormalization(template.getEnableNormalization());
        preset.setIsPreset(false);
        preset.setSubject(subject);
        subject.setRulePreset(preset);

        assignTeachers(subject, teacher, creatorLogin, dto.teachers());
        internalSyncWithGitea(subject);
        subjectRepository.saveAndFlush(subject);
    }


    @MethodCallLogged
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasAnyAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER, T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mppTransactionManager")
    public void updateSubject(UUID subjectId, UpdateSubjectDTO dto, String versionHash) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(SubjectNotFoundException::new);

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

        subject.setName(dto.name());
        subject.setSubjectDescription(dto.subjectDescription());

        if (dto.rules() != null) {
            RulePreset preset = subject.getRulePreset();
            preset.setRaportLevel(fetchRaportLevel(dto.rules().raportLevelName()));
            preset.setStudentTicketCount(dto.rules().studentTicketCount());
            preset.setMinimumTokensMatch(dto.rules().minimumTokensMatch());
            preset.setEnableNormalization(dto.rules().enableNormalization());
            preset.setIsPreset(false);
        }

        if (isAdmin || isOwner) {
            List<SubjectAssignment> currentAssignments = subject.getSubjectAssignmentList();

            List<SubjectAssignment> assignmentsToRemove = currentAssignments.stream()
                    .filter(assignment -> dto.teachers().stream()
                            .noneMatch(t -> t.login().equals(assignment.getTeacher().getAccount().getLogin())))
                    .toList();

            currentAssignments.removeAll(assignmentsToRemove);
            subjectAssignmentRepository.deleteAll(assignmentsToRemove);

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
                giteaService.deleteGiteaOrganization(subject.getOrganizationName());
            } catch (Exception e) {
                log.error("Błąd komunikacji z Gitea dla organizacji: {}", subject.getOrganizationName(), e);
                throw new GiteaIntegrationException(e);
            }
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

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, transactionManager = "mppTransactionManager")
    @Retryable(retryFor = {JpaSystemException.class, ConcurrentUpdateException.class}, backoff = @Backoff(delayExpression = "${app.retry.backoff}"), maxAttemptsExpression = "${app.retry.maxattempts}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    public void changeSubjectManager(UUID subjectId, String newManagerLogin, String versionHash) {

        Subject subject = subjectRepository.findById(subjectId).orElseThrow(SubjectNotFoundException::new);
        if (!versionHash.equals(versionHashService.hashObjectVersion(subjectId, subject.getVersion()))) {
            throw new ConcurrentUpdateException();
        }

        Teacher oldOwner = teacherRepository.findSubjectOwnerBySubjectId(subjectId)
                .orElseThrow(SubjectAssignmentNotFoundException::new);
        SubjectAssignment oldOwnerAssignment = subjectAssignmentRepository.findByTeacherLoginAndSubjectId(oldOwner.getAccount().getLogin(), subjectId)
                .orElseThrow(SubjectAssignmentNotFoundException::new);

        Teacher newOwner = teacherRepository.findActiveTeacherByLogin(newManagerLogin)
                .orElseThrow(AccessLevelNotAssignedException::new);

        SubjectAssignment newOwnerAssignment = subjectAssignmentRepository.findByTeacherLoginAndSubjectId(newOwner.getAccount().getLogin(), subjectId)
                .orElseGet(
                        () -> {
                            SubjectAssignment assignment = new SubjectAssignment();
                            assignment.setSubject(subject);
                            assignment.setTeacher(newOwner);
                            return assignment;
                        }
                );

        oldOwnerAssignment.setSubjectRole(TeacherSubjectRole.VIEWER);
        newOwnerAssignment.setSubjectRole(TeacherSubjectRole.OWNER);

        subjectAssignmentRepository.saveAndFlush(oldOwnerAssignment);
        subjectAssignmentRepository.saveAndFlush(newOwnerAssignment);
    }

    @MethodCallLogged
    @PreAuthorize("hasAnyAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER, T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mppTransactionManager")
    public List<TeacherAssignmentDTO> getSubjectUsers(UUID subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(SubjectNotFoundException::new);

        return subject.getSubjectAssignmentList().stream()
                .map(a -> new TeacherAssignmentDTO(a.getTeacher().getAccount().getLogin(), a.getSubjectRole()))
                .toList();
    }
}