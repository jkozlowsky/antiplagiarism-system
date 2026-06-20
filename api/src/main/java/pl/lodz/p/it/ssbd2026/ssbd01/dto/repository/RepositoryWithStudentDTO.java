package pl.lodz.p.it.ssbd2026.ssbd01.dto.repository;
import java.util.List;

public record RepositoryWithStudentDTO(
        String repositoryName,
        Integer ticketCount,
        List<StudentDTO> students
) {}