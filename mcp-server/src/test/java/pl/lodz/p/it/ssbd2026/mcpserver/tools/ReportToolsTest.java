package pl.lodz.p.it.ssbd2026.mcpserver.tools;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.ssbd2026.mcpserver.client.ReportApiClient;
import pl.lodz.p.it.ssbd2026.mcpserver.dto.ComparisonDTO;
import pl.lodz.p.it.ssbd2026.mcpserver.dto.ReportDetailsDTO;
import pl.lodz.p.it.ssbd2026.mcpserver.dto.ReportSummaryDTO;
import pl.lodz.p.it.ssbd2026.mcpserver.dto.RestPage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReportToolsTest {

    @Mock
    private ReportApiClient reportApiClient;

    @Mock
    private GroqSummaryService groqSummaryService;

    @InjectMocks
    private ReportTools reportTools;

    private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private ReportDetailsDTO sampleReport() {
        return new ReportDetailsDTO(ID, "lab1", "Programowanie", LocalDateTime.now(), 0.42,
                List.of(new ComparisonDTO("studentA", "studentB", 0.9, 0.7, 12.0)));
    }

    private static FeignException feignWithStatus(int status) {
        Request request = Request.create(Request.HttpMethod.GET, "http://api/reports",
                java.util.Map.of(), null, java.nio.charset.StandardCharsets.UTF_8, new RequestTemplate());
        Response response = Response.builder()
                .status(status)
                .reason("test")
                .request(request)
                .headers(java.util.Map.of())
                .build();
        return FeignException.errorStatus("ReportApiClient#call", response);
    }

    @Test
    void summarize_happyPath_returnsGroqSummary() {
        given(reportApiClient.getReportDetails(ID)).willReturn(sampleReport());
        given(groqSummaryService.summarize(org.mockito.ArgumentMatchers.any())).willReturn("STRESZCZENIE");

        String result = reportTools.summarizeReport(ID.toString());

        assertThat(result).isEqualTo("STRESZCZENIE");
        verify(groqSummaryService).summarize(org.mockito.ArgumentMatchers.argThat(r -> r.id().equals(ID)));
    }

    @Test
    void summarize_notFound_returnsFriendlyMessage() {
        given(reportApiClient.getReportDetails(ID)).willThrow(feignWithStatus(404));
        assertThat(reportTools.summarizeReport(ID.toString())).contains("Nie znaleziono");
    }

    @Test
    void summarize_forbidden_returnsAccessMessage() {
        given(reportApiClient.getReportDetails(ID)).willThrow(feignWithStatus(403));
        assertThat(reportTools.summarizeReport(ID.toString()))
                .contains("Brak uprawnień").contains("prowadzący");
    }

    @Test
    void summarize_unauthorized_returnsAuthMessage() {
        given(reportApiClient.getReportDetails(ID)).willThrow(feignWithStatus(401));
        assertThat(reportTools.summarizeReport(ID.toString()))
                .contains("Wymagane uwierzytelnienie");
    }

    @Test
    void summarize_apiDown_returnsServiceUnavailableMessage() {
        given(reportApiClient.getReportDetails(ID)).willThrow(feignWithStatus(503));
        assertThat(reportTools.summarizeReport(ID.toString()))
                .contains("api").contains("niedostępna");
    }

    @Test
    void invalidUuid_throwsIllegalArgument() {
        assertThatThrownBy(() -> reportTools.summarizeReport("not-a-uuid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UUID");
    }

    @Test
    void blankUuid_throwsIllegalArgument() {
        assertThatThrownBy(() -> reportTools.summarizeReport("  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("wymagane");
    }

    @Test
    void listReports_formatsContent() {
        ReportSummaryDTO r = new ReportSummaryDTO(ID, "lab1", 0.42, LocalDateTime.now(), "teacher", "Programowanie", 5L);
        given(reportApiClient.getReportsForSubject(ID))
                .willReturn(new RestPage<>(List.of(r), 1, 1, 0, 20));

        String result = reportTools.listReportsForSubject(ID.toString());

        assertThat(result).contains("lab1").contains("42").contains("%").contains(ID.toString());
    }

    @Test
    void listReports_empty_returnsNoReportsMessage() {
        given(reportApiClient.getReportsForSubject(ID))
                .willReturn(new RestPage<>(List.of(), 0, 0, 0, 20));
        assertThat(reportTools.listReportsForSubject(ID.toString())).contains("Brak raportów");
    }

    @Test
    void getReportDetails_feignError_throwsIllegalState() {
        given(reportApiClient.getReportDetails(ID)).willThrow(feignWithStatus(403));
        assertThatThrownBy(() -> reportTools.getReportDetails(ID.toString()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Brak uprawnień");
    }
}
