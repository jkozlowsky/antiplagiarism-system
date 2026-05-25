package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.GiteaRepoDTO;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GiteaService {

    private final RestClient giteaClient;

    public List<GiteaRepoDTO> getRepositoriesFromOrganization(String orgName) {
        String url = "api/v1/orgs/" + orgName + "/repos" + "?limit=1000";
        return giteaClient
                .get()
                .uri(url)
                .retrieve()
                .body(new ParameterizedTypeReference<List<GiteaRepoDTO>>() {
                });

    }

}
