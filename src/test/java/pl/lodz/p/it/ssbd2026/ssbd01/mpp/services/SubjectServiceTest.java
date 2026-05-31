package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.CreateSubjectManualDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.RulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RaportLevelDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Teacher;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel.AccessLevelNotAssignedException;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.*;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
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

        RaportLevelDict mockDict = new RaportLevelDict();
        mockDict.setName("PEŁNY_WGLĄD");
        when(raportLevelRepository.findByName(anyString())).thenReturn(Optional.of(mockDict));

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
}