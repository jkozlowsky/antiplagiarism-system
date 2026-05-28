package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.RulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.SubjectDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RaportLevelDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.SubjectAssignment;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Teacher;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel.AccessLevelNotAssignedException;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.*;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.SubjectService;

import java.util.Optional;
import java.util.UUID;

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
    @Mock
    private RaportLevelRepository raportLevelRepository;
    @Mock
    private RuleRepository ruleRepository;



    @InjectMocks
    private SubjectService subjectService;

    private SubjectDTO validDto;
    private final String TEACHER_LOGIN = "szponcik";

    @BeforeEach
    void setUp() {
        RulePresetDTO rules = new RulePresetDTO(UUID.randomUUID(), "PEŁNY_WGLĄD", 1, 1, true);

        validDto = new SubjectDTO(
                null, null, "SSBD", "elo zelo", "2026", "64 osoby IAS",
                "https://gitea.example.com/ssbd", null, null, null, null, null,
                null, rules, true, true, true
        );
    }

    @Test
    void shouldSuccessfullyCreateSubjectAndAssignTeacher() {
        Teacher teacher = new Teacher();
        when(teacherRepository.findActiveTeacherByLogin(TEACHER_LOGIN)).thenReturn(Optional.of(teacher));

        when(raportLevelRepository.findAll()).thenReturn(java.util.List.of(new RaportLevelDict()));

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
    }
}