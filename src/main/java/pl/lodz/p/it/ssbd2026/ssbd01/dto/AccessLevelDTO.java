package pl.lodz.p.it.ssbd2026.ssbd01.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AccessLevelDTO {
    private UUID id;
    private Integer version;
    private String accessLevelName;

    public static class AdminDTO extends AccessLevelDTO {}
    public static class StudentDTO extends AccessLevelDTO {}
    public static class TeacherDTO extends AccessLevelDTO {}
}