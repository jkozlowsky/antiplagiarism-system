package pl.lodz.p.it.ssbd2026.mcpserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.ssbd2026.mcpserver.dto.ReportDetailsDTO;
import pl.lodz.p.it.ssbd2026.mcpserver.service.GroqSummaryService;

@RestController
@RequestMapping("/summaries")
@RequiredArgsConstructor
public class SummaryController {

    private final GroqSummaryService groqSummaryService;

    @PostMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public String summarize(@RequestBody ReportDetailsDTO report) {
        return groqSummaryService.summarize(report);
    }
}
