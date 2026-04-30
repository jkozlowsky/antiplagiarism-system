package pl.lodz.p.it.ssbd2026.ssbd01.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class AccessLevelDTO {
    private UUID id;
    private Integer version;
    private String accessLevelName;
    private Boolean active;
}