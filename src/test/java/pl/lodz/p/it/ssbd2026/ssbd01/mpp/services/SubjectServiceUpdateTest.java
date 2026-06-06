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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.TeacherAssignmentDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.UpdateSubjectDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectAccessDeniedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.subject.SubjectMustHaveOwnerException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.LanguageDictRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectServiceUpdateTest {

    @Mock private SubjectRepository subjectRepository;
    @Mock private VersionHashService versionHashService;
    @Mock private TeacherRepository teacherRepository;
    @Mock private RaportLevelRepository raportLevelRepository;

    @Mock private SubjectAssignmentRepository subjectAssignmentRepository;
    @Mock private SubjectDescriptionTranslationRepository subjectDescriptionTranslationRepository;
    @Mock private LanguageDictRepositoryMPP languageDictRepository;

    @InjectMocks
    private SubjectService subjectService;

    private UUID subjectId;
    private Subject subject;
    private LanguageDict languageDict;
    private final String VALID_HASH = "valid_hash_123";
    private final String ORIGINAL_LANGUAGE = "pl";

    @BeforeEach
    void setUp() {
        subjectId = UUID.randomUUID();
        subject = new Subject();
        subject.setName("Old Name");
        org.springframework.test.util.ReflectionTestUtils.setField(subject, "id", subjectId);
        org.springframework.test.util.ReflectionTestUtils.setField(subject, "version", 1);

        languageDict = new LanguageDict();
        languageDict.setLanguage(ORIGINAL_LANGUAGE);


        Account account = new Account();
        account.setLogin("test_owner");
        Teacher teacher = new Teacher();
        teacher.setAccount(account);

        SubjectAssignment assignment = new SubjectAssignment();
        assignment.setTeacher(teacher);
        assignment.setSubjectRole(TeacherSubjectRole.OWNER);
        subject.setSubjectAssignmentList(new ArrayList<>(List.of(assignment)));
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
        doReturn(Collections.singletonList(new SimpleGrantedAuthority(role))).when(authentication).getAuthorities();
    }

    @Test
    @DisplayName("POS01: Administrator powinien pomyślnie zaktualizować przedmiot")
    void POS01_AdminShouldUpdateSubjectSuccessfully() {
        mockSecurityContext("admin_user", AccountConsts.ROLE_ADMIN);
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(versionHashService.hashObjectVersion(subjectId, 1)).thenReturn(VALID_HASH);
        when(languageDictRepository.findByLanguage(ORIGINAL_LANGUAGE)).thenReturn(Optional.ofNullable(languageDict));


        UpdateSubjectDTO dto = new UpdateSubjectDTO("New Name", "Desc",
                List.of(new TeacherAssignmentDTO("test_owner", TeacherSubjectRole.OWNER)), null);

        assertDoesNotThrow(() -> subjectService.updateSubject(subjectId, dto, VALID_HASH, ORIGINAL_LANGUAGE));
        assertEquals("New Name", subject.getName());
        verify(subjectRepository, times(1)).saveAndFlush(subject);
    }

    @Test
    @DisplayName("NEG01: Rzuca ConcurrentUpdateException gdy hash (ETag) się nie zgadza")
    void NEG01_ShouldThrowConcurrentUpdateExceptionOnHashMismatch() {
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(versionHashService.hashObjectVersion(subjectId, 1)).thenReturn(VALID_HASH);

        UpdateSubjectDTO dto = new UpdateSubjectDTO("Name", "Desc",  List.of(), null);

        assertThrows(ConcurrentUpdateException.class, () ->
                subjectService.updateSubject(subjectId, dto, "WRONG_HASH", ORIGINAL_LANGUAGE));
    }

    @Test
    @DisplayName("NEG02: Rzuca SubjectAccessDenied dla studenta próbującego edytować")
    void NEG02_ShouldThrowAccessDeniedForStudent() {
        mockSecurityContext("student_1", AccountConsts.ROLE_STUDENT);
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(versionHashService.hashObjectVersion(subjectId, 1)).thenReturn(VALID_HASH);

        UpdateSubjectDTO dto = new UpdateSubjectDTO("Name", "Desc", List.of(), null);

        assertThrows(SubjectAccessDeniedException.class, () ->
                subjectService.updateSubject(subjectId, dto, VALID_HASH, ORIGINAL_LANGUAGE));
    }

    @Test
    @DisplayName("NEG03: Rzuca SubjectMustHaveOwnerException gdy próbuje się usunąć wszystkich ownerów")
    void NEG03_ShouldThrowWhenRemovingAllOwners() {
        mockSecurityContext("test_owner", AccountConsts.ROLE_TEACHER);
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(versionHashService.hashObjectVersion(subjectId, 1)).thenReturn(VALID_HASH);
        when(languageDictRepository.findByLanguage(ORIGINAL_LANGUAGE)).thenReturn(Optional.ofNullable(languageDict));

        UpdateSubjectDTO dto = new UpdateSubjectDTO("Name", "Desc",
                List.of(new TeacherAssignmentDTO("test_owner", TeacherSubjectRole.EDITOR)), null);

        assertThrows(SubjectMustHaveOwnerException.class, () ->
                subjectService.updateSubject(subjectId, dto, VALID_HASH, ORIGINAL_LANGUAGE));
    }
}