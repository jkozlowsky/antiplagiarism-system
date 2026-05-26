package pl.lodz.p.it.ssbd2026.ssbd01.dto.converters;

import org.mapstruct.Mapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.schedule.ScheduleDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Schedule;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {
    ScheduleDTO toScheduleDTO(Schedule schedule);
    List<ScheduleDTO> toScheduleDTOList(List<Schedule> scheduleList);
}
