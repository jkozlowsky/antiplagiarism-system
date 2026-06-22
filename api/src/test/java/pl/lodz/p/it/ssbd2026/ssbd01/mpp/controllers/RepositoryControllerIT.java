package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.specification.FilterableRequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.StudentRepositoryDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.repository.RepositoryWithStudentDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.subject.SubjectStudentStatsDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.RepositoryService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RepositoryControllerIT extends BaseTestSetup {

    @Autowired
    private JwtAuthService jwtAuthService;

    @Autowired
    private RequestFunctions requestFunctions;

    @MockitoBean
    private RepositoryService repositoryService;

    private Headers teacherHeaders;
    private Headers studentHeaders;
    private Headers emptyHeaders;
    private final String studentLogin = "test_student";

    @BeforeEach
    public void setupDataAndAuth() {
        String teacherToken = jwtAuthService.generateToken("test_teacher", List.of(AccountConsts.ROLE_TEACHER), false);
        teacherHeaders = new Headers(new Header("Authorization", "Bearer " + teacherToken));

        String studentToken = jwtAuthService.generateToken(studentLogin, List.of(AccountConsts.ROLE_STUDENT), false);
        studentHeaders = new Headers(new Header("Authorization", "Bearer " + studentToken));

        emptyHeaders = new Headers();
    }

    @Test
    @DisplayName("Pomyślne pobranie repozytoriów dla przedmiotu przez nauczyciela")
    public void shouldGetRepositoriesSuccessfully() {
        UUID subjectId = UUID.randomUUID();
        RepositoryWithStudentDTO dto = new RepositoryWithStudentDTO("Repo1", 1, List.of());
        when(repositoryService.getRepositories(subjectId)).thenReturn(List.of(dto));

        requestFunctions.sendGetRequest(
                200,
                "/api/repositories/subject/" + subjectId,
                teacherHeaders
        );

        verify(repositoryService).getRepositories(subjectId);
    }

    @Test
    @DisplayName("Pomyślne pobranie kont studentów dla przedmiotu przez nauczyciela")
    public void shouldGetUserAccountsSuccessfully() {
        String subjectName = "Matematyka";
        SubjectStudentStatsDTO dto = new SubjectStudentStatsDTO();
        when(repositoryService.getUserAccounts(subjectName)).thenReturn(List.of(dto));

        requestFunctions.sendGetRequest(
                200,
                "/api/repositories/subject/" + subjectName + "/users",
                teacherHeaders
        );

        verify(repositoryService).getUserAccounts(subjectName);
    }

    @Test
    @DisplayName("Pomyślne pobranie własnych repozytoriów przez studenta")
    public void shouldGetMyRepositoriesSuccessfully() {
        StudentRepositoryDTO dto = new StudentRepositoryDTO(UUID.randomUUID(), "Repo1", "Matematyka", 0, 5);
        when(repositoryService.getStudentRepositories(studentLogin)).thenReturn(List.of(dto));

        requestFunctions.sendGetRequest(
                200,
                "/api/repositories/my-repositories",
                studentHeaders
        );

        verify(repositoryService).getStudentRepositories(studentLogin);
    }

    @Test
    @DisplayName("Odrzucenie pobrania repozytoriów przez studenta (brak uprawnień)")
    public void shouldFailToGetRepositoriesWithStudentRole() {
        UUID subjectId = UUID.randomUUID();
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");

        requestFunctions.sendGetRequest(
                403,
                "/api/repositories/subject/" + subjectId,
                studentHeaders
        );
    }

    @Test
    @DisplayName("Odrzucenie pobrania własnych repozytoriów przez nauczyciela (brak uprawnień)")
    public void shouldFailToGetMyRepositoriesWithTeacherRole() {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");

        requestFunctions.sendGetRequest(
                403,
                "/api/repositories/my-repositories",
                teacherHeaders
        );
    }

    @Test
    @DisplayName("Odrzucenie żądania bez dostarczenia tokena (unauthenticated)")
    public void shouldFailWithoutToken() {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");

        requestFunctions.sendGetRequest(
                401,
                "/api/repositories/my-repositories",
                emptyHeaders
        );
    }
}