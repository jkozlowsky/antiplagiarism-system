package pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class MppDtoTest {

    @Test
    void shouldCoverCreateSubjectTemplateDTO() {
        UUID tempId = UUID.randomUUID();
        CreateSubjectTemplateDTO dto1 = new CreateSubjectTemplateDTO("Name", "Org", "2026", "Desc", tempId, "Level", List.of());
        CreateSubjectTemplateDTO dto2 = new CreateSubjectTemplateDTO("Name", "Org", "2026", "Desc", tempId, "Level", List.of());

        assertAll(
                () -> assertEquals("Name", dto1.name()),
                () -> assertEquals("Org", dto1.organizationName()),
                () -> assertEquals("2026", dto1.edition()),
                () -> assertEquals("Desc", dto1.subjectDescription()),
                () -> assertEquals(tempId, dto1.templateId()),
                () -> assertEquals("Level", dto1.raportLevelName()),
                () -> assertTrue(dto1.teachers().isEmpty()),
                () -> assertEquals(dto1, dto2),
                () -> assertEquals(dto1.hashCode(), dto2.hashCode()),
                () -> assertNotNull(dto1.toString())
        );
    }

    @Test
    void shouldCoverStudentRepositoryDTO() {
        UUID repoId = UUID.randomUUID();
        StudentRepositoryDTO dto1 = new StudentRepositoryDTO(repoId, "Repo", "Subj", 1, 5);
        StudentRepositoryDTO dto2 = new StudentRepositoryDTO(repoId, "Repo", "Subj", 1, 5);

        assertAll(
                () -> assertEquals(repoId, dto1.repositoryId()),
                () -> assertEquals("Repo", dto1.repositoryName()),
                () -> assertEquals("Subj", dto1.subjectName()),
                () -> assertEquals(1, dto1.usedScans()),
                () -> assertEquals(5, dto1.maxScans()),
                () -> assertEquals(dto1, dto2),
                () -> assertEquals(dto1.hashCode(), dto2.hashCode()),
                () -> assertNotNull(dto1.toString())
        );
    }

    @Test
    void shouldCoverTeacherMatchedFileDTO() {
        TeacherMatchedFileDTO dto1 = new TeacherMatchedFileDTO("F1", "F2", "C1", "C2", List.of(1), List.of(2), 10);
        TeacherMatchedFileDTO dto2 = new TeacherMatchedFileDTO("F1", "F2", "C1", "C2", List.of(1), List.of(2), 10);

        assertAll(
                () -> assertEquals("F1", dto1.fileA()),
                () -> assertEquals("F2", dto1.fileB()),
                () -> assertEquals("C1", dto1.codeA()),
                () -> assertEquals("C2", dto1.codeB()),
                () -> assertEquals(1, dto1.matchedLinesA().getFirst()),
                () -> assertEquals(2, dto1.matchedLinesB().getFirst()),
                () -> assertEquals(10, dto1.tokens()),
                () -> assertEquals(dto1, dto2),
                () -> assertEquals(dto1.hashCode(), dto2.hashCode()),
                () -> assertNotNull(dto1.toString())
        );
    }
}