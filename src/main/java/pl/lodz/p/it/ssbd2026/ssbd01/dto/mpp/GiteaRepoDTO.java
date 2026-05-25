package pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp;

public record GiteaRepoDTO(
        String name,
//        String full_name,
        String clone_url,
        Boolean template
) {
}
