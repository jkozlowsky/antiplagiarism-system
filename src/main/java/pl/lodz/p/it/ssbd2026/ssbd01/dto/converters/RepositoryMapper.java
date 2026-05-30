package pl.lodz.p.it.ssbd2026.ssbd01.dto.converters;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.repository.RepositoryWithStudentDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.repository.StudentDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Repository;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Student;

@Mapper(componentModel = "spring")
public interface RepositoryMapper {

    @Mapping(source = "studentList", target = "students")
    RepositoryWithStudentDTO toDto(Repository repository);

    @Mapping(source = "account.name", target = "name")
    @Mapping(source = "account.surname", target = "surname")
    StudentDTO toStudentDto(Student student);
}
