package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.TeacherSearchDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Teacher;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.TeacherRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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
        Teacher activeTeacher = new Teacher();
        activeTeacher.setActive(true);
        Account account = new Account();
        account.setLogin("j_szewczyk");
        account.setName("Janusz");
        account.setSurname("Szewczyk");
        account.setEmail("j.szewczyk@p.lodz.pl");
        activeTeacher.setAccount(account);

        Teacher inactiveTeacher = new Teacher();
        inactiveTeacher.setActive(false);

        when(teacherRepository.findByAccountLoginContainingIgnoreCaseOrAccountEmailContainingIgnoreCase(query, query))
                .thenReturn(List.of(activeTeacher, inactiveTeacher));

        List<TeacherSearchDTO> result = teacherService.searchTeachers(query);

        assertEquals(1, result.size());
        assertEquals("j_szewczyk", result.get(0).login());
        assertEquals("Janusz", result.get(0).firstName());
        assertEquals("Szewczyk", result.get(0).lastName());
        assertEquals("j.szewczyk@p.lodz.pl", result.get(0).email());
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