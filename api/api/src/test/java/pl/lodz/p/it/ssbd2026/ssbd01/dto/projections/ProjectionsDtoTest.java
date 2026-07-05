package pl.lodz.p.it.ssbd2026.ssbd01.dto.projections;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class ProjectionsDtoTest {

    @Test
    void shouldCoverReportListProjectionDTO() {
        UUID id = UUID.randomUUID();
        LocalDateTime time = LocalDateTime.now();
        ReportListProjectionDTO dto1 = new ReportListProjectionDTO(id, "Tag", 50.0, "Sub", time, 10L);
        ReportListProjectionDTO dto2 = new ReportListProjectionDTO(id, "Tag", 50.0, "Sub", time, 10L);

        assertAll(
                () -> assertEquals(id, dto1.id()),
                () -> assertEquals("Tag", dto1.tag()),
                () -> assertEquals(50.0, dto1.averageSimilarity()),
                () -> assertEquals("Sub", dto1.subjectName()),
                () -> assertEquals(time, dto1.creationDate()),
                () -> assertEquals(10L, dto1.scannedRepositories()),
                () -> assertEquals(dto1, dto2),
                () -> assertEquals(dto1.hashCode(), dto2.hashCode()),
                () -> assertNotNull(dto1.toString())
        );
    }

    @Test
    void shouldCoverRepositorySimpleProjectionDTO() {
        UUID id = UUID.randomUUID();
        RepositorySimpleProjectionDTO dto1 = new RepositorySimpleProjectionDTO(id, "Repo", 100L);
        RepositorySimpleProjectionDTO dto2 = new RepositorySimpleProjectionDTO(id, "Repo", 100L);

        assertAll(
                () -> assertEquals(id, dto1.id()),
                () -> assertEquals("Repo", dto1.repositoryName()),
                () -> assertEquals(100L, dto1.ticketCount()),
                () -> assertEquals(dto1, dto2),
                () -> assertEquals(dto1.hashCode(), dto2.hashCode()),
                () -> assertNotNull(dto1.toString())
        );
    }

    @Test
    void shouldCoverRepoStudentProjectionDTO() {
        UUID rId = UUID.randomUUID();
        UUID sId = UUID.randomUUID();
        RepoStudentProjectionDTO dto1 = new RepoStudentProjectionDTO(rId, sId, "log", "Name", "Sur");
        RepoStudentProjectionDTO dto2 = new RepoStudentProjectionDTO(rId, sId, "log", "Name", "Sur");

        assertAll(
                () -> assertEquals(rId, dto1.repositoryId()),
                () -> assertEquals(sId, dto1.studentId()),
                () -> assertEquals("log", dto1.login()),
                () -> assertEquals("Name", dto1.name()),
                () -> assertEquals("Sur", dto1.surname()),
                () -> assertEquals(dto1, dto2),
                () -> assertEquals(dto1.hashCode(), dto2.hashCode()),
                () -> assertNotNull(dto1.toString())
        );
    }

    @Test
    void shouldCoverSubjectListProjectionDTO() {
        UUID id = UUID.randomUUID();
        SubjectListProjectionDTO dto1 = new SubjectListProjectionDTO(id, 1, "Sub", "2026", true);
        SubjectListProjectionDTO dto2 = new SubjectListProjectionDTO(id, 1, "Sub", "2026", true);

        assertAll(
                () -> assertEquals(id, dto1.id()),
                () -> assertEquals(1, dto1.version()),
                () -> assertEquals("Sub", dto1.name()),
                () -> assertEquals("2026", dto1.edition()),
                () -> assertTrue(dto1.archived()),
                () -> assertEquals(dto1, dto2),
                () -> assertEquals(dto1.hashCode(), dto2.hashCode()),
                () -> assertNotNull(dto1.toString())
        );
    }
}