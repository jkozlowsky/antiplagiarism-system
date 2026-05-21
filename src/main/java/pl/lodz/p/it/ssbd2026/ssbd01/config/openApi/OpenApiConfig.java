package pl.lodz.p.it.ssbd2026.ssbd01.config.openApi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("SSBD01 API")
                        .version("1.0")
                        .description("API documentation for the SSBD01 project"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ));
    }

    @Bean
    public OpenApiCustomizer globalResponsesCustomizer() {
        return openApi -> openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {

            Schema<?> validationSchema = new Schema<>().$ref("#/components/schemas/ValidationErrorResponse");
            Content content400 = new Content().addMediaType("application/json",
                    new MediaType().schema(validationSchema));

            Content content500 = new Content().addMediaType("text/plain",
                    new MediaType().addExamples("Przykład 500",
                            new Example().value("Exception: Wystąpił nieoczekiwany błąd serwera")));

            Content content503 = new Content().addMediaType("text/plain",
                    new MediaType().addExamples("Przykład 503",
                            new Example().value("could not execute statement; SQL [n/a]; constraint [null]")));

            operation.getResponses().addApiResponse("400", new ApiResponse()
                    .description("Błąd walidacji wejścia")
                    .content(content400));


            operation.getResponses().addApiResponse("500", new ApiResponse()
                    .description("Wewnętrzny błąd serwera")
                    .content(content500));

            operation.getResponses().addApiResponse("503", new ApiResponse()
                    .description("Niedostępność usługi bazy danych")
                    .content(content503));
            boolean isSecured = operation.getSecurity() == null ||
                    operation.getSecurity().stream().noneMatch(java.util.AbstractMap::isEmpty);

            if (isSecured) {
                Map<String, String> example401Map = Map.of(
                        "error", "Unauthorized",
                        "message", "Brak lub niepoprawny token autoryzacyjny."
                );
                Content content401 = new Content().addMediaType("application/json",
                        new MediaType().addExamples("Przykład 401", new Example().value(example401Map)));

                Content content403 = new Content().addMediaType("text/plain",
                        new MediaType().addExamples("Przykład 403",
                                new Example().value("No access: Access Denied")));

                operation.getResponses().addApiResponse("401", new ApiResponse()
                        .description("Brak autoryzacji (nieprawidłowy token JWT)")
                        .content(content401));

                operation.getResponses().addApiResponse("403", new ApiResponse()
                        .description("Odmowa dostępu (brak roli)")
                        .content(content403));
            }

        }));
    }
}