package pl.lodz.p.it.ssbd2026.ssbd01.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PropertyChangeDTO {
    private String property;
    private String oldValue;
    private String newValue;
}