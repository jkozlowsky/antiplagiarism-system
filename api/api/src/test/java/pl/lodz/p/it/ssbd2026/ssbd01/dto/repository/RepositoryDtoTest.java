package pl.lodz.p.it.ssbd2026.ssbd01.dto.repository;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class RepositoryDtoTest {

    @Test
    void shouldCoverStudentDTO() {
        StudentDTO dto1 = new StudentDTO("Name", "Surname");
        StudentDTO dto2 = new StudentDTO("Name", "Surname");

        assertAll(
                () -> assertEquals("Name", dto1.name()),
                () -> assertEquals("Surname", dto1.surname()),
                () -> assertEquals(dto1, dto2),
                () -> assertEquals(dto1.hashCode(), dto2.hashCode()),
                () -> assertNotNull(dto1.toString())
        );
    }

    @Test
    void shouldCoverRepositoryWithStudentDTO() {
        StudentDTO sDto = new StudentDTO("N", "S");
        RepositoryWithStudentDTO dto1 = new RepositoryWithStudentDTO("Repo", 5, List.of(sDto));
        RepositoryWithStudentDTO dto2 = new RepositoryWithStudentDTO("Repo", 5, List.of(sDto));

        assertAll(
                () -> assertEquals("Repo", dto1.repositoryName()),
                () -> assertEquals(5, dto1.ticketCount()),
                () -> assertEquals(1, dto1.students().size()),
                () -> assertEquals(dto1, dto2),
                () -> assertEquals(dto1.hashCode(), dto2.hashCode()),
                () -> assertNotNull(dto1.toString())
        );
    }
}