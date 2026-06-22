package pl.lodz.p.it.ssbd2026.ssbd01;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;
import pl.lodz.p.it.ssbd2026.ssbd01.config.gitea.GiteaClientConfig;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers.ReportController;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.ReportService;

import java.util.UUID;

@SpringBootApplication()
@EnableScheduling
@EnableRetry
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableFeignClients
public class Ssbd01api {

    static void main(String[] args) {
        SpringApplication.run(Ssbd01api.class, args);
//        ReportService reportService = new ReportService(new GiteaClientConfig().giteaRestClient());
//        reportService.generateReportOnDemand(new UUID(1L, 1L), "v1");
    }
}