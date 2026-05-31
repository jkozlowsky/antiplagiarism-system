package pl.lodz.p.it.ssbd2026.ssbd01.mpp.mpp18;

import io.restassured.http.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.repository.RepositoryWithStudentDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Repository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Student;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.AccessLevelTestRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.RepositoryTestRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.SubjectTestRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GetRepositoriesForSubjectIT extends BaseTestSetup {


    @Autowired
    private RequestFunctions requestFunctions;

    @Autowired
    private SubjectTestRepository subjectTestRepository;

    @Autowired
    private RepositoryTestRepository repositoryTestRepository;

    @Autowired
    private AccessLevelTestRepository accessLevelTestRepository;

    private UUID subjectId;
    private Subject subject;

    private static final String BASE_PATH = "/api/repositories/subject/";

    @BeforeEach
    public void setup() {
        subject = subjectTestRepository.findAll().getFirst();
        subjectId = subject.getId();

        List<Student> students = accessLevelTestRepository.findAll().stream()
                .filter(accessLevel -> accessLevel instanceof Student)
                .map(accessLevel -> (Student) accessLevel)
                .limit(2)
                .toList();

        Repository repository = new Repository();
        repository.setRepositoryName("name");
        repository.setSubject(subject);
        repository.setTicketCount(0);

        repository.getStudentList().addAll(students);

        repositoryTestRepository.save(repository);
    }

    @Test
    void getRepositories_asTeacher_Success() throws JsonProcessingException {
        String url = BASE_PATH + subjectId;

        String responseBody = requestFunctions.sendGetRequest(
                200,
                url,
                new Headers()
        );

        assertNotNull(responseBody);

        ObjectMapper objectMapper = new ObjectMapper();
        List<RepositoryWithStudentDTO> dtos = objectMapper.readValue(responseBody, new TypeReference<List<RepositoryWithStudentDTO>>() {
        });
        assertNotNull(dtos.getFirst().students().getFirst());
        assertEquals(2, dtos.getFirst().students().size());
    }
}