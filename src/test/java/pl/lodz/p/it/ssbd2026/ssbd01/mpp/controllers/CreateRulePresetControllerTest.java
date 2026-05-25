package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.Headers;
import io.restassured.specification.FilterableRequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.CreateRulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RaportLevelDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.RulePreset;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RaportLevelDictRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RuleRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;

import java.util.List;
import java.util.UUID;

public class CreateRulePresetControllerTest extends BaseTestSetup {

    @Autowired
    private JwtAuthService jwtAuthService;

    @Autowired
    private RequestFunctions requestFunctions;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private RaportLevelDictRepository raportLevelDictRepository;

    @Autowired
    private PlatformTransactionManager mppTransactionManager;

    private Headers teacherHeaders;
    private Headers studentHeaders;
    private UUID subjectId;
    private UUID templateId;

    @BeforeEach
    public void setupData() {
        String mockTeacherUuid = UUID.randomUUID().toString();
        String teacherToken = jwtAuthService.generateToken(mockTeacherUuid, List.of(AccountConsts.ROLE_TEACHER), false);
        teacherHeaders = new Headers(new io.restassured.http.Header("Authorization", "Bearer " + teacherToken));

        String mockStudentUuid = UUID.randomUUID().toString();
        String studentToken = jwtAuthService.generateToken(mockStudentUuid, List.of(AccountConsts.ROLE_STUDENT), false);
        studentHeaders = new Headers(new io.restassured.http.Header("Authorization", "Bearer " + studentToken));

        TransactionTemplate transactionTemplate = new TransactionTemplate(mppTransactionManager);
        transactionTemplate.execute(_ -> {
            Subject subject = new Subject();
            subject.setName("TestowyPrzedmiot");
            subject.setEdition("2026");
            subject.setGiteaURL("http://gitea.com");
            subject.setArchived(false);
            subject.setOrganizationName("TestowyPrzedmiot");

            subjectId = subjectRepository.save(subject).getId();

            RaportLevelDict dict = raportLevelDictRepository.findByName("FULL_VIEW");

            RulePreset template = new RulePreset();
            template.setIsPreset(true);
            template.setStudentTicketCount(5);
            template.setRaportLevel(dict);
            template.setMinimumTokensMatch(20);
            template.setEnableNormalization(false);

            templateId = ruleRepository.save(template).getId();

            return null;
        });
    }

    @Test
    @DisplayName("Powinno pomyślnie utworzyć własny preset przy poprawnych parametrach")
    public void shouldCreateCustomRulePresetSuccessfully() throws Exception {
        CreateRulePresetDTO dto = new CreateRulePresetDTO(15, "FULL_VIEW", null, 20, true);
        String jsonBody = new ObjectMapper().writeValueAsString(dto);

        requestFunctions.sendPostRequest(
                jsonBody,
                200,
                "/api/rules/subject/" + subjectId,
                teacherHeaders
        );
    }

    @Test
    @DisplayName("Powinno pomyślnie utworzyć preset na podstawie szablonu")
    public void shouldCreateRulePresetFromTemplateSuccessfully() throws Exception {
        CreateRulePresetDTO dto = new CreateRulePresetDTO(null, null, templateId, null, null);
        String jsonBody = new ObjectMapper().writeValueAsString(dto);

        requestFunctions.sendPostRequest(
                jsonBody,
                200,
                "/api/rules/subject/" + subjectId,
                teacherHeaders
        );
    }

    @Test
    @DisplayName("Powinno zwrócić kod 404, gdy przedmiot nie istnieje")
    public void shouldReturnWhenSubjectNotFound() throws Exception {
        CreateRulePresetDTO dto = new CreateRulePresetDTO(null, null, templateId, null, null);
        String jsonBody = new ObjectMapper().writeValueAsString(dto);

        requestFunctions.sendPostRequest(
                jsonBody,
                404,
                "/api/rules/subject/" + UUID.randomUUID(),
                teacherHeaders
        );
    }

    @Test
    @DisplayName("Powinno zwrócić kod 404, gdy podany szablon presetu nie istnieje")
    public void shouldReturnWhenTemplateNotFound() throws Exception {
        CreateRulePresetDTO dto = new CreateRulePresetDTO(null, null, UUID.randomUUID(), null, null);
        String jsonBody = new ObjectMapper().writeValueAsString(dto);

        requestFunctions.sendPostRequest(
                jsonBody,
                404,
                "/api/rules/subject/" + subjectId,
                teacherHeaders
        );
    }

    @Test
    @DisplayName("Powinno zwrócić kod 400, gdy nie podano szablonu i brakuje parametrów własnych")
    public void shouldReturnWhenCustomParametersAreMissing() throws Exception {
        CreateRulePresetDTO dto = new CreateRulePresetDTO(null, "FULL_VIEW", null, null, null);
        String jsonBody = new ObjectMapper().writeValueAsString(dto);

        requestFunctions.sendPostRequest(
                jsonBody,
                400,
                "/api/rules/subject/" + subjectId,
                teacherHeaders
        );
    }

    @Test
    @DisplayName("Powinno zwrócić kod 400, gdy minimumTokensMatch ma niedozwoloną wartość graniczną")
    public void shouldReturnWhenMinimumTokensMatchIsInvalid() throws Exception {
        CreateRulePresetDTO dto = new CreateRulePresetDTO(15, "FULL_VIEW", null, 0, true);
        String jsonBody = new ObjectMapper().writeValueAsString(dto);

        requestFunctions.sendPostRequest(
                jsonBody,
                400,
                "/api/rules/subject/" + subjectId,
                teacherHeaders
        );
    }

    @Test
    @DisplayName("Powinno zwrócić kod 400, gdy studentTicketCount ma niedozwoloną wartość graniczną")
    public void shouldReturnWhenStudentTicketCountIsInvalid() throws Exception {
        CreateRulePresetDTO dto = new CreateRulePresetDTO(-1, "FULL_VIEW", null, 20, true);
        String jsonBody = new ObjectMapper().writeValueAsString(dto);

        requestFunctions.sendPostRequest(
                jsonBody,
                400,
                "/api/rules/subject/" + subjectId,
                teacherHeaders
        );
    }

    @Test
    @DisplayName("Powinno zwrócić kod 400 przy błędnym formacie UUID")
    public void shouldReturnWhenUuidFormatIsInvalid() throws Exception {
        CreateRulePresetDTO dto = new CreateRulePresetDTO(15, "FULL_VIEW", null, 20, true);
        String jsonBody = new ObjectMapper().writeValueAsString(dto);

        requestFunctions.sendPostRequest(
                jsonBody,
                400,
                "/api/rules/subject/to-nie-jest-uuid",
                teacherHeaders
        );
    }

    @Test
    @DisplayName("Powinno zwrócić kod 403, gdy użytkownik nie ma roli nauczyciela")
    public void shouldReturnWhenUserIsNotTeacher() throws Exception {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");
        CreateRulePresetDTO dto = new CreateRulePresetDTO(null, null, templateId, null, null);
        String jsonBody = new ObjectMapper().writeValueAsString(dto);

        requestFunctions.sendPostRequest(
                jsonBody,
                403,
                "/api/rules/subject/" + subjectId,
                studentHeaders
        );
    }
}