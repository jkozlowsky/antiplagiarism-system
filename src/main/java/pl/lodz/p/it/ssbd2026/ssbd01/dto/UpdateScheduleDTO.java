package pl.lodz.p.it.ssbd2026.ssbd01.dto;

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
    private LocalDateTime scheduleDateTime;

    @NotBlank
    @Size(max = TagConsts.TAG_LENGTH)
    private String tag;
}
