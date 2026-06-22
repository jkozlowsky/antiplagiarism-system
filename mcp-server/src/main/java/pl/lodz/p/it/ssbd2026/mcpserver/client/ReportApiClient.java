package pl.lodz.p.it.ssbd2026.mcpserver.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pl.lodz.p.it.ssbd2026.mcpserver.config.FeignAuthForwardingConfig;
import pl.lodz.p.it.ssbd2026.mcpserver.dto.ReportDetailsDTO;
import pl.lodz.p.it.ssbd2026.mcpserver.dto.ReportSummaryDTO;
import pl.lodz.p.it.ssbd2026.mcpserver.dto.RestPage;

import java.util.UUID;

@FeignClient(name = "api", path = "/api", configuration = FeignAuthForwardingConfig.class)
public interface ReportApiClient {

    @GetMapping("/reports/subject/{subjectId}")
    RestPage<ReportSummaryDTO> getReportsForSubject(@PathVariable UUID subjectId);

    @GetMapping("/reports/{reportId}")
    ReportDetailsDTO getReportDetails(@PathVariable UUID reportId);
}
