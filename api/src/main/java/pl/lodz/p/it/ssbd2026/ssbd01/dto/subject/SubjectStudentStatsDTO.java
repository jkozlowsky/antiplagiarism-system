package pl.lodz.p.it.ssbd2026.ssbd01.dto.subject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectStudentStatsDTO {
    private UUID studentId;
    private String firstName;
    private String lastName;
    private UUID repositoryId;
    private String repositoryName;
    private Double plagiarismRatio;
}
