package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.subject.SubjectStudentStatsDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Repository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Student;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.GiteaRepositoryRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.CommonFunctions;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RepositoryServiceGetUserAccountsTest {

    @Mock
    private GiteaRepositoryRepository giteaRepositoryRepository;

    @InjectMocks
    private RepositoryService repositoryService;

    private UUID subjectId;
    private Repository repo;
    private Student student;

    private final CommonFunctions cf = new CommonFunctions();
    private final String name = cf.generateRandomStringOfLength(10);
    private final String surname = cf.generateRandomStringOfLength(10);
    private final String repoName = cf.generateRandomStringOfLength(10);

    @BeforeEach
    void setUp() {
        subjectId = UUID.randomUUID();

        Account account = new Account();
        account.setName(name);
        account.setSurname(surname);

        student = new Student();
        student.setAccount(account);

        repo = new Repository();
        repo.setRepositoryName(repoName);
        repo.setStudentList(List.of(student));
    }

    @Test
    @DisplayName("Verify if, valid DTO was returned while trying to get user accounts")
    void POS01_getUserAccounts() {
        when(giteaRepositoryRepository.findBySubjectId(subjectId)).thenReturn(List.of(repo));

        List<SubjectStudentStatsDTO> result = repositoryService.getUserAccounts(subjectId);

        assertNotNull(result);
        assertEquals(1, result.size());

        SubjectStudentStatsDTO dto = result.getFirst();
        assertEquals(student.getId(), dto.getStudentId());
        assertEquals(name, dto.getFirstName());
        assertEquals(surname, dto.getLastName());
        assertEquals(repo.getId(), dto.getRepositoryId());
        assertEquals(repoName, dto.getRepositoryName());

        assertNull(dto.getPlagiarismRatio()); // TODO: jak będzie agregacja danych to zaaktualizować
    }

    @Test
    @DisplayName("Verify if, empty list of students in subject is handled properly")
    void NEG01_getUserAccounts() {
        when(giteaRepositoryRepository.findBySubjectId(subjectId)).thenReturn(Collections.emptyList());

        List<SubjectStudentStatsDTO> result = repositoryService.getUserAccounts(subjectId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
