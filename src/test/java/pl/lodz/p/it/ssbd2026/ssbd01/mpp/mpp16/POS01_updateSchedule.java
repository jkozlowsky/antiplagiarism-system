package pl.lodz.p.it.ssbd2026.ssbd01.mpp.mpp16;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.http.Headers;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.schedule.UpdateScheduleDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.RuleRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.ScheduleRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.repositories.SubjectAssignmentRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;

import java.time.LocalDateTime;
import java.util.UUID;

// TODO: dokończyć na danych prawidłowych - przedmiot i prowadzący z możliwością edycji
public class POS01_updateSchedule extends BaseTestSetup {
    @Autowired
    private RequestFunctions requestFunctions;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private RuleRepository ruleRepository;
    @Autowired
    private SubjectAssignmentRepository assignmentRepository;
    @Autowired
    private EntityManager em;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private UUID scheduleId;
    private UUID assignmentId;

    @BeforeEach
    void setup() {

    }

    @Test
    @Disabled
    void POS01_updateSchedule() throws Exception {
        UpdateScheduleDTO dto = new UpdateScheduleDTO();
        dto.setScheduleDateTime(LocalDateTime.now().plusDays(5));
        dto.setTag("nowyTag");

        requestFunctions.sendPutRequest(
                objectMapper.writeValueAsString(dto),
                204,
                "/api/schedules/" + scheduleId,
                new Headers()
        );
    }
}