package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.CreateSubjectManualDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.GiteaRepoDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.GiteaUserDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.RulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectAccessDeniedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel.AccessLevelNotAssignedException;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private SubjectAssignmentRepository subjectAssignmentRepository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private RaportLevelRepository raportLevelRepository;
    @Mock
    private RuleRepository ruleRepository;
    @Mock
    private GiteaService giteaService;
    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private SubjectService subjectService;

    private CreateSubjectManualDTO validDto;
    private final String TEACHER_LOGIN = "szponcik";

    @BeforeEach
    void setUp() {
        RulePresetDTO rules = new RulePresetDTO(UUID.randomUUID(), "PEŁNY_WGLĄD", 1, 1, true, 1, "hash123");

        validDto = new CreateSubjectManualDTO(
                "SSBD",
                "elo_zelo_org",
                "2026",
                "64 osoby IAS",
                "https://gitea.example.com/ssbd",
                rules,
                null
        );
    }

    @Test
    void shouldSuccessfullyCreateSubjectAndAssignTeacher() {
        Teacher teacher = new Teacher();
        when(teacherRepository.findActiveTeacherByLogin(TEACHER_LOGIN)).thenReturn(Optional.of(teacher));

        ReportLevelDict mockDict = new ReportLevelDict();
        mockDict.setName("PEŁNY_WGLĄD");
        when(raportLevelRepository.findByName(anyString())).thenReturn(Optional.of(mockDict));

        when(giteaService.getRepositoriesFromOrganization(anyString())).thenReturn(List.of());

        subjectService.createSubjectManual(validDto, TEACHER_LOGIN);

        assertAll(
                () -> verify(subjectRepository, times(1)).saveAndFlush(any(Subject.class))
        );
    }

    @Test
    void shouldThrowExceptionWhenTeacherNotFound() {
        String notTeacherLogin = "skibidi";
        when(teacherRepository.findActiveTeacherByLogin(notTeacherLogin)).thenReturn(Optional.empty());

        assertThrows(AccessLevelNotAssignedException.class, () ->
                subjectService.createSubjectManual(validDto, notTeacherLogin)
        );
    }

    @Test
    @DisplayName("POS02: Powinien zsynchronizować repozytoria z Gitea i przypisać studentów")
    void POS02_ShouldSyncWithGiteaAndAssignStudents() {
        UUID subjectId = UUID.randomUUID();
        Subject subject = new Subject();
        ReflectionTestUtils.setField(subject, "id", subjectId);
        subject.setOrganizationName("test-org");

        Teacher teacher = new Teacher();
        Account teacherAcc = new Account();
        teacherAcc.setLogin(TEACHER_LOGIN);
        teacher.setAccount(teacherAcc);

        SubjectAssignment assignment = new SubjectAssignment();
        assignment.setTeacher(teacher);
        assignment.setSubjectRole(TeacherSubjectRole.OWNER);
        subject.getSubjectAssignmentList().add(assignment);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(TEACHER_LOGIN);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));

        GiteaRepoDTO repoDTO = new GiteaRepoDTO("grupa-01", "https://git.local/grupa-01", false);
        when(giteaService.getRepositoriesFromOrganization("test-org")).thenReturn(List.of(repoDTO));

        GiteaUserDTO userDTO = new GiteaUserDTO("jkowalski", "jkowalski@edu.pl");
        when(giteaService.getRepoCollaborators("test-org", "grupa-01")).thenReturn(List.of(userDTO));

        Student student = new Student();
        when(studentRepository.findByAccountEmailIgnoreCaseOrAccountLoginIgnoreCase("jkowalski@edu.pl", "jkowalski"))
                .thenReturn(Optional.of(student));

        subjectService.syncSubjectWithGitea(subjectId);

        assertEquals(1, subject.getRepositoryList().size());
        assertEquals("grupa-01", subject.getRepositoryList().get(0).getRepositoryName());
        assertEquals(1, subject.getRepositoryList().get(0).getStudentList().size());
        verify(subjectRepository, times(1)).saveAndFlush(subject);
    }

    @Test
    @DisplayName("NEG02: Powinien rzucić błąd braku uprawnień, gdy synchronizację wywołuje ktoś z zewnątrz")
    void NEG02_ShouldThrowExceptionWhenSyncingWithoutPermissions() {
        UUID subjectId = UUID.randomUUID();
        Subject subject = new Subject();
        ReflectionTestUtils.setField(subject, "id", subjectId);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.getName()).thenReturn("random_student");
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));

        assertThrows(SubjectAccessDeniedException.class, () ->
                subjectService.syncSubjectWithGitea(subjectId)
        );
    }
}