package pl.lodz.p.it.ssbd2026.ssbd01.dto.converters;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.ReportDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Report;

@Mapper(componentModel = "spring")
public interface ReportMapper {

    @Mapping(source = "averageSimilarity", target = "average_similarity")
    @Mapping(source = "createdAt", target = "created_at")
    @Mapping(source = "createdBy.login", target = "created_by")
    @Mapping(source = "subject.name", target = "subject_name")
    @Mapping(source = "scannedRepositories", target = "scanned_repositories")
    ReportDTO toDto(Report report);

}