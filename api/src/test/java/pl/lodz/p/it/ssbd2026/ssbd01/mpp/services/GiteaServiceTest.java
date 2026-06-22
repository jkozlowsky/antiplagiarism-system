package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.GiteaRepoDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.GiteaUserDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.GiteaIntegrationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GiteaServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient giteaClient;

    private GiteaService giteaService;

    @BeforeEach
    void setUp() {
        giteaService = new GiteaService(giteaClient);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldGetRepositoriesFromOrganization_Success() {
        List<GiteaRepoDTO> expectedRepos = List.of(
                new GiteaRepoDTO("repo1", "http://clone1", false),
                new GiteaRepoDTO("repo2", "http://clone2", false)
        );

        when(giteaClient.get()
                .uri(anyString())
                .retrieve()
                .body(any(ParameterizedTypeReference.class)))
                .thenReturn(expectedRepos);

        List<GiteaRepoDTO> actualRepos = giteaService.getRepositoriesFromOrganization("testOrg");

        assertEquals(expectedRepos, actualRepos);
        assertEquals(2, actualRepos.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldGetRepositoriesFromOrganization_ReturnEmptyOnNotFound() {
        when(giteaClient.get()
                .uri(anyString())
                .retrieve()
                .body(any(ParameterizedTypeReference.class)))
                .thenThrow(HttpClientErrorException.NotFound.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        List<GiteaRepoDTO> actualRepos = giteaService.getRepositoriesFromOrganization("nonExistingOrg");

        assertTrue(actualRepos.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldGetRepositoriesFromOrganization_ThrowGiteaIntegrationExceptionOnOtherErrors() {
        when(giteaClient.get()
                .uri(anyString())
                .retrieve()
                .body(any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("Connection error"));

        assertThrows(GiteaIntegrationException.class, () ->
                giteaService.getRepositoriesFromOrganization("testOrg")
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldGetRepoCollaborators_Success() {
        GiteaUserDTO mockUser = mock(GiteaUserDTO.class);
        List<GiteaUserDTO> expectedUsers = List.of(mockUser);

        when(giteaClient.get()
                .uri(anyString())
                .retrieve()
                .body(any(ParameterizedTypeReference.class)))
                .thenReturn(expectedUsers);

        List<GiteaUserDTO> actualUsers = giteaService.getRepoCollaborators("testOrg", "testRepo");

        assertEquals(expectedUsers, actualUsers);
        assertEquals(1, actualUsers.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldGetRepoCollaborators_ReturnEmptyOnNotFound() {
        when(giteaClient.get()
                .uri(anyString())
                .retrieve()
                .body(any(ParameterizedTypeReference.class)))
                .thenThrow(HttpClientErrorException.NotFound.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        List<GiteaUserDTO> actualUsers = giteaService.getRepoCollaborators("testOrg", "nonExistingRepo");

        assertTrue(actualUsers.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldGetRepoCollaborators_ThrowGiteaIntegrationExceptionOnOtherErrors() {
        when(giteaClient.get()
                .uri(anyString())
                .retrieve()
                .body(any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("Server error"));

        assertThrows(GiteaIntegrationException.class, () ->
                giteaService.getRepoCollaborators("testOrg", "testRepo")
        );
    }

    @Test
    void shouldDeleteGiteaOrganization_Success() {
        assertDoesNotThrow(() -> giteaService.deleteGiteaOrganization("testOrg"));
    }

    @Test
    void shouldDeleteGiteaOrganization_IgnoreNotFound() {
        when(giteaClient.delete()
                .uri(eq("/api/v1/orgs/{org}"), eq("testOrg"))
                .retrieve()
                .toBodilessEntity())
                .thenThrow(HttpClientErrorException.NotFound.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        assertDoesNotThrow(() -> giteaService.deleteGiteaOrganization("testOrg"));
    }

    @Test
    void shouldDeleteGiteaOrganization_ThrowGiteaIntegrationExceptionOnOtherErrors() {
        when(giteaClient.delete()
                .uri(eq("/api/v1/orgs/{org}"), eq("testOrg"))
                .retrieve()
                .toBodilessEntity())
                .thenThrow(new RestClientException("Timeout"));

        assertThrows(GiteaIntegrationException.class, () ->
                giteaService.deleteGiteaOrganization("testOrg")
        );
    }
}