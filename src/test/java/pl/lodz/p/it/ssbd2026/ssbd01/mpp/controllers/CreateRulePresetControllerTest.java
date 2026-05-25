package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.CreateRulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
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
    private PlatformTransactionManager mppTransactionManager;

    private Headers teacherHeaders;
    private UUID subjectId;

    @BeforeEach
    public void setupData() {
        String teacherToken = jwtAuthService.generateToken("teacher", List.of(AccountConsts.ROLE_TEACHER), false);
        teacherHeaders = new Headers(new io.restassured.http.Header("Authorization", "Bearer " + teacherToken));

        TransactionTemplate transactionTemplate = new TransactionTemplate(mppTransactionManager);
        transactionTemplate.execute(_ -> {
            Subject subject = new Subject();
            subject.setName("TestowyPrzedmiot");
            subject.setEdition("2026");
            subject.setGiteaURL("http://gitea.com");
            subject.setArchived(false);
            subjectId = subjectRepository.save(subject).getId();
            return null;
        });
    }

    @Test
    @DisplayName("Powinno pomyślnie utworzyć preset przez kontroler")
    public void shouldCreateRulePresetSuccessfully() throws Exception {
        CreateRulePresetDTO dto = new CreateRulePresetDTO(15, "FULL_VIEW");
        String jsonBody = new ObjectMapper().writeValueAsString(dto);

        requestFunctions.sendPostRequest(
                jsonBody,
                200,
                "/api/rules/subject/" + subjectId,
                teacherHeaders
        );
    }
}