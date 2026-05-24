package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.GiteaRepoDTO;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GiteaService {

    private final RestClient giteaClient;

    public List<GiteaRepoDTO> getRepositoriesFromOrganization(String orgName) {
        return giteaClient
                .get()
                .uri("/orgs/" + orgName + "/repos" + "?limit=1000")
                .retrieve()
                .body(new ParameterizedTypeReference<List<GiteaRepoDTO>>() {
                });

    }

}
