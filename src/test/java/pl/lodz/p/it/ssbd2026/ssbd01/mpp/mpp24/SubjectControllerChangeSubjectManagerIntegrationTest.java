package pl.lodz.p.it.ssbd2026.ssbd01.mpp.mpp24;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.specification.FilterableRequestSpecification;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.subject.ChangeManagerRequestDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel.AccessLevelDictNotFoundException;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SubjectControllerChangeSubjectManagerIntegrationTest extends BaseTestSetup {
    @Autowired
    private RequestFunctions requestFunctions;
    @Autowired
    private JwtAuthService jwtAuthService;
    @Autowired
    private ScheduleTestRepository scheduleTestRepository;
    @Autowired
    private SubjectAssignmentTestRepository subjectAssignmentTestRepository;
    @Autowired
    private TeacherTestRepository teacherTestRepository;
    @Autowired
    private AccountTestRepository accountTestRepository;
    @Autowired
    private LanguageDictTestRepository languageDictTestRepository;
    @Autowired
    @Qualifier("adminTransactionManager")
    private PlatformTransactionManager transactionManager;
    @Autowired
    private AccessLevelDictTestRepository accessLevelDictTestRepository;

    @Autowired
    private VersionHashService versionHashService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Headers adminHeaders;
    private Headers teacherHeaders;

    private UUID subjectId;
    private UUID newManagerAssignmentId;
    private UUID newTeacherAccountId;
    private static final String oldManagerLogin = "test_teacher";
    private static final String newManagerLogin = "mkarbowańczyk";

    private Account createAccount(String login, LanguageDict lang, AccessLevel accessLevel) {
        Account newAccount = new Account();
        newAccount.setLogin(login);
        newAccount.setEmail(login + "@test.pl");
        newAccount.setPasswordHash("$2a$10$hashhash");
        newAccount.setName("Nowy");
        newAccount.setSurname("Kierownik");
        newAccount.setLanguageVersion(lang);
        newAccount.getAccessLevelList().add(accessLevel);
        accessLevel.setAccount(newAccount);
        newAccount.setIsBlocked(false);
        newAccount.setIsVerified(true);
        newAccount.setFailedAttempts(0);
        return newAccount;
    }

    @BeforeAll
    public void setUpChangeSubjectManager() {
        String adminToken = jwtAuthService.generateToken("test_admin", List.of(AccountConsts.ROLE_ADMIN), false);
        adminHeaders = new Headers(new Header("Authorization", "Bearer " + adminToken));

        String teacherToken = jwtAuthService.generateToken("test_teacher", List.of(AccountConsts.ROLE_TEACHER), false);
        teacherHeaders = new Headers(new Header("Authorization", "Bearer " + teacherToken));

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.execute(_ -> {
            SubjectAssignment ownerAssignment = subjectAssignmentTestRepository
                    .findByTeacherAccountLogin(oldManagerLogin).getFirst();
            subjectId = ownerAssignment.getSubject().getId();

            LanguageDict lang = languageDictTestRepository.findAll().getFirst();

            AccessLevelDict teacherAccessLevelDict = accessLevelDictTestRepository.findAll().stream()
                    .filter(accessLevelDict -> accessLevelDict.getAccessLevelName().equals("TEACHER"))
                    .findFirst().orElseThrow(AccessLevelDictNotFoundException::new);

            Teacher newTeacher = new Teacher();
            newTeacher.setActive(true);
            newTeacher.setAccessLevelDict(teacherAccessLevelDict);
            teacherTestRepository.saveAndFlush(newTeacher);

            Account newAccount = createAccount(newManagerLogin, lang, newTeacher);
            accountTestRepository.saveAndFlush(newAccount);
            newTeacherAccountId = newAccount.getId();

            SubjectAssignment newAssignment = new SubjectAssignment();
            newAssignment.setSubject(ownerAssignment.getSubject());
            newAssignment.setTeacher(newTeacher);
            newAssignment.setSubjectRole(TeacherSubjectRole.VIEWER);
            subjectAssignmentTestRepository.save(newAssignment);
            newManagerAssignmentId = newAssignment.getId();
            return null;
        });
    }

    @AfterAll
    public void tearDown() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.execute(_ -> {
            if (newManagerAssignmentId != null) {
                subjectAssignmentTestRepository.deleteById(newManagerAssignmentId);
            }
            teacherTestRepository.findByAccount_Login(newManagerLogin).ifPresent(t -> teacherTestRepository.delete(t));
            accountTestRepository.findByLogin(newManagerLogin).ifPresent(a -> accountTestRepository.delete(a));
            subjectAssignmentTestRepository.findByTeacherAccountLogin(oldManagerLogin).stream()
                    .filter(sa -> sa.getSubject().getId().equals(subjectId))
                    .findFirst()
                    .ifPresent(sa -> {
                        sa.setSubjectRole(TeacherSubjectRole.OWNER);
                        subjectAssignmentTestRepository.save(sa);
                    });
            return null;
        });
    }

    private String getVersionHash() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        return txTemplate.execute(_ -> {
            Subject subject = subjectAssignmentTestRepository.findByTeacherAccountLogin(oldManagerLogin).getFirst().getSubject();
            return versionHashService.hashObjectVersion(subject.getId(), subject.getVersion());
        });
    }

    private Headers withIfMatch(Headers baseHeaders, String hash) {
        List<Header> headerList = new ArrayList<>(baseHeaders.asList());
        headerList.add(new Header("If-Match", hash));
        return new Headers(headerList);
    }


    @Test
    public void POS01_ChangeSubjectManager_Successfully() throws JsonProcessingException {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");
        ChangeManagerRequestDTO dto = new ChangeManagerRequestDTO(newManagerLogin);
        requestFunctions.sendPatchRequest(
                objectMapper.writeValueAsString(dto),
                HttpStatus.NO_CONTENT.value(),
                "/api/subjects/" + subjectId + "/manager",
                withIfMatch(adminHeaders, getVersionHash())
        );
    }

    @Test
    public void NEG01_ChangeSubjectManager_Unauthenticated() throws JsonProcessingException {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");
        ChangeManagerRequestDTO dto = new ChangeManagerRequestDTO(newManagerLogin);
        requestFunctions.sendPatchRequest(
                objectMapper.writeValueAsString(dto),
                HttpStatus.UNAUTHORIZED.value(),
                "/api/subjects/" + subjectId + "/manager",
                withIfMatch(new Headers(), getVersionHash())
        );
    }

    @Test
    public void NEG02_ChangeSubjectManager_ForbiddenForTeacher() throws JsonProcessingException {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");
        ChangeManagerRequestDTO dto = new ChangeManagerRequestDTO(newManagerLogin);
        requestFunctions.sendPatchRequest(
                objectMapper.writeValueAsString(dto),
                HttpStatus.FORBIDDEN.value(),
                "/api/subjects/" + subjectId + "/manager",
                withIfMatch(teacherHeaders, getVersionHash())
        );
    }

    @Test
    public void NEG03_ChangeSubjectManager_ForbiddenForNonExistingTeacher() throws JsonProcessingException {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");
        ChangeManagerRequestDTO dto = new ChangeManagerRequestDTO("nonexistingteacher");
        requestFunctions.sendPatchRequest(
                objectMapper.writeValueAsString(dto),
                HttpStatus.CONFLICT.value(),
                "/api/subjects/" + subjectId + "/manager",
                withIfMatch(adminHeaders, getVersionHash())
        );
    }

    @Test
    public void NEG04_ChangeSubjectManager_ForbiddenForNonExistingSubject() throws JsonProcessingException {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");
        ChangeManagerRequestDTO dto = new ChangeManagerRequestDTO(newManagerLogin);
        requestFunctions.sendPatchRequest(
                objectMapper.writeValueAsString(dto),
                HttpStatus.BAD_REQUEST.value(),
                "/api/subjects/" + subjectId + "1/manager",
                withIfMatch(adminHeaders, getVersionHash())
        );
    }
}