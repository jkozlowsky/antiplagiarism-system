package pl.lodz.p.it.ssbd2026.ssbd01.mpp.mpp22;

import de.jplag.JPlagResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.GiteaRepoDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.ScheduleConfigDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.GiteaService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.ReportDatabaseService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.ReportService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.SubjectStatisticsService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.utils.GitService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.utils.JPlagService;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceGenerateByScheduleTest {

    @Mock
    private ReportDatabaseService reportDatabaseService;
    @Mock
    private GiteaService giteaService;
    @Mock
    private JPlagService jPlagService;
    @Mock
    private SubjectStatisticsService subjectStatisticsService;

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private ReportService reportService;

    private ScheduleConfigDTO config;
    private final String ORG_NAME = "TestOrg";
    private final String TAG = "zad1";
    private final UUID SCHEDULE_ID = UUID.randomUUID();
    private final UUID SUBJECT_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        config = new ScheduleConfigDTO(SCHEDULE_ID, SUBJECT_ID, ORG_NAME, TAG, 10, true);

        Subject mockSubject = new Subject();
        ReflectionTestUtils.setField(mockSubject, "id", SUBJECT_ID);
        mockSubject.setArchived(false);
        lenient().when(subjectRepository.findById(SUBJECT_ID)).thenReturn(Optional.of(mockSubject));
    }

    @Test
    public void POS01_ScheduleReady_ReportIsEventuallyGeneratedAndSaved() {
        GiteaRepoDTO validRepo = new GiteaRepoDTO("repo-student", "http://gitea/repo", false);
        GiteaRepoDTO templateRepo = new GiteaRepoDTO("template", "http://gitea/template", true);

        JPlagResult mockResult = mock(JPlagResult.class);
        when(mockResult.getAllComparisons()).thenReturn(List.of());

        when(reportDatabaseService.fetchNextScheduleToExecute()).thenReturn(config);

        when(giteaService.getRepositoriesFromOrganization(ORG_NAME)).thenReturn(List.of(validRepo, templateRepo));
        when(jPlagService.runPlagiarismScanOnOrganisation(eq(ORG_NAME), eq(TAG), eq(10), eq(true), anyString()))
                .thenReturn(mockResult);

        try (MockedStatic<GitService> mockedGit = mockStatic(GitService.class)) {
            mockedGit.when(() -> GitService.cloneTag(anyString(), anyString(), any(File.class), any())).thenReturn(true);

            boolean result = reportService.generateReportBySchedule();

            assertTrue(result);
            mockedGit.verify(() -> GitService.cloneTag(eq("http://gitea/repo"), eq(TAG), any(File.class), any()), times(1));
            mockedGit.verify(() -> GitService.cloneTag(eq("http://gitea/template"), any(), any(File.class), any()), never());
        }

        verify(reportDatabaseService).saveScheduleSuccess(eq(config), eq(1L), anyString(), eq(0.0));
        verify(subjectStatisticsService).recalculateAndUpdateStatistics(SUBJECT_ID);
    }

    @Test
    public void POS02_NoSchedulesReady_NothingHappens() {
        when(reportDatabaseService.fetchNextScheduleToExecute()).thenReturn(null);

        boolean result = reportService.generateReportBySchedule();

        assertFalse(result);
        verifyNoInteractions(giteaService, jPlagService, subjectStatisticsService);
        verify(reportDatabaseService, never()).saveScheduleSuccess(any(), anyLong(), anyString(), anyDouble());
        verify(reportDatabaseService, never()).saveScheduleFailure(any());
    }

    @Test
    public void POS03_ScheduleIsNotDeletedAfterExecution() {
        GiteaRepoDTO validRepo = new GiteaRepoDTO("repo-student", "http://gitea/repo", false);
        JPlagResult mockResult = mock(JPlagResult.class);
        when(mockResult.getAllComparisons()).thenReturn(List.of());

        when(reportDatabaseService.fetchNextScheduleToExecute()).thenReturn(config);
        when(giteaService.getRepositoriesFromOrganization(ORG_NAME)).thenReturn(List.of(validRepo));
        when(jPlagService.runPlagiarismScanOnOrganisation(any(), any(), any(), any(), any())).thenReturn(mockResult);

        try (MockedStatic<GitService> mockedGit = mockStatic(GitService.class)) {
            mockedGit.when(() -> GitService.cloneTag(anyString(), anyString(), any(File.class), any())).thenReturn(true);

            boolean result = reportService.generateReportBySchedule();

            assertTrue(result);

            verify(reportDatabaseService).saveScheduleSuccess(eq(config), eq(1L), anyString(), eq(0.0));
        }
    }

    @Test
    public void NEG01_GiteaUnavailable_ExceptionIsLoggedAndReportIsNotSaved() {
        when(reportDatabaseService.fetchNextScheduleToExecute()).thenReturn(config);

        when(giteaService.getRepositoriesFromOrganization(ORG_NAME))
                .thenThrow(new RuntimeException("Gitea unavailable"));

        boolean result = reportService.generateReportBySchedule();

        assertTrue(result);

        verify(reportDatabaseService, never()).saveScheduleSuccess(any(), anyLong(), anyString(), anyDouble());

        verify(reportDatabaseService, times(1)).saveScheduleFailure(SCHEDULE_ID);
        verifyNoInteractions(subjectStatisticsService);
    }

    @Test
    public void NEG02_JPlagFails_ExceptionIsLoggedAndReportIsNotSaved() {
        GiteaRepoDTO validRepo = new GiteaRepoDTO("repo-student", "http://gitea/repo", false);

        when(reportDatabaseService.fetchNextScheduleToExecute()).thenReturn(config);
        when(giteaService.getRepositoriesFromOrganization(ORG_NAME)).thenReturn(List.of(validRepo));

        when(jPlagService.runPlagiarismScanOnOrganisation(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("JPlag failed"));

        try (MockedStatic<GitService> mockedGit = mockStatic(GitService.class)) {
            mockedGit.when(() -> GitService.cloneTag(anyString(), anyString(), any(File.class), any())).thenReturn(true);

            boolean result = reportService.generateReportBySchedule();

            assertTrue(result);

            verify(reportDatabaseService, never()).saveScheduleSuccess(any(), anyLong(), anyString(), anyDouble());

            verify(reportDatabaseService, times(1)).saveScheduleFailure(SCHEDULE_ID);
            verifyNoInteractions(subjectStatisticsService);
        }
    }
}