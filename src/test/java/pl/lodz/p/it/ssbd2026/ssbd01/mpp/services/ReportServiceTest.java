package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.FilteredComparisonDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.StudentReportDetailsDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectAccessDeniedException;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.mpp21.MPP21_ViewOwnReportsTest;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.ReportsRepository;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @Mock
    private ReportsRepository reportsRepository;

    @InjectMocks
    private ReportService reportService;

    private Report testReport;
    private File testZipFile;
    private final String STUDENT_LOGIN = "jkowalski";

    @BeforeEach
    void setUp() throws Exception {
        Account account = new Account();
        account.setLogin(STUDENT_LOGIN);

        Student student = new Student();
        student.setAccount(account);

        Repository repo = new Repository();
        repo.setRepositoryName("Repo_jkowalski");
        repo.setStudentList(List.of(student));

        RaportLevelDict levelDict = new RaportLevelDict();
        levelDict.setName("FULL_VIEW");

        RulePreset preset = new RulePreset();
        preset.setRaportLevel(levelDict);

        Subject subject = new Subject();
        subject.setName("Programowanie Java");
        subject.setRulePreset(preset);

        testReport = new Report();
        ReflectionTestUtils.setField(testReport, "id", UUID.randomUUID());
        testReport.setSubject(subject);
        testReport.setRepositoryList(List.of(repo));

        testZipFile = MPP21_ViewOwnReportsTest.createDummyJplagReport("Repo_jkowalski", "Repo_Innego_Studenta");
        testReport.setPathToReport(testZipFile.getAbsolutePath());
    }

    @Test
    @DisplayName("Pobranie pełnych detali (FULL_VIEW) i anonimizacja drugiego studenta")
    void getStudentReportDetails_FullView_Success() {
        when(reportsRepository.findById(testReport.getId())).thenReturn(Optional.of(testReport));

        StudentReportDetailsDTO result = reportService.getStudentReportDetails(testReport.getId(), STUDENT_LOGIN);

        assertNotNull(result);
        assertEquals(1, result.matches().size());

        FilteredComparisonDTO match = result.matches().get(0);
        assertEquals("Repozytorium Innego Studenta", match.matchedWith());
        assertEquals(85.5, match.maxSimilarity());
        assertEquals(40.2, match.averageSimilarity());
        assertEquals(120.0, match.longestMatch());
    }

    @Test
    @DisplayName("Filtrowanie wyników pod ONLY_HIGHEST_PERCENT")
    void getStudentReportDetails_OnlyHighestPercent() {
        testReport.getSubject().getRulePreset().getRaportLevel().setName("ONLY_HIGHEST_PERCENT");
        when(reportsRepository.findById(testReport.getId())).thenReturn(Optional.of(testReport));

        StudentReportDetailsDTO result = reportService.getStudentReportDetails(testReport.getId(), STUDENT_LOGIN);

        assertEquals(1, result.matches().size());
        FilteredComparisonDTO match = result.matches().get(0);

        assertNotNull(match.maxSimilarity());
        assertNull(match.averageSimilarity());
        assertNull(match.longestMatch());
    }

    @Test
    @DisplayName("Rzucenie wyjątku w przypadku braku dostępu do repozytorium w tym raporcie")
    void getStudentReportDetails_ThrowsAccessDenied_WhenNoRepositoryMatch() {
        when(reportsRepository.findById(testReport.getId())).thenReturn(Optional.of(testReport));

        assertThrows(SubjectAccessDeniedException.class, () -> {
            reportService.getStudentReportDetails(testReport.getId(), "obcy_student");
        });
    }

    @Test
    @DisplayName("Rzucenie wyjątku gdy poziom to NOTHING")
    void getStudentReportDetails_ThrowsAccessDenied_WhenLevelIsNothing() {
        testReport.getSubject().getRulePreset().getRaportLevel().setName("NOTHING");
        when(reportsRepository.findById(testReport.getId())).thenReturn(Optional.of(testReport));

        assertThrows(SubjectAccessDeniedException.class, () -> {
            reportService.getStudentReportDetails(testReport.getId(), STUDENT_LOGIN);
        });
    }
}