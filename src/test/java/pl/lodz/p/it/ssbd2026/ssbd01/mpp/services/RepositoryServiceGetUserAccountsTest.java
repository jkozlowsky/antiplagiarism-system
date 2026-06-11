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
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Report;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Repository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Student;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.GiteaRepositoryRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.ReportsRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.CommonFunctions;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RepositoryServiceGetUserAccountsTest {

    @Mock
    private GiteaRepositoryRepository giteaRepositoryRepository;

    @Mock
    private ReportsRepository reportsRepository;

    @InjectMocks
    private RepositoryService repositoryService;

    private String wholeSubjectName;
    private Repository repo;
    private Student student;

    private final CommonFunctions cf = new CommonFunctions();
    private final String name = cf.generateRandomStringOfLength(10);
    private final String surname = cf.generateRandomStringOfLength(10);
    private final String repoName = cf.generateRandomStringOfLength(10);

    @BeforeEach
    void setUp() {
        wholeSubjectName = "SysOpMax`2024";

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
        when(giteaRepositoryRepository.findBySubject_Name(wholeSubjectName)).thenReturn(List.of(repo));

        Report mockReport = new Report();
        mockReport.setAverageSimilarity(45.5);
        when(reportsRepository.findByInitiatingRepositoryIdOrderByCreatedAtDesc(repo.getId()))
                .thenReturn(Optional.of(mockReport));

        List<SubjectStudentStatsDTO> result = repositoryService.getUserAccounts(wholeSubjectName);

        assertNotNull(result);
        assertEquals(1, result.size());

        SubjectStudentStatsDTO dto = result.getFirst();
        assertEquals(student.getId(), dto.getStudentId());
        assertEquals(name, dto.getFirstName());
        assertEquals(surname, dto.getLastName());
        assertEquals(repo.getId(), dto.getRepositoryId());
        assertEquals(repoName, dto.getRepositoryName());

        assertNotNull(dto.getPlagiarismRatio());
        assertEquals(45.5, dto.getPlagiarismRatio());
    }

    @Test
    @DisplayName("Verify if, empty list of students in subject is handled properly")
    void NEG01_getUserAccounts() {
        when(giteaRepositoryRepository.findBySubject_Name(wholeSubjectName)).thenReturn(Collections.emptyList());

        List<SubjectStudentStatsDTO> result = repositoryService.getUserAccounts(wholeSubjectName);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}