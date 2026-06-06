package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import de.jplag.JPlagResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.GiteaRepoDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.StudentReportDetailsDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectAccessDeniedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ownReport.NoTicketsLeftException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ownReport.RepositoryNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.GiteaRepositoryRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.ReportsRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.utils.GitService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.utils.JPlagService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.utils.JplagReportParser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GenerateStudentReportServiceTest {

    @Mock
    private GiteaRepositoryRepository giteaRepositoryRepository;
    @Mock
    private ReportsRepository reportsRepository;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private GiteaService giteaService;
    @Mock
    private JPlagService jPlagService;

    @InjectMocks
    private ReportService reportService;

    private UUID repositoryId;
    private Repository repository;
    private Subject subject;
    private RulePreset rulePreset;
    private final String studentLogin = "student64";

    @BeforeEach
    void setUp() {
        repositoryId = UUID.randomUUID();

        Account account = new Account();
        account.setLogin(studentLogin);
        Student student = new Student();
        student.setAccount(account);

        ReportLevelDict levelDict = new ReportLevelDict();
        levelDict.setName("FULL_VIEW");

        rulePreset = new RulePreset();
        rulePreset.setRaportLevel(levelDict);
        rulePreset.setStudentTicketCount(3);
        rulePreset.setMinimumTokensMatch(12);
        rulePreset.setEnableNormalization(true);

        subject = new Subject();
        subject.setOrganizationName("IAS64USECASE55");
        subject.setName("IAS64");
        subject.setRulePreset(rulePreset);
        ReflectionTestUtils.setField(subject, "id", UUID.randomUUID());

        repository = new Repository();
        ReflectionTestUtils.setField(repository, "id", repositoryId);
        repository.setRepositoryName("repo64");
        repository.setSubject(subject);
        repository.setStudentList(List.of(student));
    }

    @Test
    @DisplayName("Verify, if generating a student report works.")
    void POS01_GenerateStudentReport() {
        String studentTag = "64";
        String taskTag = "task64";

        GiteaRepoDTO validRepo = new GiteaRepoDTO("test-repo", "url1", false);

        when(giteaRepositoryRepository.findByIdWithLock(repositoryId)).thenReturn(Optional.of(repository));

        when(reportsRepository.countReportsByIdAndCreatedByLogin(repositoryId, studentLogin)).thenReturn(0);

        when(giteaService.getRepositoriesFromOrganization("IAS64USECASE55")).thenReturn(List.of(validRepo));
        when(giteaRepositoryRepository.getAllBySubjectId(subject.getId())).thenReturn(List.of(repository));

        JPlagResult resultJP = mock(JPlagResult.class);
        when(resultJP.getAllComparisons()).thenReturn(List.of());
        when(jPlagService.runPlagiarismScanOnOrganisation(anyString(), anyString(), anyInt(), anyBoolean(), anyString()))
                .thenReturn(resultJP);

        when(reportsRepository.saveAndFlush(any(Report.class))).thenAnswer(inv -> {
            Report report = inv.getArgument(0);
            ReflectionTestUtils.setField(report, "id", UUID.randomUUID());
            when(reportsRepository.findById(report.getId())).thenReturn(Optional.of(report));
            return report;
        });

        try (MockedStatic<GitService> gitService = mockStatic(GitService.class);
             MockedStatic<JplagReportParser> mockedParser = mockStatic(JplagReportParser.class)) {

            mockedParser.when(() -> JplagReportParser.parseFullTeacherReport(anyString())).thenReturn(List.of());

            StudentReportDetailsDTO result = reportService.generateStudentReport(repositoryId, studentTag, taskTag, studentLogin);

            assertNotNull(result);

            verify(reportsRepository).saveAndFlush(any(Report.class));
        }
    }

    @Test
    @DisplayName("Verify, if student cannot check code when not tickets are remaining.")
    void NEG01_GenerateStudentReport() {
        when(giteaRepositoryRepository.findByIdWithLock(repositoryId)).thenReturn(Optional.of(repository));

        when(reportsRepository.countReportsByIdAndCreatedByLogin(repositoryId, studentLogin)).thenReturn(3);

        assertThrows(NoTicketsLeftException.class, () -> reportService.generateStudentReport(repositoryId, "64", "64", studentLogin));
    }

    @Test
    @DisplayName("Verify, if report level HIDDEN result in no access to the system.")
    void NEG02_GenerateStudentReport() {
        rulePreset.getRaportLevel().setName("HIDDEN");

        when(giteaRepositoryRepository.findByIdWithLock(repositoryId)).thenReturn(Optional.of(repository));

        assertThrows(SubjectAccessDeniedException.class, () -> reportService.generateStudentReport(repositoryId, "64", "64", studentLogin));
    }

    @Test
    @DisplayName("Verify, if checking code from repository which user does not own is blocked properly.")
    void NEG03_GenerateStudentReport() {
        when(giteaRepositoryRepository.findByIdWithLock(repositoryId)).thenReturn(Optional.of(repository));

        assertThrows(SubjectAccessDeniedException.class, () -> reportService.generateStudentReport(repositoryId, "64", "64", "ias67"));
    }

    @Test
    @DisplayName("Verify how system behaves when repository is not found.")
    void NEG04_GenerateStudentReport() {
        when(giteaRepositoryRepository.findByIdWithLock(repositoryId)).thenReturn(Optional.empty());

        assertThrows(RepositoryNotFoundException.class, () -> reportService.generateStudentReport(repositoryId, "64", "64", studentLogin));
    }
}