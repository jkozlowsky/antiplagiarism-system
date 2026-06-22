package pl.lodz.p.it.ssbd2026.ssbd01.dto.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.TagConsts;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateScheduleDTO {

    @NotNull
    @Future
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime scheduleDateTime;

    @NotBlank
    @Size(max = TagConsts.TAG_LENGTH)
    private String tag;

    @NotBlank
    private String versionHash;
}
