package pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.SubjectConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnUpdate;


import java.time.LocalDateTime;
import java.util.UUID;


public record SubjectDTO(

    @Null(groups = {OnCreate.class, OnUpdate.class})
    UUID id,

    @Null(groups = {OnCreate.class, OnUpdate.class})
    String versionHash,

    @NotBlank(groups = OnCreate.class)
    @Size(max = SubjectConsts.SUBJECT_NAME_LENGTH, groups = {OnCreate.class, OnUpdate.class})
    String name,

    @NotBlank(groups = OnCreate.class)
    @Size(max = SubjectConsts.SUBJECT_EDITION_LENGTH, groups = {OnCreate.class, OnUpdate.class})
    String edition,

    @Size(max = SubjectConsts.SUBJECT_DESCRIPTION_LENGTH, groups = {OnCreate.class, OnUpdate.class})
    String subjectDescription,

    @NotBlank(groups = OnCreate.class)
    @Size(max = SubjectConsts.GITEA_URL_LENGTH, groups = {OnCreate.class, OnUpdate.class})
    String giteaURL,

    @Null(groups = {OnCreate.class, OnUpdate.class})
    Boolean archived,

    @Null(groups = {OnCreate.class, OnUpdate.class})
    LocalDateTime createdAt,

    @Null(groups = {OnCreate.class, OnUpdate.class})
    LocalDateTime updatedAt,

    @Null(groups = {OnCreate.class, OnUpdate.class})
    String createdBy,

    @Null(groups = {OnCreate.class, OnUpdate.class})
    String modifiedBy

)
{}
