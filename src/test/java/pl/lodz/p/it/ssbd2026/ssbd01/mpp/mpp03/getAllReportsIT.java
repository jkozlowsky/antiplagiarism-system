package pl.lodz.p.it.ssbd2026.ssbd01.mpp.mpp03;

import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Report;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.ReportTestRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.SubjectTestRepository;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class getAllReportsIT extends BaseTestSetup {

    @Autowired
    private SubjectTestRepository subjectTestRepository;

    @Autowired
    private ReportTestRepository reportTestRepository;

    private Subject testSubject;
    private Report testReport;

    private static final String BASE_PATH = "/api/reports/subject/";

    @BeforeEach
    public void setupData() {
        testSubject = new Subject();

        testSubject.setArchived(false);

        testSubject.setName("Test Subject Name");
        testSubject.setEdition("2026/2027");
        testSubject.setOrganizationName("Test Organization");
        testSubject.setGiteaURL("http://localhost:3000/test");

        testSubject = subjectTestRepository.save(testSubject);

        testReport = new Report();
        testReport.setSubject(testSubject);
        testReport.setTag("v1.0.0");
        testReport.setAverageSimilarity(45.5);
        testReport.setPathToReport("/report");


        testReport = reportTestRepository.save(testReport);
    }

    @AfterEach
    public void cleanupData() {
        reportTestRepository.deleteAll();
        subjectTestRepository.deleteAll();
    }

    @Autowired
    private RequestFunctions requestFunctions;

    @Test
    public void getAllReports_ShouldReturn200AndPage_WhenTeacherAuthorized() {
        String response = requestFunctions.sendGetRequest(
                200,
                BASE_PATH + testSubject.getId(),
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
    public void getAllReports_ShouldReturnEmptyPage_WhenSubjectHasNoReports() {
        Subject emptySubject = new Subject();
        emptySubject.setArchived(false);
        emptySubject.setEdition("25");
        emptySubject.setOrganizationName("Test Organization");
        emptySubject.setName("name");
        emptySubject.setGiteaURL("http://localhost:3000/test");
        emptySubject = subjectTestRepository.save(emptySubject);

        String response = requestFunctions.sendGetRequest(
                200,
                BASE_PATH + emptySubject.getId(),
                new Headers()
        );

        JsonPath jsonPath = JsonPath.from(response);

        assertEquals(0, jsonPath.getInt("content.size()"));
        assertEquals(0, jsonPath.getInt("totalElements"));
        assertEquals(0, jsonPath.getInt("totalPages"));
    }

    @Test
    public void getAllReports_ShouldReturn200AndRespectPaginationParams() {
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
                BASE_PATH + testSubject.getId(),
                new Headers(),
                queryParams
        );

        JsonPath jsonPath = JsonPath.from(response);

        assertEquals(1, jsonPath.getInt("content.size()"));
        assertEquals(2, jsonPath.getInt("totalElements"));
        assertEquals(2, jsonPath.getInt("totalPages"));
        assertEquals(1, jsonPath.getInt("size"));
    }
}
