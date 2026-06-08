package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.RepositoryMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.RuleMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.SubjectMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.StudentSubjectDetailsDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.SubjectDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.SubjectAssignment;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.TeacherSubjectRole;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectAccessDeniedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectAssignmentRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectDescriptionTranslationRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectServiceBrowsingTest {

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private SubjectAssignmentRepository subjectAssignmentRepository;

    @Mock
    private SubjectMapper subjectMapper;

    @Mock
    private RuleMapper ruleMapper;

    @Mock
    private RepositoryMapper repositoryMapper;

    @Mock
    private VersionHashService versionHashService;

    @Mock
    private SubjectDescriptionTranslationRepository subjectDescriptionTranslationRepository;

    @Mock
    private pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.ScheduleRepository scheduleRepository;

    @InjectMocks
    private SubjectService subjectService;

    private UUID subjectId;
    private Subject subject;
    private final String validHash = "mockedHash123";

    @BeforeEach
    void setUp() {
        subjectId = UUID.randomUUID();

        Account creator = new Account();
        creator.setLogin("test_admin");

        subject = new Subject();
        org.springframework.test.util.ReflectionTestUtils.setField(subject, "id", subjectId);
        org.springframework.test.util.ReflectionTestUtils.setField(subject, "version", 1);
        org.springframework.test.util.ReflectionTestUtils.setField(subject, "createdBy", creator);

        subject.setName("Architektura Systemów Komputerowych");
        subject.setEdition("2026/2027");
        subject.setOrganizationName("ask-org-2026");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(String username, String role) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        lenient().when(authentication.getName()).thenReturn(username);
        GrantedAuthority authority = new SimpleGrantedAuthority(role);
        lenient().doReturn(Collections.singletonList(authority)).when(authentication).getAuthorities();
    }

    @Test
    @DisplayName("POS01_getSubjectDetails: Admin powinien otrzymać detale przedmiotu z pełnymi prawami")
    void POS01_AdminShouldGetSubjectDetailsWithAllPermissions() {
        mockSecurityContext("test_admin", "ADMIN");
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        lenient().when(versionHashService.hashObjectVersion(any(), any())).thenReturn(validHash);

        SubjectDTO mockedDto = mock(SubjectDTO.class);
        when(mockedDto.canEdit()).thenReturn(true);
        when(mockedDto.canManageTeachers()).thenReturn(true);
        when(mockedDto.canViewStats()).thenReturn(true);

        when(subjectMapper.toSubjectDTO(subject, validHash, true, true, true, null, null, "", null)).thenReturn(mockedDto);

        SubjectDTO result = subjectService.getSubjectDetails(subjectId);

        assertTrue(result.canEdit());
        assertTrue(result.canManageTeachers());
        assertTrue(result.canViewStats());
    }

    @Test
    @DisplayName("POS02_getSubjectDetails: Nauczyciel VIEWER nie ma praw do edycji i zarządzania")
    void POS02_TeacherViewerShouldGetDetailsWithoutEditPermissions() {
        mockSecurityContext("test_teacher", "TEACHER");
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));

        SubjectAssignment assignment = new SubjectAssignment();
        assignment.setSubject(subject);
        assignment.setSubjectRole(TeacherSubjectRole.VIEWER);

        when(subjectAssignmentRepository.findByTeacherLoginAndSubjectId("test_teacher", subjectId))
                .thenReturn(Optional.of(assignment));
        lenient().when(versionHashService.hashObjectVersion(any(), any())).thenReturn(validHash);

        SubjectDTO mockedDto = mock(SubjectDTO.class);
        when(mockedDto.canEdit()).thenReturn(false);
        when(mockedDto.canManageTeachers()).thenReturn(false);

        when(subjectMapper.toSubjectDTO(subject, validHash, false, false, true, null, null, "", null)).thenReturn(mockedDto);

        SubjectDTO result = subjectService.getSubjectDetails(subjectId);

        assertFalse(result.canEdit());
        assertFalse(result.canManageTeachers());
    }

    @Test
    @DisplayName("POS04_getStudentSubjectDetails: Student poprawnie przypisany otrzymuje bezpieczne detale przedmiotu")
    void POS04_AssignedStudentShouldGetDetails() {
        mockSecurityContext("test_student", "STUDENT");
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(subjectRepository.isStudentAssignedToSubject(subjectId, "test_student")).thenReturn(true);

        StudentSubjectDetailsDTO result = subjectService.getStudentSubjectDetails(subjectId);

        assertNotNull(result);
        assertEquals("Architektura Systemów Komputerowych", result.name());
        assertEquals("NOTHING", result.reportVisibilityLevel());
    }

    @Test
    @DisplayName("NEG01_getSubjectDetails: Rzuca SubjectNotFoundException gdy przedmiot nie istnieje")
    void NEG01_ShouldThrowWhenSubjectNotFound() {
        mockSecurityContext("test_teacher", "TEACHER");
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        assertThrows(SubjectNotFoundException.class, () ->
                subjectService.getSubjectDetails(subjectId)
        );
    }

    @Test
    @DisplayName("NEG02_getStudentSubjectDetails: Student nieprzypisany do przedmiotu dostaje AccessDenied")
    void NEG02_UnassignedStudentShouldThrowAccessDenied() {
        mockSecurityContext("test_student", "STUDENT");
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(subjectRepository.isStudentAssignedToSubject(subjectId, "test_student")).thenReturn(false);

        assertThrows(SubjectAccessDeniedException.class, () ->
                subjectService.getStudentSubjectDetails(subjectId)
        );
    }

    @Test
    @DisplayName("NEG03_getSubjectDetails: Prowadzący nieprzypisany do przedmiotu dostaje AccessDenied")
    void NEG03_UnassignedTeacherShouldThrowAccessDenied() {
        mockSecurityContext("test_teacher", "TEACHER");
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(subjectAssignmentRepository.findByTeacherLoginAndSubjectId("test_teacher", subjectId))
                .thenReturn(Optional.empty());

        assertThrows(SubjectAccessDeniedException.class, () ->
                subjectService.getSubjectDetails(subjectId)
        );
    }

    @Test
    @DisplayName("NEG04_getSubjectDetails: Student próbujący pobrać detale dla nauczyciela dostaje AccessDenied")
    void NEG04_StudentTryingToGetTeacherDetailsThrows() {
        mockSecurityContext("test_student", "STUDENT");
        lenient().when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));

        assertThrows(SubjectAccessDeniedException.class, () ->
                subjectService.getSubjectDetails(subjectId)
        );
    }

    @Test
    @DisplayName("NEG05_getStudentSubjectDetails: Nauczyciel próbujący pobrać detale studenckie dostaje wyjątek")
    void NEG05_TeacherTryingToGetStudentDetailsThrows() {
        mockSecurityContext("test_teacher", "TEACHER");
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(subjectRepository.isStudentAssignedToSubject(subjectId, "test_teacher")).thenReturn(false);

        assertThrows(SubjectAccessDeniedException.class, () ->
                subjectService.getStudentSubjectDetails(subjectId)
        );
    }
}