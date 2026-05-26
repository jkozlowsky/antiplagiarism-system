package pl.lodz.p.it.ssbd2026.ssbd01.mpp.mpp05;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Report;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.ReportsRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.ReportService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemoveReportTest {

    @Mock
    private ReportsRepository reportsRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void deleteReport_Success_WhenFileExists() {
        UUID reportId = UUID.randomUUID();
        Report report = new Report();
        report.setPathToReport("reports/TestOrg/v1/timestamp.jplag");

        when(reportsRepository.findById(reportId)).thenReturn(Optional.of(report));

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.deleteIfExists(any(Path.class))).thenReturn(true);

            reportService.deleteReport(reportId);

            mockedFiles.verify(() -> Files.deleteIfExists(eq(Path.of(report.getPathToReport()))));
            verify(reportsRepository).delete(report);
        }
    }

    @Test
    void deleteReport_Success_WhenFileIsAlreadyMissingOnDisk() {
        UUID reportId = UUID.randomUUID();
        Report report = new Report();
        report.setPathToReport("reports/TestOrg/v1/missing_file.jplag");

        when(reportsRepository.findById(reportId)).thenReturn(Optional.of(report));

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.deleteIfExists(any(Path.class))).thenReturn(false);

            assertDoesNotThrow(() -> reportService.deleteReport(reportId));

            verify(reportsRepository).delete(report);
        }
    }

    @Test
    void deleteReport_ThrowsException_WhenIoFails() {
        UUID reportId = UUID.randomUUID();
        Report report = new Report();
        report.setPathToReport("reports/TestOrg/v1/locked_file.jplag");

        when(reportsRepository.findById(reportId)).thenReturn(Optional.of(report));

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.deleteIfExists(any(Path.class)))
                    .thenThrow(new IOException("File is locked"));

            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    reportService.deleteReport(reportId)
            );
            assertTrue(exception.getMessage().contains("Could not delete the report file"));

            verify(reportsRepository, never()).delete(any());
        }
    }

}