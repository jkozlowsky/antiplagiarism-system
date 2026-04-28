package pl.lodz.p.it.ssbd2026.ssbd01;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class Ssbd01api {

    static void main(String[] args) {
        SpringApplication.run(Ssbd01api.class, args);
    }
}