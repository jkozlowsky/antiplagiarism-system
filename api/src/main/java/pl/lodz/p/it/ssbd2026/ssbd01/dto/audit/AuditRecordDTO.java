package pl.lodz.p.it.ssbd2026.ssbd01.dto.audit;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditRecordDTO {
    private String commitId;
    private String author;
    private LocalDateTime commitDate;
    private String changeType;
    private String entityType;
    private String entityId;
    private List<PropertyChangeDTO> changes;
}