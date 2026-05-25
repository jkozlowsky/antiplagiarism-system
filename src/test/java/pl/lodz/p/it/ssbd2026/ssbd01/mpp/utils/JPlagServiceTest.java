package pl.lodz.p.it.ssbd2026.ssbd01.mpp.utils;
import de.jplag.JPlag;
import de.jplag.JPlagResult;
import de.jplag.exceptions.ExitException;
import de.jplag.reporting.reportobject.ReportObjectFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.JplagScanException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ReportFileException;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JPlagServiceTest {

    @InjectMocks
    private JPlagService jPlagService;

    @Test
    void runPlagiarismScanOnOrganisation_Success() {
        String orgName = "TestOrg";
        String tag = "v1.0";
        Integer minTokens = 12;
        Boolean normalize = true;
        String reportPath = "reports/TestOrg/v1.0/timestamp.jplag";

        JPlagResult mockResult = mock(JPlagResult.class);

        try (MockedStatic<JPlag> mockedJPlag = mockStatic(JPlag.class);
             MockedConstruction<ReportObjectFactory> mockedFactory = mockConstruction(ReportObjectFactory.class)) {

            mockedJPlag.when(() -> JPlag.run(any())).thenReturn(mockResult);

            JPlagResult actualResult = jPlagService.runPlagiarismScanOnOrganisation(
                    orgName, tag, minTokens, normalize, reportPath
            );

            assertEquals(mockResult, actualResult);

            assertEquals(1, mockedFactory.constructed().size());
            ReportObjectFactory factoryInstance = mockedFactory.constructed().get(0);

            verify(factoryInstance).createAndSaveReport(mockResult);
        }
    }

    @Test
    void runPlagiarismScanOnOrganisation_ThrowsJplagScanException_WhenJPlagFails() {
        String orgName = "TestOrg";
        String tag = "v1.0";
        String reportPath = "reports/path";

        try (MockedStatic<JPlag> mockedJPlag = mockStatic(JPlag.class)) {

            ExitException mockExitException = mock(ExitException.class);
            mockedJPlag.when(() -> JPlag.run(any())).thenThrow(mockExitException);

            assertThrows(JplagScanException.class, () ->
                    jPlagService.runPlagiarismScanOnOrganisation(
                            orgName, tag, 12, true, reportPath
                    )
            );
        }
    }

    @Test
    void runPlagiarismScanOnOrganisation_ThrowsReportFileException_WhenFileNotFound() {
        String orgName = "TestOrg";
        String tag = "v1.0";

        String reportPath = System.getProperty("user.dir");

        JPlagResult mockResult = mock(JPlagResult.class);

        try (MockedStatic<JPlag> mockedJPlag = mockStatic(JPlag.class)) {

            mockedJPlag.when(() -> JPlag.run(any())).thenReturn(mockResult);

            assertThrows(ReportFileException.class, () ->
                    jPlagService.runPlagiarismScanOnOrganisation(
                            orgName, tag, 12, true, reportPath
                    )
            );
        }
    }
}