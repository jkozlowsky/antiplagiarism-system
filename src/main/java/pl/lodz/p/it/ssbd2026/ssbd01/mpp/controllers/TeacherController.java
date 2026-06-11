package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2026.ssbd01.config.openApi.ApiSecuredResponses;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.TeacherSearchDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.TeacherService;

import java.util.List;

@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
@ApiSecuredResponses
public class TeacherController {

    private final TeacherService teacherService;

    @Operation(summary = "Wyszukaj prowadzących", description = "Zwraca listę aktywnych prowadzących na podstawie dopasowania loginu lub adresu e-mail.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Zwraca listę dopasowanych prowadzących")
    })
    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER, T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    public ResponseEntity<List<TeacherSearchDTO>> searchTeachers(@RequestParam String query) {
        return ResponseEntity.ok(teacherService.searchTeachers(query));
    }
}