package pl.lodz.p.it.ssbd2026.ssbd01.mpp.mpp03;

import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GetAllReportsForTeacherIT extends BaseTestSetup {

    @Autowired
    private RequestFunctions requestFunctions;

    @Autowired
    private SubjectTestRepository subjectTestRepository;

    @Autowired
    private ReportTestRepository reportTestRepository;

    @Autowired
    private AccessLevelDictTestRepository accessLevelDictTestRepository;

    @Autowired
    private AccountTestRepository accountTestRepository;

    @Autowired
    private TeacherTestRepository teacherTestRepository;

    @Autowired
    private SubjectAssignmentTestRepository subjectAssignmentTestRepository;

    private Subject testSubject;
    private Report testReport;
    private Teacher testTeacher;

    private static final String BASE_PATH = "/api/reports/teacher";

    @BeforeEach
    public void setupData() {
        Account adminAccount = accountTestRepository.findByLogin("admin").orElseGet(() -> {
            Account newAccount = new Account();
            newAccount.setLogin("admin");
            newAccount.setPasswordHash("Password123!");
            newAccount.setEmail("admin@example.com");
            newAccount.setLogin("pl");
            newAccount.setIsVerified(true);
            newAccount.setIsBlocked(false);
            return accountTestRepository.save(newAccount);
        });

        AccessLevelDict teacherDict = accessLevelDictTestRepository.findByAccessLevelName("TEACHER")
                .orElseGet(() -> {
                    AccessLevelDict dict = new AccessLevelDict();
                    dict.setAccessLevelName("TEACHER");
                    return accessLevelDictTestRepository.save(dict);
                });

        testTeacher = new Teacher();
        testTeacher.setAccount(adminAccount);
        testTeacher.setAccessLevelDict(teacherDict);
        testTeacher = teacherTestRepository.save(testTeacher);

        testSubject = new Subject();
        testSubject.setArchived(false);
        testSubject.setEdition("25");
        testSubject.setName("Test Subject");
        testSubject.setOrganizationName("Test Organization");
        testSubject.setGiteaURL("http://localhost:3000/test");
        testSubject = subjectTestRepository.save(testSubject);

        SubjectAssignment assignment = new SubjectAssignment();
        assignment.setSubject(testSubject);
        assignment.setSubjectRole(TeacherSubjectRole.OWNER);
        assignment.setTeacher(testTeacher);
        subjectAssignmentTestRepository.save(assignment);

        testReport = new Report();
        testReport.setSubject(testSubject);
        testReport.setTag("v1.0.0");
        testReport.setAverageSimilarity(45.5);
        testReport.setPathToReport("/path");
        testReport = reportTestRepository.save(testReport);
    }

    @AfterEach
    public void cleanupData() {
        reportTestRepository.deleteAll();
        subjectAssignmentTestRepository.deleteAll();
        subjectTestRepository.deleteAll();
        teacherTestRepository.deleteAll();
    }

    @Test
    public void getAllReportsForTeacher_ShouldReturn200AndPage_WhenTeacherAuthorized() {
        String response = requestFunctions.sendGetRequest(
                200,
                BASE_PATH,
                new Headers()
        );

        JsonPath jsonPath = JsonPath.from(response);

        assertTrue(jsonPath.getInt("content.size()") >= 1);
        assertEquals(testReport.getId().toString(), jsonPath.getString("content[0].id"));
        assertEquals(testReport.getTag(), jsonPath.getString("content[0].tag"));
        assertEquals(testReport.getAverageSimilarity().floatValue(), jsonPath.getFloat("content[0].average_similarity"));
        assertTrue(jsonPath.getInt("totalElements") >= 1);
        assertTrue(jsonPath.getInt("totalPages") >= 1);
        assertEquals(0, jsonPath.getInt("number"));
    }

    @Test
    public void getAllReportsForTeacher_ShouldReturnEmptyPage_WhenTeacherHasNoAssignments() {
        // Delete the assignment so the teacher is no longer assigned to the subject
        subjectAssignmentTestRepository.deleteAll();

        String response = requestFunctions.sendGetRequest(
                200,
                BASE_PATH,
                new Headers()
        );

        JsonPath jsonPath = JsonPath.from(response);

        assertEquals(0, jsonPath.getInt("content.size()"));
        assertEquals(0, jsonPath.getInt("totalElements"));
        assertEquals(0, jsonPath.getInt("totalPages"));
    }

    @Test
    public void getAllReportsForTeacher_ShouldReturn200AndRespectPaginationParams() {
        // Create a second report for the same subject
        Report secondReport = new Report();
        secondReport.setSubject(testSubject);
        secondReport.setTag("v1.1.0");
        secondReport.setAverageSimilarity(12.5);
        secondReport.setPathToReport("/path");
        reportTestRepository.save(secondReport);

        Map<String, String> queryParams = Map.of(
                "page", "0",
                "size", "1"
        );

        String response = requestFunctions.sendGetRequest(
                200,
                BASE_PATH,
                new Headers(),
                queryParams
        );

        JsonPath jsonPath = JsonPath.from(response);

        assertEquals(1, jsonPath.getInt("content.size()"));
        // Assuming there are exactly 2 reports in the DB for this teacher
        assertEquals(2, jsonPath.getInt("totalElements"));
        assertEquals(2, jsonPath.getInt("totalPages"));
        assertEquals(1, jsonPath.getInt("size"));
    }
}