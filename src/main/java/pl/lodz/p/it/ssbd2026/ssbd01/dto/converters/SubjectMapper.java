package pl.lodz.p.it.ssbd2026.ssbd01.dto.converters;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.SubjectDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.RulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.projections.SubjectListProjectionDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Subject;

import java.time.LocalDateTime;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface SubjectMapper {

    @Mapping(target = "id", source = "subject.id")
    @Mapping(target = "versionHash", source = "versionHash")
    @Mapping(target = "createdBy", source = "subject.createdBy.login")
    @Mapping(target = "modifiedBy", source = "subject.modifiedBy.login")
    @Mapping(target = "canEdit", source = "canEdit")
    @Mapping(target = "canManageTeachers", source = "canManageTeachers")
    @Mapping(target = "canViewStats", source = "canViewStats")
    @Mapping(target = "templateId", source = "templateId")
    @Mapping(target = "manualRules", source = "manualRules")
    @Mapping(target = "subjectDescription", source = "originalDescription")
    @Mapping(target = "aggregatedAverageSimilarity", source = "subject.subjectStatistics.averageSimilarity")
    @Mapping(target = "nextScan", source = "nextScan")
    SubjectDTO toSubjectDTO(Subject subject,
                            String versionHash,
                            boolean canEdit,
                            boolean canManageTeachers,
                            boolean canViewStats,
                            UUID templateId,
                            RulePresetDTO manualRules,
                            String originalDescription,
                            LocalDateTime nextScan);


    @Mapping(target = "id", source = "subject.id")
    @Mapping(target = "name", source = "subject.name")
    @Mapping(target = "edition", source = "subject.edition")
    @Mapping(target = "archived", source = "subject.archived")
    @Mapping(target = "versionHash", source = "versionHash")
    @Mapping(target = "canEdit", source = "canEdit")
    @Mapping(target = "canManageTeachers", source = "canManageTeachers")
    @Mapping(target = "canViewStats", source = "canViewStats")
    @Mapping(target = "templateId", source = "templateId")
    @Mapping(target = "manualRules", source = "manualRules")
    @Mapping(target = "subjectDescription", source = "originalDescription")
    @Mapping(target = "nextScan", source = "nextScan")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "aggregatedAverageSimilarity", ignore = true)
    SubjectDTO projectionToSubjectDTO(SubjectListProjectionDTO subject,
                                      String versionHash,
                                      boolean canEdit,
                                      boolean canManageTeachers,
                                      boolean canViewStats,
                                      UUID templateId,
                                      RulePresetDTO manualRules,
                                      String originalDescription,
                                      LocalDateTime nextScan);
}