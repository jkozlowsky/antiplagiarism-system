package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.ScheduleMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.schedule.ScheduleDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.schedule.UpdateScheduleDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.ScheduleService;

import java.util.List;
import java.security.Principal;
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
    @Operation(summary = "Pobierz harmonogramy dla przedmiotu", description = "Zwraca listę harmonogramów dla wybranego przedmiotu")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista harmonogramów została pobrana pomyślnie"),
            @ApiResponse(responseCode = "401", description = "Użytkownik nie jest uwierzytelniony"),
            @ApiResponse(responseCode = "403", description = "Użytkownik nie ma uprawnień do tego przedmiotu (nieodpowiednia rola lub brak przypisania do przedmiotu)")
    })
    public List<ScheduleDTO> getSchedules(@PathVariable UUID subjectId) {
        return scheduleMapper.toScheduleDTOList(scheduleService.getSchedules(subjectId));
    }

    
    @PostMapping("/subject/{subjectId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @Operation(summary = "Utwórz harmonogram wykonywania analizy porównawczej w ramach przedmiotu", description = "Zwraca ScheduleDTO z utworzonego obiektu")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Poprawnie utworzono obiekt"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono przedmiotu"),
    })
    public ScheduleDTO createSchedule(@PathVariable UUID subjectId, @RequestBody ScheduleDTO scheduleDto) {
        return scheduleMapper.toScheduleDTO(scheduleService.createSchedule(subjectId, scheduleMapper.toSchedule(scheduleDto)));
    }


    @PutMapping("/{scheduleId}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @Operation(summary = "Edycja harmonogramu", description = "Prowadzący może zmianić date skanowania repozytoriów.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Harmonogram został pomyślnie zaktualizowany"),
            @ApiResponse(responseCode = "400", description = "Wprowadzono nieprawidłowe dane"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień do edycji harmonogramu"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono harmonogramu")
    })
    public ResponseEntity<Void> updateSchedule(@PathVariable UUID scheduleId, @Valid @RequestBody UpdateScheduleDTO scheduleDto, Principal principal) {
        scheduleService.updateSchedule(scheduleId, scheduleDto, principal.getName());
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @Operation(summary = "Usuwanie harmonogramu", description = "Prowadzący posiadający uprawnienia edycji może usunąć istniejący harmonogram.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Harmonogram został pomyślnie usunięty"),
            @ApiResponse(responseCode = "401", description = "Brak uwierzytelnienia", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień do usunięcia harmonogramu (np. rola studenta lub brak przypisania z prawem edycji)", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono harmonogramu", content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<Void> deleteSchedule(@PathVariable UUID scheduleId, Principal principal) {
        scheduleService.deleteSchedule(scheduleId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}