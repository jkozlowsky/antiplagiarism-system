package pl.lodz.p.it.ssbd2026.ssbd01.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChangeEmailDTO(@NotBlank @Email String email, @NotNull Integer version) {}
