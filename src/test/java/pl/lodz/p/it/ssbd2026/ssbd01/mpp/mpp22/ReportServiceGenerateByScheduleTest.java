package pl.lodz.p.it.ssbd2026.ssbd01.mpp.mpp22;

import de.jplag.JPlagResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.GiteaRepoDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Report;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Repository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Schedule;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.GiteaRepositoryRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.ReportsRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.ScheduleRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.GiteaService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.ReportService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.utils.GitService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.utils.JPlagService;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceGenerateByScheduleTest {
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private GiteaService giteaService;
    @Mock
    private JPlagService jPlagService;
    @Mock
    private GiteaRepositoryRepository giteaRepositoryRepository;
    @Mock
    private ReportsRepository reportsRepository;
    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private ReportService reportService;

    @Captor
    private ArgumentCaptor<Report> reportCaptor;

    private Subject subject;
    private RulePreset rulePreset;
    private Schedule schedule;
    private final String ORG_NAME = "TestOrg";
    private final String TAG = "zad1";

    @BeforeEach
    void setUp() {
        rulePreset = new RulePreset();
        rulePreset.setMinimumTokensMatch(10);
        rulePreset.setEnableNormalization(true);

        subject = new Subject();
        subject.setOrganizationName(ORG_NAME);
        subject.setRulePreset(rulePreset);

        rulePreset.setSubject(subject);

        schedule = new Schedule();
        schedule.setTag(TAG);
        schedule.setRulePreset(rulePreset);
    }

    @Test
    public void POS01_ScheduleReady_ReportIsEventuallyGeneratedAndSaved() {
        GiteaRepoDTO validRepo = new GiteaRepoDTO("repo-student", "http://gitea/repo", false);
        GiteaRepoDTO templateRepo = new GiteaRepoDTO("template", "http://gitea/template", true);
        List<Repository> dbRepos = List.of(new Repository());

        JPlagResult mockResult = mock(JPlagResult.class);
        when(mockResult.getAllComparisons()).thenReturn(List.of());

        when(scheduleRepository.findSchedulesToExecute()).thenReturn(List.of(schedule));
        when(giteaService.getRepositoriesFromOrganization(ORG_NAME)).thenReturn(List.of(validRepo, templateRepo));
        when(giteaRepositoryRepository.getAllBySubjectId(subject.getId())).thenReturn(dbRepos);
        when(jPlagService.runPlagiarismScanOnOrganisation(eq(ORG_NAME), eq(TAG), eq(10), eq(true), anyString()))
                .thenReturn(mockResult);
        when(reportsRepository.calculateAggregatedAverageForSubject(subject.getId())).thenReturn(42.0);

        try (MockedStatic<GitService> mockedGit = mockStatic(GitService.class)) {
            reportService.generateReportBySchedule();

            mockedGit.verify(() -> GitService.cloneTag(eq("http://gitea/repo"), eq(TAG), any(File.class)), times(1));
            mockedGit.verify(() -> GitService.cloneTag(eq("http://gitea/template"), any(), any(File.class)), never());
        }

        verify(reportsRepository).saveAndFlush(reportCaptor.capture());
        Report saved = reportCaptor.getValue();
        assertEquals(subject, saved.getSubject());
        assertEquals(TAG, saved.getTag());
        assertEquals(dbRepos, saved.getRepositoryList());
        assertTrue(saved.getPathToReport().startsWith("reports/" + ORG_NAME + "/" + TAG + "/"));
        assertTrue(saved.getPathToReport().endsWith(".jplag"));
        assertEquals(0.0, saved.getAverageSimilarity());
    }

    @Test
    public void POS02_NoSchedulesReady_NothingHappens() {
        when(scheduleRepository.findSchedulesToExecute()).thenReturn(List.of());

        reportService.generateReportBySchedule();

        verifyNoInteractions(giteaService, jPlagService, giteaRepositoryRepository, reportsRepository);
    }

    @Test
    public void POS03_ScheduleIsNotDeletedAfterExecution() {
        JPlagResult mockResult = mock(JPlagResult.class);
        when(mockResult.getAllComparisons()).thenReturn(List.of());

        when(scheduleRepository.findSchedulesToExecute()).thenReturn(List.of(schedule));
        when(giteaService.getRepositoriesFromOrganization(ORG_NAME)).thenReturn(List.of());
        when(giteaRepositoryRepository.getAllBySubjectId(any())).thenReturn(List.of());
        when(jPlagService.runPlagiarismScanOnOrganisation(any(), any(), any(), any(), any())).thenReturn(mockResult);
        when(reportsRepository.calculateAggregatedAverageForSubject(any())).thenReturn(0.0);

        reportService.generateReportBySchedule();

        verify(scheduleRepository, never()).delete(any());
        verify(scheduleRepository, never()).deleteById(any());
    }

    @Test
    public void NEG01_GiteaUnavailable_ExceptionIsLoggedAndReportIsNotSaved() {
        when(scheduleRepository.findSchedulesToExecute()).thenReturn(List.of(schedule));
        when(giteaService.getRepositoriesFromOrganization(ORG_NAME))
                .thenThrow(new RuntimeException("Gitea unavailable"));

        assertThrows(RuntimeException.class, () -> reportService.generateReportBySchedule());

        verify(reportsRepository, never()).saveAndFlush(any());
    }

    @Test
    public void NEG02_JPlagFails_ExceptionIsLoggedAndReportIsNotSaved() {
        when(scheduleRepository.findSchedulesToExecute()).thenReturn(List.of(schedule));
        when(giteaService.getRepositoriesFromOrganization(ORG_NAME)).thenReturn(List.of());
        when(giteaRepositoryRepository.getAllBySubjectId(any())).thenReturn(List.of());
        when(jPlagService.runPlagiarismScanOnOrganisation(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("JPlag failed"));

        assertThrows(RuntimeException.class,() -> reportService.generateReportBySchedule());

        verify(reportsRepository, never()).saveAndFlush(any());
    }
}