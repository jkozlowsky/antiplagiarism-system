package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.SubjectDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.SubjectAssignment;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Teacher;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel.AccessLevelNotAssignedException;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectAssignmentRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.TeacherRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.SubjectService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private SubjectAssignmentRepository subjectAssignmentRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @InjectMocks
    private SubjectService subjectService;

    private SubjectDTO validDto;
    private final String TEACHER_LOGIN = "szponcik";

    @BeforeEach
    void setUp() {
        validDto = new SubjectDTO(
                null, null, "SSBD","elo zelo" ,"2026", "64 osoby IAS",
                "https://gitea.example.com/ssbd", null, null, null, null, null
        );
    }

    @Test
    void shouldSuccessfullyCreateSubjectAndAssignTeacher() {
        Teacher teacher = new Teacher();
        when(teacherRepository.findActiveTeacherByLogin(TEACHER_LOGIN)).thenReturn(Optional.of(teacher));

        subjectService.createSubject(validDto, TEACHER_LOGIN);

        assertAll(
                () -> verify(subjectRepository, times(1)).saveAndFlush(any(Subject.class)),
                () -> verify(subjectAssignmentRepository, times(1)).saveAndFlush(any(SubjectAssignment.class))
        );
    }

    @Test
    void shouldThrowExceptionWhenTeacherNotFound() {
        String notTeacherLogin = "skibidi";
        when(teacherRepository.findActiveTeacherByLogin(notTeacherLogin)).thenReturn(Optional.empty());

        assertThrows(AccessLevelNotAssignedException.class, () ->
                subjectService.createSubject(validDto, notTeacherLogin)
        );

        assertAll(
                () -> verify(subjectRepository, never()).saveAndFlush(any(Subject.class)),
                () -> verify(subjectAssignmentRepository, never()).saveAndFlush(any(SubjectAssignment.class))
        );
    }
}