package pl.lodz.p.it.ssbd2026.ssbd01.dto.converters;

import org.mapstruct.Mapper;
import org.mapstruct.SubclassMapping;
import org.mapstruct.SubclassMappings;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AccessLevelDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.AccessLevel;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Admin;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Student;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Teacher;

@Mapper(componentModel = "spring")
public interface AccessLevelMapper {
    @SubclassMappings({
            @SubclassMapping(target = AccessLevelDTO.AdminDTO.class, source = Admin.class),
            @SubclassMapping(target = AccessLevelDTO.StudentDTO.class, source = Student.class),
            @SubclassMapping(target = AccessLevelDTO.TeacherDTO.class, source = Teacher.class)
    })
    AccessLevelDTO toAccessLevelDTO(AccessLevel accessLevel);
}