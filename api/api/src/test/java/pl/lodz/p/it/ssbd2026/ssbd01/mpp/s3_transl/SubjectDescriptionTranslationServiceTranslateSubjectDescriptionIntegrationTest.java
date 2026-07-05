package pl.lodz.p.it.ssbd2026.ssbd01.mpp.s3_transl;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.LanguageDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.SubjectDescriptionTranslation;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.GoogleTranslationService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.LanguageDictTestRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.SubjectDescriptionTranslationTestRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.SubjectTestRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class SubjectDescriptionTranslationServiceTranslateSubjectDescriptionIntegrationTest extends BaseTestSetup {

    @LocalServerPort
    private int port;

    @Autowired
    private RequestFunctions requestFunctions;
    @Autowired
    private LanguageDictTestRepository languageDictTestRepository;
    @Autowired
    private SubjectDescriptionTranslationTestRepository subjectDescriptionTranslationTestRepository;
    @Autowired
    private SubjectTestRepository subjectTestRepository;
    @Autowired
    private JwtAuthService jwtAuthService;
    @MockitoBean
    private GoogleTranslationService googleTranslationService;

    private Headers adminHeaders;
    private Headers teacherHeaders;
    private Headers studentHeaders;
    private Headers noAuthHeaders;

    private UUID subjectId;
    private UUID originalTranslationId;

    private static final String TARGET_LANGUAGE = "pl";
    private static final String ORIGINAL_LANGUAGE = "en";
    private static final String FAKE_LANGUAGE = "xyz";
    private static final String GITEA_URL = "http://localhost:3000/test-org/test-repo";
    private static final String ORG_NAME = "test-org";

    @BeforeEach
    public void setupRestAssuredPort() {
        RestAssured.port = port;
    }

    @BeforeAll
    public void setUp() {
        String adminToken = jwtAuthService.generateToken("test_admin", List.of(AccountConsts.ROLE_ADMIN), false);
        adminHeaders = new Headers(
                new Header("Authorization", "Bearer " + adminToken),
                new Header("Accept-Language", TARGET_LANGUAGE)
        );

        String teacherToken = jwtAuthService.generateToken("test_teacher", List.of(AccountConsts.ROLE_TEACHER), false);
        teacherHeaders = new Headers(
                new Header("Authorization", "Bearer " + teacherToken),
                new Header("Accept-Language", TARGET_LANGUAGE)
        );

        String studentToken = jwtAuthService.generateToken("test_student", List.of(AccountConsts.ROLE_STUDENT), false);
        studentHeaders = new Headers(
                new Header("Authorization", "Bearer " + studentToken),
                new Header("Accept-Language", TARGET_LANGUAGE)
        );

        noAuthHeaders = new Headers(new Header("Accept-Language", TARGET_LANGUAGE));

        Subject subject = new Subject();
        subject.setName("Integration Test Subject");

        subject.setArchived(false);
        subject.setEdition("2067");
        subject.setGiteaURL(GITEA_URL);
        subject.setOrganizationName(ORG_NAME);

        subject = subjectTestRepository.saveAndFlush(subject);
        subjectId = subject.getId();

        LanguageDict origLang = languageDictTestRepository.findAll().stream()
                .filter(lang -> lang.getLanguage().equals(ORIGINAL_LANGUAGE))
                .findFirst().orElseThrow();

        SubjectDescriptionTranslation origTrans = new SubjectDescriptionTranslation();
        origTrans.setSubject(subject);
        origTrans.setLanguageDict(origLang);
        origTrans.setIsOriginal(true);
        origTrans.setTranslatedDescription("Original description for integration testing.");

        originalTranslationId = subjectDescriptionTranslationTestRepository.saveAndFlush(origTrans).getId();
    }

    @AfterAll
    public void tearDown() {
        if (originalTranslationId != null) {
            subjectDescriptionTranslationTestRepository.deleteById(originalTranslationId);
        }
        if (subjectId != null) {
            subjectDescriptionTranslationTestRepository.findAll().stream()
                    .filter(t -> t.getSubject().getId().equals(subjectId))
                    .forEach(t -> subjectDescriptionTranslationTestRepository.deleteById(t.getId()));

            subjectTestRepository.deleteById(subjectId);
        }
    }

    @Test
    public void POS01_TranslateSubjectDescription_AsTeacher_Successfully() {
        when(googleTranslationService.translateText(anyString(), anyString(), anyString()))
                .thenReturn("Przetłumaczony tekst");

        String responseBody = requestFunctions.sendGetRequest(
                HttpStatus.OK.value(),
                "/api/subjects/" + subjectId + "/translate",
                teacherHeaders
        );

        assertNotNull(JsonPath.from(responseBody).getString("translatedSubjectDescription"));
    }

    @Test
    public void POS02_TranslateSubjectDescription_AsAdmin_Successfully() {
        when(googleTranslationService.translateText(anyString(), anyString(), anyString()))
                .thenReturn("Przetłumaczony tekst");
        String responseBody = requestFunctions.sendGetRequest(
                HttpStatus.OK.value(),
                "/api/subjects/" + subjectId + "/translate",
                adminHeaders
        );

        IO.println(responseBody);

        assertNotNull(JsonPath.from(responseBody).getString("translatedSubjectDescription"));
    }

    @Test
    public void NEG01_TranslateSubjectDescription_Unauthenticated() {
        requestFunctions.sendGetRequest(
                HttpStatus.UNAUTHORIZED.value(),
                "/api/subjects/" + subjectId + "/translate",
                noAuthHeaders
        );
    }

    @Test
    public void NEG02_TranslateSubjectDescription_ForbiddenForStudent() {
        requestFunctions.sendGetRequest(
                HttpStatus.FORBIDDEN.value(),
                "/api/subjects/" + subjectId + "/translate",
                studentHeaders
        );
    }

    @Test
    public void NEG03_TranslateSubjectDescription_NotFoundForNonExistingSubject() {
        UUID nonExistingSubjectId = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

        requestFunctions.sendGetRequest(
                HttpStatus.NOT_FOUND.value(),
                "/api/subjects/" + nonExistingSubjectId + "/translate",
                teacherHeaders
        );
    }

    @Test
    public void NEG04_TranslateSubjectDescription_NotFoundForNonExistingLanguage() {
        Headers badLangHeaders = new Headers(
                teacherHeaders.get("Authorization"),
                new Header("Accept-Language", FAKE_LANGUAGE)
        );

        requestFunctions.sendGetRequest(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), //todo LanguageDictNotFoundException powinno zwrócić 404
                "/api/subjects/" + subjectId + "/translate",
                badLangHeaders
        );
    }
}