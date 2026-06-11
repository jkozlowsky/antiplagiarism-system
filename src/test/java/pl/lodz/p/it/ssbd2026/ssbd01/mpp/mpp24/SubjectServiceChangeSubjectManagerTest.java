package pl.lodz.p.it.ssbd2026.ssbd01.mpp.mpp24;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel.AccessLevelNotAssignedException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectAssignmentRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.TeacherRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.SubjectService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubjectServiceChangeSubjectManagerTest {
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private SubjectAssignmentRepository subjectAssignmentRepository;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private VersionHashService versionHashService;

    @InjectMocks
    private SubjectService subjectService;

    private UUID subjectId;
    private UUID currentOwnerId;
    private UUID newManagerId;

    private Subject subject;
    private Teacher currentManagerTeacher;
    private Teacher newManagerTeacher;
    private SubjectAssignment currentOwnerAssignment;
    private SubjectAssignment newOwnerAssignment;

    @BeforeEach
    void setUp() {
        subjectId = UUID.randomUUID();
        currentOwnerId = UUID.randomUUID();
        newManagerId = UUID.randomUUID();

        subject = new Subject();
        ReflectionTestUtils.setField(subject, "id", subjectId);
        subject.setArchived(false);

        currentManagerTeacher = createTestTeacher(currentOwnerId, "current_manager");
        newManagerTeacher = createTestTeacher(newManagerId, "new_manager");

        currentOwnerAssignment = createTestSubjectAssignment(UUID.randomUUID(), currentManagerTeacher, subject, TeacherSubjectRole.OWNER);
        newOwnerAssignment = createTestSubjectAssignment(UUID.randomUUID(), newManagerTeacher, subject, TeacherSubjectRole.EDITOR);
    }

    private Teacher createTestTeacher(UUID id, String login) {
        Teacher teacher = new Teacher();
        Account account = new Account();
        account.setLogin(login);
        ReflectionTestUtils.setField(teacher, "id", id);
        teacher.setAccount(account);
        return teacher;
    }

    private SubjectAssignment createTestSubjectAssignment(UUID id, Teacher teacher, Subject subject, TeacherSubjectRole role) {
        SubjectAssignment assignment = new SubjectAssignment();
        ReflectionTestUtils.setField(assignment, "id", id);
        assignment.setTeacher(teacher);
        assignment.setSubject(subject);
        assignment.setSubjectRole(role);
        teacher.getSubjectAssignmentList().add(assignment);
        return assignment;
    }

    @Test
    public void POS01_ChangeSubjectManager_Successfully() {
        when(subjectRepository.findByIdForceIncrement(subjectId)).thenReturn(Optional.of(subject));
        when(versionHashService.hashObjectVersion(subjectId, subject.getVersion())).thenReturn("hash123");

        when(teacherRepository.findSubjectOwnerBySubjectId(subjectId)).thenReturn(Optional.of(currentManagerTeacher));
        when(subjectAssignmentRepository.findByTeacherLoginAndSubjectId("current_manager", subjectId))
                .thenReturn(Optional.of(currentOwnerAssignment));

        when(teacherRepository.findActiveTeacherByLogin("new_manager")).thenReturn(Optional.of(newManagerTeacher));
        when(subjectAssignmentRepository.findByTeacherLoginAndSubjectId("new_manager", subjectId))
                .thenReturn(Optional.of(newOwnerAssignment));

        subjectService.changeSubjectManager(subjectId, "new_manager", "hash123");
        verify(subjectAssignmentRepository, times(2)).saveAndFlush(any());
        assertAll(
                () -> assertEquals(TeacherSubjectRole.OWNER, newOwnerAssignment.getSubjectRole()),
                () -> assertEquals(TeacherSubjectRole.VIEWER, currentOwnerAssignment.getSubjectRole())
        );
    }

    @Test
    public void POS02_ChangeSubjectManager_WhenNewManagerIsNotAssignedToSubject() {
        String newManagerLoginNotAssigned = "new_manager_not_assigned";
        Account newManagerAccountNotAssigned = new Account();
        newManagerAccountNotAssigned.setLogin(newManagerLoginNotAssigned);
        Teacher newManagerTeacherNotAssigned = new Teacher();
        UUID newManagerIdNotAssigned = UUID.randomUUID();
        newManagerTeacherNotAssigned.setAccount(newManagerAccountNotAssigned);
        ReflectionTestUtils.setField(newManagerTeacherNotAssigned, "id", newManagerIdNotAssigned);

        when(subjectRepository.findByIdForceIncrement(subjectId)).thenReturn(Optional.of(subject));

        when(versionHashService.hashObjectVersion(subjectId, subject.getVersion())).thenReturn("hash123");

        when(teacherRepository.findSubjectOwnerBySubjectId(subjectId)).thenReturn(Optional.of(currentManagerTeacher));
        when(subjectAssignmentRepository.findByTeacherLoginAndSubjectId("current_manager", subjectId))
                .thenReturn(Optional.of(currentOwnerAssignment));

        when(teacherRepository.findActiveTeacherByLogin(newManagerLoginNotAssigned)).thenReturn(Optional.of(newManagerTeacherNotAssigned));
        when(subjectAssignmentRepository.findByTeacherLoginAndSubjectId(newManagerLoginNotAssigned, subjectId))
                .thenReturn(Optional.empty());

        subjectService.changeSubjectManager(subjectId, newManagerLoginNotAssigned, "hash123");
        assertAll(
                () -> assertEquals(TeacherSubjectRole.VIEWER, currentOwnerAssignment.getSubjectRole()),
                () -> verify(subjectAssignmentRepository, times(2)).saveAndFlush(any())
        );
    }

    @Test
    public void NEG01_ChangeSubjectManager_WhenNewManagerNotFound() {
        when(subjectRepository.findByIdForceIncrement(subjectId)).thenReturn(Optional.of(subject));
        when(versionHashService.hashObjectVersion(subjectId, subject.getVersion())).thenReturn("hash123");

        when(teacherRepository.findSubjectOwnerBySubjectId(subjectId)).thenReturn(Optional.of(currentManagerTeacher));
        when(subjectAssignmentRepository.findByTeacherLoginAndSubjectId("current_manager", subjectId))
                .thenReturn(Optional.of(currentOwnerAssignment));

        when(teacherRepository.findActiveTeacherByLogin("new_manager")).thenReturn(Optional.empty());

        assertAll(
                () -> assertThrows(AccessLevelNotAssignedException.class, () -> subjectService.changeSubjectManager(subjectId, "new_manager", "hash123")),
                () -> assertEquals(TeacherSubjectRole.EDITOR, newOwnerAssignment.getSubjectRole()),
                () -> assertEquals(TeacherSubjectRole.OWNER, currentOwnerAssignment.getSubjectRole()),
                () -> verify(subjectAssignmentRepository, never()).saveAndFlush(any())
        );
    }

    @Test
    public void NEG02_ChangeSubjectManager_WhenSubjectNotFound() {
        when(subjectRepository.findByIdForceIncrement(subjectId)).thenReturn(Optional.empty());

        assertAll(
                () -> assertThrows(SubjectNotFoundException.class, () -> subjectService.changeSubjectManager(subjectId, "new_manager", "hash123")),
                () -> assertEquals(TeacherSubjectRole.EDITOR, newOwnerAssignment.getSubjectRole()),
                () -> assertEquals(TeacherSubjectRole.OWNER, currentOwnerAssignment.getSubjectRole()),
                () -> verify(subjectAssignmentRepository, never()).saveAndFlush(any())
        );
    }
}