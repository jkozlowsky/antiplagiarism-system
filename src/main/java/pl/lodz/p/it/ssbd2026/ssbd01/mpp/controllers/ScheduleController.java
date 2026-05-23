package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.UpdateScheduleDTO;
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
    public ResponseEntity<Void> updateSchedule(@PathVariable UUID scheduleId, @Valid @RequestBody UpdateScheduleDTO scheduleDto) {
        scheduleService.updateSchedule(scheduleId, scheduleDto);
        return ResponseEntity.noContent().build();
    }

    
    @DeleteMapping("/{scheduleId}")
    public void deleteSchedule(@PathVariable UUID scheduleId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}