package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.TeacherSearchDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.projections.TeacherSearchProjectionDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.TeacherRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @InjectMocks
    private TeacherService teacherService;

    @Test
    @DisplayName("POS01: Powinien zwrócić zmapowaną listę dla poprawnego zapytania")
    void POS01_ShouldReturnMappedTeachersForValidQuery() {
        String query = "janusz";
        UUID teacherId = UUID.randomUUID();
        TeacherSearchProjectionDTO projectedTeacher = new TeacherSearchProjectionDTO(
                teacherId,
                "j_szewczyk",
                "j.szewczyk@p.lodz.pl",
                "Janusz",
                "Szewczyk"

        );

        when(teacherRepository.findProjectedActiveTeachersByQuery(query))
                .thenReturn(List.of(projectedTeacher));

        List<TeacherSearchDTO> result = teacherService.searchTeachers(query);

        assertEquals(1, result.size());
        assertEquals("j_szewczyk", result.get(0).login());
        assertEquals("Janusz", result.get(0).firstName());
        assertEquals("Szewczyk", result.get(0).lastName());
        assertEquals("j.szewczyk@p.lodz.pl", result.get(0).email());

        verify(teacherRepository, times(1)).findProjectedActiveTeachersByQuery(query);
    }

    @Test
    @DisplayName("NEG01: Powinien zwrócić pustą listę, gdy zapytanie jest krótsze niż 2 znaki")
    void NEG01_ShouldReturnEmptyListForShortQuery() {
        List<TeacherSearchDTO> result1 = teacherService.searchTeachers("a");
        List<TeacherSearchDTO> result2 = teacherService.searchTeachers(null);
        List<TeacherSearchDTO> result3 = teacherService.searchTeachers("   ");

        assertTrue(result1.isEmpty());
        assertTrue(result2.isEmpty());
        assertTrue(result3.isEmpty());
        verifyNoInteractions(teacherRepository);
    }
}