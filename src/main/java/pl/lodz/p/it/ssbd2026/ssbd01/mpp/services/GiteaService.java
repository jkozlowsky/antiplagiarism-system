package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
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
        String url = "api/v1/orgs/" + orgName + "/repos" + "?limit=1000";
        return giteaClient
                .get()
                .uri(url)
                .retrieve()
                .body(new ParameterizedTypeReference<List<GiteaRepoDTO>>() {
                });
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

            log.info("Organizacja {} została pomyślnie usunięta z serwera Gitea.", organizationName);

        } catch (RestClientException e) {
            log.error("Błąd podczas usuwania organizacji {} z serwera Gitea", organizationName, e);
            throw new RuntimeException("Nie udało się usunąć przedmiotu z zewnętrznego serwera Gitea", e);
        }
    }


    public List<GiteaUserDTO> getRepoCollaborators(String orgName, String repoName) {
        String url = "api/v1/repos/" + orgName + "/" + repoName + "/collaborators";
        try {
            return giteaClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<GiteaUserDTO>>() {
                    });
        } catch (RestClientException e) {
            log.error("Błąd podczas pobierania współpracowników dla repozytoriów: {}/{}", orgName, repoName, e);
            throw new GiteaIntegrationException(e);
        }
    }
}