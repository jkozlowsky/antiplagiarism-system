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
import org.springframework.data.domain.PageRequest;
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
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.SubjectStatisticsService;
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
    @Mock
    private SubjectStatisticsService subjectStatisticsService;

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

        when(scheduleRepository.findSchedulesToExecute(PageRequest.of(0,1))).thenReturn(List.of(schedule));
        when(giteaService.getRepositoriesFromOrganization(ORG_NAME)).thenReturn(List.of(validRepo, templateRepo));
        when(giteaRepositoryRepository.getAllBySubjectId(subject.getId())).thenReturn(dbRepos);
        when(jPlagService.runPlagiarismScanOnOrganisation(eq(ORG_NAME), eq(TAG), eq(10), eq(true), anyString()))
                .thenReturn(mockResult);
        // Removed unnecessary calculateAggregatedAverageForSubject stub

        try (MockedStatic<GitService> mockedGit = mockStatic(GitService.class)) {
            // FIX: Explicitly tell the mock to return true so the clone is "successful"
            mockedGit.when(() -> GitService.cloneTag(anyString(), anyString(), any(File.class), any())).thenReturn(true);

            reportService.generateReportBySchedule();

            mockedGit.verify(() -> GitService.cloneTag(eq("http://gitea/repo"), eq(TAG), any(File.class), any()), times(1));
            mockedGit.verify(() -> GitService.cloneTag(eq("http://gitea/template"), any(), any(File.class), any()), never());
        }

        verify(reportsRepository).saveAndFlush(reportCaptor.capture());
        verify(subjectStatisticsService).recalculateAndUpdateStatistics(subject.getId()); // Verify new service call

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
        when(scheduleRepository.findSchedulesToExecute(PageRequest.of(0,1))).thenReturn(List.of());

        reportService.generateReportBySchedule();

        verifyNoInteractions(giteaService, jPlagService, giteaRepositoryRepository, reportsRepository);
    }

    @Test
    public void POS03_ScheduleIsNotDeletedAfterExecution() {
        GiteaRepoDTO validRepo = new GiteaRepoDTO("repo-student", "http://gitea/repo", false);
        JPlagResult mockResult = mock(JPlagResult.class);
        when(mockResult.getAllComparisons()).thenReturn(List.of());

        when(scheduleRepository.findSchedulesToExecute(PageRequest.of(0,1))).thenReturn(List.of(schedule));

        // FIX: Provide a repo so it actually proceeds to the logic
        when(giteaService.getRepositoriesFromOrganization(ORG_NAME)).thenReturn(List.of(validRepo));
        when(giteaRepositoryRepository.getAllBySubjectId(any())).thenReturn(List.of());
        when(jPlagService.runPlagiarismScanOnOrganisation(any(), any(), any(), any(), any())).thenReturn(mockResult);

        try (MockedStatic<GitService> mockedGit = mockStatic(GitService.class)) {
            // FIX: Ensure clone passes
            mockedGit.when(() -> GitService.cloneTag(anyString(), anyString(), any(File.class), any())).thenReturn(true);

            reportService.generateReportBySchedule();

            verify(scheduleRepository, never()).delete(any());
            verify(scheduleRepository, never()).deleteById(any());
        }
    }

    @Test
    public void NEG01_GiteaUnavailable_ExceptionIsLoggedAndReportIsNotSaved() {
        when(scheduleRepository.findSchedulesToExecute(PageRequest.of(0,1))).thenReturn(List.of(schedule));
        when(giteaService.getRepositoriesFromOrganization(ORG_NAME))
                .thenThrow(new RuntimeException("Gitea unavailable"));

        boolean result = reportService.generateReportBySchedule();

        assertTrue(result);
        assertTrue(schedule.isExecuted());
        assertTrue(schedule.isHasFailed());
        verify(reportsRepository, never()).saveAndFlush(any(Report.class));
        verify(scheduleRepository, times(1)).saveAndFlush(schedule);
    }

    @Test
    public void NEG02_JPlagFails_ExceptionIsLoggedAndReportIsNotSaved() {
        GiteaRepoDTO validRepo = new GiteaRepoDTO("repo-student", "http://gitea/repo", false);

        when(scheduleRepository.findSchedulesToExecute(PageRequest.of(0,1))).thenReturn(List.of(schedule));
        // FIX: Give it a repo to scan so it doesn't fail at the Git stage
        when(giteaService.getRepositoriesFromOrganization(ORG_NAME)).thenReturn(List.of(validRepo));
        when(giteaRepositoryRepository.getAllBySubjectId(any())).thenReturn(List.of());
        when(jPlagService.runPlagiarismScanOnOrganisation(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("JPlag failed"));

        try (MockedStatic<GitService> mockedGit = mockStatic(GitService.class)) {
            // FIX: Ensure clone passes so it reaches JPlag
            mockedGit.when(() -> GitService.cloneTag(anyString(), anyString(), any(File.class), any())).thenReturn(true);

            boolean result = reportService.generateReportBySchedule();

            assertTrue(result);
            assertTrue(schedule.isExecuted());
            assertTrue(schedule.isHasFailed());
            verify(reportsRepository, never()).saveAndFlush(any(Report.class));
            verify(scheduleRepository, times(1)).saveAndFlush(schedule);
        }
    }
}