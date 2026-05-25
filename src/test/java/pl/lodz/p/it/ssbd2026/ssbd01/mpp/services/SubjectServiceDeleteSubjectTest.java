package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.common.utils.PostCommitTransactionManager;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ConcurrentUpdateException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectAccessDeniedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubjectServiceDeleteSubjectTest {

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private VersionHashService versionHashService;

    @Mock
    private PostCommitTransactionManager postCommitTransactionManager;

    @InjectMocks
    private SubjectService subjectService;

    private UUID subjectId;
    private Subject subject;
    private SubjectAssignment assignment;
    private final String currentUsername = "jankowalski";
    private final String validHash = "validHash123";

    @BeforeEach
    void setUp() {
        subjectId = UUID.randomUUID();

        Account teacherAccount = new Account();
        teacherAccount.setLogin(currentUsername);

        Teacher teacher = new Teacher();
        teacher.setAccount(teacherAccount);

        assignment = new SubjectAssignment();
        assignment.setTeacher(teacher);
        assignment.setHasEditPermissions(true);

        subject = new Subject();
        // potrzebne do ustawienia pól prywatnych
        ReflectionTestUtils.setField(subject, "id", subjectId);
        ReflectionTestUtils.setField(subject, "version", 1);
        subject.setSubjectAssignmentList(List.of(assignment));

        Report report = new Report();
        report.setPathToReport("/tmp/report.pdf");
        subject.setReportList(List.of(report));
    }

    @Test
    @DisplayName("POS01: Powinien poprawnie usunąć przedmiot, jeśli dane i uprawnienia są prawidłowe")
    void POS01_deleteSubject_ShouldDeleteSuccessfully() {
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(versionHashService.hashObjectVersion(subjectId, subject.getVersion())).thenReturn(validHash);

        subjectService.deleteSubject(subjectId, validHash, currentUsername);

        verify(subjectRepository, times(1)).delete(subject);
        verify(subjectRepository, times(1)).flush();
        verify(postCommitTransactionManager, times(1)).register(any(Runnable.class));
    }

    @Test
    @DisplayName("NEG01: Powinien rzucić wyjątek SubjectNotFoundException, gdy przedmiot nie istnieje")
    void NEG01_deleteSubject_ShouldThrowWhenSubjectNotFound() {
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        assertThrows(SubjectNotFoundException.class, () ->
                subjectService.deleteSubject(subjectId, validHash, currentUsername)
        );

        verify(subjectRepository, never()).delete(any());
    }

    @Test
    @DisplayName("NEG02: Powinien rzucić wyjątek ConcurrentUpdateException przy błędzie blokady optymistycznej")
    void NEG02_deleteSubject_ShouldThrowOnConcurrentUpdate() {
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(versionHashService.hashObjectVersion(subjectId, subject.getVersion())).thenReturn("differentHash");

        assertThrows(ConcurrentUpdateException.class, () ->
                subjectService.deleteSubject(subjectId, validHash, currentUsername)
        );

        verify(subjectRepository, never()).delete(any());
    }

    @Test
    @DisplayName("NEG03: Powinien rzucić wyjątek SubjectAccessDeniedException, gdy użytkownik nie ma uprawnień")
    void NEG03_deleteSubject_ShouldThrowWhenAccessDenied() {
        assignment.setHasEditPermissions(false);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(versionHashService.hashObjectVersion(subjectId, subject.getVersion())).thenReturn(validHash);

        assertThrows(SubjectAccessDeniedException.class, () ->
                subjectService.deleteSubject(subjectId, validHash, currentUsername)
        );

        verify(subjectRepository, never()).delete(any());
    }
}