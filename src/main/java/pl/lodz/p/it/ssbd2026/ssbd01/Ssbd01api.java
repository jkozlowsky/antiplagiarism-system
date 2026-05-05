package pl.lodz.p.it.ssbd2026.ssbd01;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication()
@EnableScheduling
@EnableRetry
@EnableJpaAuditing
public class Ssbd01api {

    static void main(String[] args) {
        SpringApplication.run(Ssbd01api.class, args);
    }
}