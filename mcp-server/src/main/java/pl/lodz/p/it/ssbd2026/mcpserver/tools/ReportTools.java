package pl.lodz.p.it.ssbd2026.mcpserver.tools;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.ssbd2026.mcpserver.client.ReportApiClient;
import pl.lodz.p.it.ssbd2026.mcpserver.dto.ReportDetailsDTO;
import pl.lodz.p.it.ssbd2026.mcpserver.dto.ReportSummaryDTO;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportTools {

    private final ReportApiClient reportApiClient;
    private final GroqSummaryService groqSummaryService;

    @McpTool(
            name = "list_reports_for_subject",
            description = "Zwraca listę raportów antyplagiatowych dla danego przedmiotu (subjectId jako UUID). "
                    + "Wymaga tokenu prowadzącego przypisanego do przedmiotu.")
    public String listReportsForSubject(
            @McpToolParam(description = "UUID przedmiotu", required = true) String subjectId) {
        UUID id = parseUuid(subjectId, "subjectId");
        List<ReportSummaryDTO> reports;
        try {
            reports = reportApiClient.getReportsForSubject(id).contentOrEmpty();
        } catch (FeignException e) {
            return feignMessage(e, "listy raportów dla przedmiotu " + id);
        }
        if (reports.isEmpty()) {
            return "Brak raportów dla przedmiotu " + id + ".";
        }
        StringBuilder sb = new StringBuilder("Raporty dla przedmiotu " + id + ":\n");
        reports.forEach(r -> sb.append("- id=").append(r.id())
                .append(", tag=").append(r.tag())
                .append(", średnie podobieństwo=").append(formatPercent(r.average_similarity()))
                .append(", przeskanowane repozytoria=").append(r.scanned_repositories())
                .append(", data=").append(r.created_at())
                .append('\n'));
        return sb.toString();
    }

    @McpTool(
            name = "get_report_details",
            description = "Zwraca szczegóły raportu antyplagiatowego (porównania par zgłoszeń i podobieństwa) dla danego reportId (UUID). "
                    + "Wymaga tokenu prowadzącego z dostępem do raportu.")
    public ReportDetailsDTO getReportDetails(
            @McpToolParam(description = "UUID raportu", required = true) String reportId) {
        UUID id = parseUuid(reportId, "reportId");
        try {
            return reportApiClient.getReportDetails(id);
        } catch (FeignException e) {
            throw new IllegalStateException(feignMessage(e, "szczegółów raportu " + id), e);
        }
    }

    @McpTool(
            name = "summarize_report",
            description = "Generuje zwięzłe podsumowanie raportu antyplagiatowego po polsku (model na Groq) dla danego reportId (UUID). "
                    + "Dostęp tylko dla prowadzącego przypisanego do przedmiotu.")
    public String summarizeReport(
            @McpToolParam(description = "UUID raportu", required = true) String reportId) {
        UUID id = parseUuid(reportId, "reportId");
        ReportDetailsDTO report;
        try {
            report = reportApiClient.getReportDetails(id);
        } catch (FeignException e) {
            return feignMessage(e, "raportu " + id);
        }
        log.info("Generuję podsumowanie raportu {} ({} porównań)", report.id(),
                report.comparisons() == null ? 0 : report.comparisons().size());
        return groqSummaryService.summarize(report);
    }

    private UUID parseUuid(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Pole '" + field + "' jest wymagane.");
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Pole '" + field + "' nie jest poprawnym UUID: " + value);
        }
    }

    private String feignMessage(FeignException e, String what) {
        return switch (e.status()) {
            case 401 -> "Wymagane uwierzytelnienie: przekaż ważny token prowadzącego w nagłówku Authorization.";
            case 403 -> "Brak uprawnień: dostęp ma wyłącznie prowadzący przypisany do przedmiotu.";
            case 404 -> "Nie znaleziono " + what + ".";
            default -> {
                log.error("Błąd komunikacji z api podczas pobierania {}: status={}", what, e.status(), e);
                yield "Usługa raportów (api) jest chwilowo niedostępna. Spróbuj ponownie za chwilę.";
            }
        };
    }

    private String formatPercent(Double value) {
        return value == null ? "b/d" : String.format("%.1f%%", value * 100);
    }
}
