package pl.lodz.p.it.ssbd2026.ssbd01.mpp.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.ReportService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportScheduler {
    private final ReportService reportService;

    @Scheduled(cron = "0 * * * * *")
    public void schedule() {
        reportService.generateReportBySchedule();
    }
}