package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.File;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitService {

    private final RestClient giteaClient;

    public static void cloneTag(String repoUrl, String tagName, File localPath) {
        try (Git git = Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(localPath)
                .setBranch("refs/tags/" + tagName)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("token", "bda43f3d99674914c59753bb04d306b1426aa54e"))
                .call()) { // TODO: remove git token from here!!!!!!!!!

            System.out.println("Successfully cloned tag: " + tagName);

        } catch (GitAPIException e) {
//            throw new RuntimeException("Failed to clone repository at tag " + tagName, e);
        } catch (JGitInternalException e){
            log.info("Cannot clone repository {} at tag {} because it is already present in the folder", repoUrl, tagName);
        }
    }

    /**
     * Usuwa organizację (przedmiot) z serwera Gitea.
     * * @param organizationName Nazwa organizacji/przedmiotu w Gitea
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
}
