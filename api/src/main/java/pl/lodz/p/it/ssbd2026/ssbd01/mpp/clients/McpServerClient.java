package pl.lodz.p.it.ssbd2026.ssbd01.mpp.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.TeacherReportDetailsDTO;

@FeignClient(name = "mcp-server")
public interface McpServerClient {

    @PostMapping(value = "/summaries", consumes = "application/json", produces = "text/plain")
    String summarizeReport(@RequestBody TeacherReportDetailsDTO report);
}
