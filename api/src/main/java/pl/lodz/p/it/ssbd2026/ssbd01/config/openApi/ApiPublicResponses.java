package pl.lodz.p.it.ssbd2026.ssbd01.config.openApi;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import pl.lodz.p.it.ssbd2026.ssbd01.rest.handlers.ValidationErrorResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Błąd walidacji wejścia",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Wewnętrzny błąd serwera",
                content = @Content(mediaType = "text/plain", examples = @ExampleObject("Exception: Wystąpił nieoczekiwany błąd serwera"))),
        @ApiResponse(responseCode = "503", description = "Niedostępność usługi bazy danych",
                content = @Content(mediaType = "text/plain", examples = @ExampleObject("Service is temporarily unavailable. Please try again later.")))
})
public @interface ApiPublicResponses {
}
