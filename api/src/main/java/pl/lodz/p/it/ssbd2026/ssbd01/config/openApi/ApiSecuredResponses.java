package pl.lodz.p.it.ssbd2026.ssbd01.config.openApi;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiPublicResponses
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Brak autoryzacji (nieprawidłowy token JWT)",
                content = @Content(mediaType = "application/json", examples = @ExampleObject("{\n  \"error\": \"Unauthorized\",\n  \"message\": \"Brak lub niepoprawny token autoryzacyjny.\"\n}"))),
        @ApiResponse(responseCode = "403", description = "Odmowa dostępu (brak roli)",
                content = @Content(mediaType = "text/plain", examples = @ExampleObject("No access: Access Denied"))),
})
public @interface ApiSecuredResponses {
}



