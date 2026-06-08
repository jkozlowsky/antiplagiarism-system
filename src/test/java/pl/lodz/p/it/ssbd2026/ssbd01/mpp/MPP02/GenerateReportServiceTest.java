package pl.lodz.p.it.ssbd2026.ssbd01.mpp.MPP02;

import de.jplag.JPlagResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.GiteaRepoDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.SubjectConfigDTO; // Upewnij się, że masz poprawny import!
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.GitCloneException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectAccessDeniedException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.GiteaService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.ReportDatabaseService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.ReportService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.SubjectStatisticsService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.utils.GitService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.utils.JPlagService;

import java.io.File;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GenerateReportServiceTest {

    @Mock
    private ReportDatabaseService reportDatabaseService; // 👈 Nowy główny mock bazy
    @Mock
    private GiteaService giteaService;
    @Mock
    private JPlagService jPlagService;
    @Mock
    private SubjectStatisticsService subjectStatisticsService;

    @InjectMocks
    private ReportService reportService;

    @Test
    void generateReportOnDemand_Success() {
        UUID subjectId = UUID.randomUUID();
        String tag = "v1.0";
        String orgName = "TestOrg";
        String login = "teacher123";

        SubjectConfigDTO configDTO = new SubjectConfigDTO(orgName, 12, true);
        when(reportDatabaseService.verifyAccessAndGetConfig(subjectId, login)).thenReturn(configDTO);

        GiteaRepoDTO validRepo = new GiteaRepoDTO("test-repo", "url1", false);
        GiteaRepoDTO templateRepo = new GiteaRepoDTO("template-repo", "url2", true);
        when(giteaService.getRepositoriesFromOrganization(orgName)).thenReturn(List.of(validRepo, templateRepo));

        JPlagResult mockResult = mock(JPlagResult.class);
        when(mockResult.getAllComparisons()).thenReturn(List.of());

        when(jPlagService.runPlagiarismScanOnOrganisation(
                eq(orgName),
                eq(tag),
                eq(12),
                eq(true),
                anyString()
        )).thenReturn(mockResult);

        Report expectedReport = new Report();
        when(reportDatabaseService.saveGeneratedReport(
                eq(subjectId), eq(tag), eq(1L), anyString(), eq(0.0)
        )).thenReturn(expectedReport);

        ReflectionTestUtils.setField(reportService, "giteaToken", "dummy-token-123");

        try (MockedStatic<GitService> mockedGitService = mockStatic(GitService.class)) {

            mockedGitService.when(() -> GitService.cloneTag(
                    anyString(), anyString(), any(File.class), anyString()
            )).thenReturn(true);

            Report actualReport = reportService.generateReportOnDemand(subjectId, tag, login);

            assertEquals(expectedReport, actualReport);

            mockedGitService.verify(
                    () -> GitService.cloneTag(
                            eq("url1"),
                            eq(tag),
                            any(File.class),
                            anyString()
                    ), times(1)
            );

            verify(reportDatabaseService, times(1)).saveGeneratedReport(eq(subjectId), eq(tag), eq(1L), anyString(), eq(0.0));
            verify(subjectStatisticsService, times(1)).recalculateAndUpdateStatistics(subjectId);
        }
    }

    @Test
    void generateReportOnDemand_ThrowsException_WhenSubjectNotFound() {
        UUID subjectId = UUID.randomUUID();
        String login = "teacher123";

        when(reportDatabaseService.verifyAccessAndGetConfig(subjectId, login))
                .thenThrow(new SubjectNotFoundException());

        assertThrows(SubjectNotFoundException.class, () ->
                reportService.generateReportOnDemand(subjectId, "v1.0", login)
        );

        verifyNoInteractions(giteaService, jPlagService, subjectStatisticsService);
    }

    @Test
    void generateReportOnDemand_ThrowsException_WhenTeacherNotAssigned() {
        UUID subjectId = UUID.randomUUID();
        String login = "unauthorizedTeacher";
        String tag = "v1.0";

        when(reportDatabaseService.verifyAccessAndGetConfig(subjectId, login))
                .thenThrow(new SubjectAccessDeniedException());

        assertThrows(SubjectAccessDeniedException.class, () ->
                reportService.generateReportOnDemand(subjectId, tag, login)
        );

        verifyNoInteractions(giteaService, jPlagService, subjectStatisticsService);
    }

    @Test
    void generateReportOnDemand_ThrowsException_WhenOnlyTemplateReposExist() {
        UUID subjectId = UUID.randomUUID();
        String tag = "v1.0";
        String login = "teacher123";

        SubjectConfigDTO configDTO = new SubjectConfigDTO("TestOrg", 10, true);
        when(reportDatabaseService.verifyAccessAndGetConfig(subjectId, login)).thenReturn(configDTO);

        GiteaRepoDTO templateRepo = new GiteaRepoDTO("template-repo","url2", true);
        when(giteaService.getRepositoriesFromOrganization("TestOrg")).thenReturn(List.of(templateRepo));

        try (MockedStatic<GitService> mockedGitService = mockStatic(GitService.class)) {

            assertThrows(GitCloneException.class, () -> {
                reportService.generateReportOnDemand(subjectId, tag, login);
            });

            mockedGitService.verifyNoInteractions();
            verify(reportDatabaseService, never()).saveGeneratedReport(any(), any(), anyLong(), any(), anyDouble());
            verifyNoInteractions(subjectStatisticsService);
        }
    }
}