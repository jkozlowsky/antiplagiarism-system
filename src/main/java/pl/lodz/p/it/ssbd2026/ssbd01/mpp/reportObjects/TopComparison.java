package pl.lodz.p.it.ssbd2026.ssbd01.mpp.reportObjects;

public record TopComparison(
        String firstSubmission,
        String secondSubmission,
        Similarities similarities
) {}

