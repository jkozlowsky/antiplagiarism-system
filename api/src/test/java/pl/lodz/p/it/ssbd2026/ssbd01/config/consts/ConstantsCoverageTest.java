package pl.lodz.p.it.ssbd2026.ssbd01.config.consts;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConstantsCoverageTest {

    @Test
    void shouldCoverAccountConsts() {
        assertNotNull(AccountConsts.ROLE_ADMIN);
        assertDoesNotThrow(AccountConsts::new);
    }

    @Test
    void shouldCoverRepositoryContsts() {
        assertNotNull(RepositoryContsts.REPOSITORY_NAME_LENGTH);
        assertDoesNotThrow(RepositoryContsts::new);
    }

    @Test
    void shouldCoverSubjectConsts() {
        assertNotNull(SubjectConsts.SUBJECT_NAME_LENGTH);
        assertDoesNotThrow(SubjectConsts::new);
    }

    @Test
    void shouldCoverTagConsts() {
        assertNotNull(TagConsts.TAG_LENGTH);
        assertDoesNotThrow(TagConsts::new);
    }
}