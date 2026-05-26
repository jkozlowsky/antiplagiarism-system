package pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.SubjectConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.validators.ValidSubjectCreation;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@ValidSubjectCreation(groups = OnCreate.class)
public record SubjectDTO(

        @Null(groups = {OnCreate.class, OnUpdate.class})
        @Schema(description = "Identyfikator przedmiotu", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Hash wersji do blokady optymistycznej", example = "hashedVersion123")
        @Null(groups = {OnCreate.class, OnUpdate.class})
        String versionHash,

        @Schema(description = "Nazwa przedmiotu", example = "64 Osoby IAS", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = OnCreate.class)
        @Size(max = SubjectConsts.SUBJECT_NAME_LENGTH, groups = {OnCreate.class, OnUpdate.class})
        String name,

        @Schema(description = "Nazwa organizacji na serwerze Gitea", example = "SSBD_64", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = OnCreate.class)
        @Size(max = SubjectConsts.SUBJECT_NAME_LENGTH, groups = {OnCreate.class, OnUpdate.class})
        String organizationName,

        @Schema(description = "Edycja (rok)", example = "2026", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = OnCreate.class)
        @Size(max = SubjectConsts.SUBJECT_EDITION_LENGTH, groups = {OnCreate.class, OnUpdate.class})
        String edition,

        @Schema(description = "Opcjonalny opis przedmiotu", example = "lasciate ogni speranza voi ch'entrate")
        @Size(max = SubjectConsts.SUBJECT_DESCRIPTION_LENGTH, groups = {OnCreate.class, OnUpdate.class})
        String subjectDescription,

        @Schema(description = "Adres URL do organizacji w Gitea", example = "https://gitea.it.p.lodz.pl/IO_2026", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = OnCreate.class)
        @Size(max = SubjectConsts.GITEA_URL_LENGTH, groups = {OnCreate.class, OnUpdate.class})
        String giteaURL,

        @Schema(description = "Flaga archiwizacji", example = "false")
        @Null(groups = {OnCreate.class, OnUpdate.class})
        Boolean archived,

        @Schema(description = "Data i czas utworzenia przedmiotu", example = "2026-05-25T10:15:30", accessMode = Schema.AccessMode.READ_ONLY)
        @Null(groups = {OnCreate.class, OnUpdate.class})
        LocalDateTime createdAt,

        @Schema(description = "Data i czas ostatniej modyfikacji przedmiotu", example = "2026-05-26T14:20:00", accessMode = Schema.AccessMode.READ_ONLY)
        @Null(groups = {OnCreate.class, OnUpdate.class})
        LocalDateTime updatedAt,

        @Schema(description = "Login użytkownika, który utworzył przedmiot", example = "admin", accessMode = Schema.AccessMode.READ_ONLY)
        @Null(groups = {OnCreate.class, OnUpdate.class})
        String createdBy,

        @Schema(description = "Login użytkownika, który jako ostatni modyfikował przedmiot", example = "admin", accessMode = Schema.AccessMode.READ_ONLY)
        @Null(groups = {OnCreate.class, OnUpdate.class})
        String modifiedBy,

        @Schema(description = "Opcjonalne UUID szablonu reguł", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID templateId,

        @Schema(description = "Ręczne definicje reguł, jeśli nie wybrano szablonu")
        RulePresetDTO manualRules

) {}