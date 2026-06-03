package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.GiteaRepoDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.GiteaUserDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.GiteaIntegrationException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiteaService {

    private final RestClient giteaClient;

    public List<GiteaRepoDTO> getRepositoriesFromOrganization(String orgName) {
        String url = "api/v1/orgs/" + orgName + "/repos?limit=1000";
        try {
            return giteaClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<GiteaRepoDTO>>() {});
        } catch (HttpClientErrorException.NotFound e) {
            return List.of();
        } catch (RestClientException e) {
            throw new GiteaIntegrationException(e);
        }
    }

    public List<GiteaUserDTO> getRepoCollaborators(String orgName, String repoName) {
        String url = "api/v1/repos/" + orgName + "/" + repoName + "/collaborators";
        try {
            return giteaClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<GiteaUserDTO>>() {});
        } catch (HttpClientErrorException.NotFound e) {
            return List.of();
        } catch (RestClientException e) {
            throw new GiteaIntegrationException(e);
        }
    }

    /**
     * Usuwa organizację (przedmiot) z serwera Gitea.
     *
     * @param organizationName Nazwa organizacji/przedmiotu w Gitea
     */
    public void deleteGiteaOrganization(String organizationName) {
        try {
            giteaClient.delete()
                    .uri("/api/v1/orgs/{org}", organizationName)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Organizacja {} nie istnieje na serwerze Gitea, pomijanie usuwania.", organizationName);
        } catch (RestClientException e) {
            throw new GiteaIntegrationException(e);
        }
    }
}