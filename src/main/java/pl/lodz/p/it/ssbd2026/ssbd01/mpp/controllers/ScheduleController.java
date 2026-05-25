package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.ScheduleMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.schedule.ScheduleDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.ScheduleService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
@MethodCallLogged
public class ScheduleController  {
    private final ScheduleService scheduleService;
    private final ScheduleMapper scheduleMapper;

    @GetMapping("/subject/{subjectId}")
    @ResponseStatus(HttpStatus.OK)
    public List<ScheduleDTO> getSchedules(@PathVariable UUID subjectId) {
        return scheduleMapper.toScheduleDTOList(scheduleService.getSchedules(subjectId));
    }

    
    @PostMapping("/subject/{subjectId}")
    public void createSchedule(@PathVariable UUID subjectId, @RequestBody Object scheduleDto) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    
    @PutMapping("/{scheduleId}")
    public void updateSchedule(@PathVariable UUID scheduleId, @RequestBody Object scheduleDto) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    
    @DeleteMapping("/{scheduleId}")
    public void deleteSchedule(@PathVariable UUID scheduleId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}