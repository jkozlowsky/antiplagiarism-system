package pl.lodz.p.it.ssbd2026.ssbd01.mpp.mpp16;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.http.Headers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.schedule.UpdateScheduleDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.CommonFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;

import java.time.LocalDateTime;
import java.util.UUID;

public class NEG01_updateSchedule extends BaseTestSetup {
    @Autowired
    private RequestFunctions requestFunctions;

    private final CommonFunctions commonFunctions = new CommonFunctions();

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void NEG01_updateSchedule() throws Exception {
        UpdateScheduleDTO dto = new UpdateScheduleDTO();
        dto.setScheduleDateTime(LocalDateTime.now().minusDays(1));
        dto.setTag(commonFunctions.generateRandomStringOfLength(17));

        requestFunctions.sendPutRequest(
                objectMapper.writeValueAsString(dto),
                400,
                "/api/schedules/" + UUID.randomUUID(),
                new Headers()
        );
    }
}