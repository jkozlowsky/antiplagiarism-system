package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.ScheduleService;

import java.util.UUID;

@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
@MethodCallLogged
public class ScheduleController  {
    private final ScheduleService scheduleService;
    
    @GetMapping("/subject/{subjectId}")
    public void getSchedules(@PathVariable UUID subjectId) {
        throw new UnsupportedOperationException("Not implemented yet");
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