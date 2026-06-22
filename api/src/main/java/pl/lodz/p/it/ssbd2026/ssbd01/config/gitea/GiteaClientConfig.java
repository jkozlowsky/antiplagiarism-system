package pl.lodz.p.it.ssbd2026.ssbd01.config.gitea;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@Slf4j
public class GiteaClientConfig {
    @Value("${gitea.url}")
    private String giteaUrl;

    @Value("${gitea.token}")
    private String giteaToken;

    @Bean
    public RestClient giteaRestClient() {
        log.warn("-----------gitea-url--------");
        log.warn(giteaUrl);
        return RestClient.builder()
                .baseUrl(giteaUrl)
                .defaultHeader("Authorization", "token " + giteaToken)
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
