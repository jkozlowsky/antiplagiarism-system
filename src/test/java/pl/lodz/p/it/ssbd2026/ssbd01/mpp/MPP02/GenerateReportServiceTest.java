package pl.lodz.p.it.ssbd2026.ssbd01.mpp.MPP02;

import de.jplag.JPlagResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.GiteaRepoDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Report;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Repository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.SubjectNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.GiteaRepositoryRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.ReportsRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.GiteaService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.ReportService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.utils.GitService;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.utils.JPlagService;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GenerateReportServiceTest {
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

    @InjectMocks
    private ReportService reportService;

    @Captor
    private ArgumentCaptor<Report> reportCaptor;

    @Test
    void generateReportOnDemand_Success() {
        UUID subjectId = UUID.randomUUID();
        String tag = "v1.0";
        String orgName = "TestOrg";

        RulePreset rulePreset = new RulePreset();
        rulePreset.setMinimumTokensMatch(12);
        rulePreset.setEnableNormalization(true);

        Subject subject = new Subject();
        subject.setOrganizationName(orgName);
        subject.setRulePreset(rulePreset);

        GiteaRepoDTO validRepo = new GiteaRepoDTO("test-repo", "url1", false);
        GiteaRepoDTO templateRepo = new GiteaRepoDTO("template-repo", "url2", true);

        List<Repository> dbRepositories = List.of(new Repository(), new Repository());

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(giteaService.getRepositoriesFromOrganization(orgName)).thenReturn(List.of(validRepo, templateRepo));
        when(giteaRepositoryRepository.getAllBySubjectId(subjectId)).thenReturn(dbRepositories);

        JPlagResult mockResult = mock(JPlagResult.class);
        when(mockResult.getAllComparisons()).thenReturn(List.of());

        when(jPlagService.runPlagiarismScanOnOrganisation(
                eq(orgName),
                eq(tag),
                eq(12),
                eq(true),
                anyString()
        )).thenReturn(mockResult);

        try (MockedStatic<GitService> mockedGitService = mockStatic(GitService.class)) {
            reportService.generateReportOnDemand(subjectId, tag);
            mockedGitService.verify(
                    () -> GitService.cloneTag(
                            eq("url1"),
                            eq(tag),
                            any(File.class)
                    ), times(1)
            );
        }

        verify(reportsRepository).saveAndFlush(reportCaptor.capture());
        Report savedReport = reportCaptor.getValue();

        assertEquals(subject, savedReport.getSubject());
        assertEquals(tag, savedReport.getTag());
        assertEquals(dbRepositories, savedReport.getRepositoryList());

        String expectedPathPrefix = "reports/" + orgName + "/" + tag + "/";
        assertTrue(savedReport.getPathToReport().startsWith(expectedPathPrefix));
        assertTrue(savedReport.getPathToReport().endsWith(".jplag"));
    }

    @Test
    void generateReportOnDemand_ThrowsException_WhenSubjectNotFound() {
        UUID subjectId = UUID.randomUUID();
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        assertThrows(SubjectNotFoundException.class, () ->
                reportService.generateReportOnDemand(subjectId, "v1.0")
        );

        verifyNoInteractions(giteaService, jPlagService, giteaRepositoryRepository, reportsRepository);
    }

    @Test
    void generateReportOnDemand_DoesNotClone_WhenOnlyTemplateReposExist() {
        UUID subjectId = UUID.randomUUID();
        String tag = "v1.0";
        Subject subject = new Subject();
        subject.setOrganizationName("TestOrg");
        subject.setRulePreset(new RulePreset());

        GiteaRepoDTO templateRepo = new GiteaRepoDTO("template-repo","url2", true);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(giteaService.getRepositoriesFromOrganization("TestOrg")).thenReturn(List.of(templateRepo));
        JPlagResult mockResult = mock(JPlagResult.class);
        when(mockResult.getAllComparisons()).thenReturn(List.of());

        when(jPlagService.runPlagiarismScanOnOrganisation(
                eq("TestOrg"),
                eq(tag),
                eq(null),
                eq(null),
                anyString()
        )).thenReturn(mockResult);

        try (MockedStatic<GitService> mockedGitService = mockStatic(GitService.class)) {
            reportService.generateReportOnDemand(subjectId, tag);
            mockedGitService.verifyNoInteractions();
        }
    }
}
