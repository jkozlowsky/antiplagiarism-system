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
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.RuleMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.SubjectMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.SubjectDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.SubjectAssignment;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.TeacherSubjectRole;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectAccessDeniedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectAssignmentRepository;
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
    private VersionHashService versionHashService;

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

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.getName()).thenReturn(username);
        GrantedAuthority authority = new SimpleGrantedAuthority(role);
        doReturn(Collections.singletonList(authority)).when(authentication).getAuthorities();
    }

    @Test
    @DisplayName("POS01_getAllSubjects: Admin powinien otrzymać wszystkie przedmioty z pełnymi prawami")
    void POS01_AdminShouldGetAllSubjectsWithAllPermissions() {
        mockSecurityContext("test_admin", "ADMIN");
        when(subjectRepository.findAll()).thenReturn(List.of(subject));
        lenient().when(versionHashService.hashObjectVersion(any(), any())).thenReturn(validHash);

        SubjectDTO mockedDto = mock(SubjectDTO.class);
        when(mockedDto.canEdit()).thenReturn(true);
        when(mockedDto.canManageTeachers()).thenReturn(true);
        when(mockedDto.canViewStats()).thenReturn(true);

        when(subjectMapper.toSubjectDTO(subject, validHash, true, true, true, null, null)).thenReturn(mockedDto);

        List<SubjectDTO> result = subjectService.getAllSubjects();

        assertEquals(1, result.size());
        SubjectDTO dto = result.get(0);
        assertTrue(dto.canEdit());
        assertTrue(dto.canManageTeachers());
        assertTrue(dto.canViewStats());

        verify(subjectRepository, times(1)).findAll();
        verifyNoInteractions(subjectAssignmentRepository);
    }

    @Test
    @DisplayName("POS02_getAllSubjects: Prowadzący jako OWNER powinien mieć prawa do edycji i kadr")
    void POS02_TeacherOwnerShouldGetSubjectsWithEditAndManagePermissions() {
        mockSecurityContext("test_teacher", "TEACHER");

        SubjectAssignment assignment = new SubjectAssignment();
        assignment.setSubject(subject);
        assignment.setSubjectRole(TeacherSubjectRole.OWNER);

        when(subjectAssignmentRepository.findAllByTeacherLogin("test_teacher"))
                .thenReturn(List.of(assignment));
        lenient().when(versionHashService.hashObjectVersion(any(), any())).thenReturn(validHash);

        SubjectDTO mockedDto = mock(SubjectDTO.class);
        when(mockedDto.canEdit()).thenReturn(true);
        when(mockedDto.canManageTeachers()).thenReturn(true);
        when(mockedDto.canViewStats()).thenReturn(true);

        when(subjectMapper.toSubjectDTO(subject, validHash, true, true, true, null, null)).thenReturn(mockedDto);

        List<SubjectDTO> result = subjectService.getAllSubjects();

        assertEquals(1, result.size());
        SubjectDTO dto = result.get(0);
        assertTrue(dto.canEdit());
        assertTrue(dto.canManageTeachers());
        assertTrue(dto.canViewStats());
    }

    @Test
    @DisplayName("POS03_getAllSubjects: Student powinien otrzymać przedmioty, ale bez praw do edycji")
    void POS03_StudentShouldGetSubjectsWithNoPermissions() {
        mockSecurityContext("test_student", "STUDENT");
        when(subjectRepository.findAllByStudentLogin("test_student")).thenReturn(List.of(subject));
        lenient().when(versionHashService.hashObjectVersion(any(), any())).thenReturn(validHash);

        SubjectDTO mockedDto = mock(SubjectDTO.class);
        when(mockedDto.canEdit()).thenReturn(false);
        when(mockedDto.canManageTeachers()).thenReturn(false);
        when(mockedDto.canViewStats()).thenReturn(false);

        when(subjectMapper.toSubjectDTO(subject, validHash, false, false, false, null, null)).thenReturn(mockedDto);

        List<SubjectDTO> result = subjectService.getAllSubjects();

        assertEquals(1, result.size());
        SubjectDTO dto = result.get(0);
        assertFalse(dto.canEdit());
        assertFalse(dto.canManageTeachers());
        assertFalse(dto.canViewStats());
    }

    @Test
    @DisplayName("POS04_getSubjectDetails: Student poprawnie przypisany otrzymuje detale przedmiotu")
    void POS04_AssignedStudentShouldGetDetails() {
        mockSecurityContext("test_student", "STUDENT");
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(subjectRepository.isStudentAssignedToSubject(subjectId, "test_student")).thenReturn(true);
        when(versionHashService.hashObjectVersion(subjectId, subject.getVersion())).thenReturn(validHash);

        SubjectDTO mockedDto = mock(SubjectDTO.class);
        when(mockedDto.name()).thenReturn("Architektura Systemów Komputerowych");
        when(mockedDto.canEdit()).thenReturn(false);

        when(subjectMapper.toSubjectDTO(subject, validHash, false, false, false, null, null)).thenReturn(mockedDto);

        SubjectDTO result = subjectService.getSubjectDetails(subjectId);

        assertNotNull(result);
        assertEquals("Architektura Systemów Komputerowych", result.name());
        assertFalse(result.canEdit());
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
    @DisplayName("NEG02_getSubjectDetails: Student nieprzypisany do przedmiotu dostaje AccessDenied")
    void NEG02_UnassignedStudentShouldThrowAccessDenied() {
        mockSecurityContext("test_student", "STUDENT");
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(subjectRepository.isStudentAssignedToSubject(subjectId, "test_student")).thenReturn(false);

        assertThrows(SubjectAccessDeniedException.class, () ->
                subjectService.getSubjectDetails(subjectId)
        );
    }

    @Test
    @DisplayName("NEG03_getSubjectDetails: Prowadzący nieprzypisany do przedmiotu dostaje AccessDenied")
    void NEG03_UnassignedTeacherShouldThrowAccessDenied() {
        mockSecurityContext("test_teacher", "TEACHER");
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(subjectAssignmentRepository.findByTeacherLoginAndSubjectId("test_teacher", subjectId))
                .thenReturn(Collections.emptyList());

        assertThrows(SubjectAccessDeniedException.class, () ->
                subjectService.getSubjectDetails(subjectId)
        );
    }
}