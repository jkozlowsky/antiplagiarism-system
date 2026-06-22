package pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp;

public record SubjectConfigDTO(
        String organizationName,
        Integer minimumTokensMatch,
        Boolean enableNormalization
) {
}
