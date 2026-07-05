package pl.lodz.p.it.ssbd2026.mcpserver.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import pl.lodz.p.it.ssbd2026.mcpserver.dto.ComparisonDTO;
import pl.lodz.p.it.ssbd2026.mcpserver.dto.ReportDetailsDTO;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GroqSummaryService {

    private static final int MAX_COMPARISONS_IN_PROMPT = 15;

    private final RestClient restClient;
    private final String model;
    private final int maxTokens;
    private final boolean apiKeyConfigured;

    public GroqSummaryService(
            @Value("${groq.api.url}") String apiUrl,
            @Value("${groq.api.key:}") String apiKey,
            @Value("${groq.model}") String model,
            @Value("${groq.max-tokens:1024}") int maxTokens,
            @Value("${groq.timeout.connect-ms:5000}") int connectTimeoutMs,
            @Value("${groq.timeout.read-ms:60000}") int readTimeoutMs
    ) {
        this.model = model;
        this.maxTokens = maxTokens;
        this.apiKeyConfigured = StringUtils.hasText(apiKey);
        if (!apiKeyConfigured) {
            log.warn("Brak skonfigurowanego groq.api.key – narzędzie summarize_report będzie zwracać błąd. "
                    + "W produkcji klucz pochodzi z Vault (secret/mcp-server -> groq.api.key).");
        }

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Retryable(
            retryFor = {HttpServerErrorException.class, ResourceAccessException.class,
                    HttpClientErrorTooManyRequestsException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0))
    public String summarize(ReportDetailsDTO report) {
        if (!apiKeyConfigured) {
            return "Nie można wygenerować podsumowania: brak skonfigurowanego klucza API Groq "
                    + "(groq.api.key). Skonfiguruj sekret w Vault (secret/mcp-server).";
        }

        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", 0.2,
                "max_tokens", maxTokens,
                "messages", List.of(
                        Map.of("role", "system", "content", """
                                Jesteś asystentem prowadzącego zajęcia. Na podstawie surowych danych
                                raportu antyplagiatowego (JPlag) napisz zwięzłe podsumowanie po polsku.
                                Wskaż ogólny poziom podobieństwa, wyróżnij pary zgłoszeń o najwyższym
                                podobieństwie i zasygnalizuj, które przypadki wymagają ręcznej weryfikacji.
                                Pisz rzeczowo, nie wymyślaj danych spoza raportu."""),
                        Map.of("role", "user", "content", buildPrompt(report))
                )
        );

        ChatCompletionResponse response = restClient.post()
                .body(body)
                .retrieve()
                .onStatus(status -> status.value() == 429, (req, res) -> {
                    throw new HttpClientErrorTooManyRequestsException(
                            "Groq rate limit (HTTP 429)");
                })
                .body(ChatCompletionResponse.class);

        if (response == null || response.choices() == null || response.choices().isEmpty()
                || response.choices().getFirst().message() == null) {
            log.warn("Groq zwrócił pustą odpowiedź dla raportu {}", report.id());
            return "Nie udało się wygenerować podsumowania – model nie zwrócił treści.";
        }
        return response.choices().getFirst().message().content();
    }

    @Recover
    public String summarizeRecover(Exception e, ReportDetailsDTO report) {
        log.error("Nie udało się wygenerować podsumowania raportu {} po ponowieniach: {}",
                report.id(), e.getMessage(), e);
        return "Usługa modelu (Groq) jest chwilowo niedostępna – nie udało się wygenerować "
                + "podsumowania. Spróbuj ponownie za chwilę.";
    }

    private String buildPrompt(ReportDetailsDTO report) {
        StringBuilder sb = new StringBuilder();
        sb.append("Przedmiot: ").append(report.subjectName()).append('\n');
        sb.append("Etykieta (tag): ").append(report.tag()).append('\n');
        sb.append("Data utworzenia: ").append(report.createdAt()).append('\n');
        sb.append("Średnie podobieństwo w raporcie: ")
                .append(formatPercent(report.averageSimilarity())).append('\n');

        List<ComparisonDTO> comparisons = report.comparisons() == null ? List.of() : report.comparisons();
        sb.append("Liczba porównanych par: ").append(comparisons.size()).append("\n\n");
        sb.append("Najwyżej podobne pary (max ").append(MAX_COMPARISONS_IN_PROMPT).append("):\n");

        comparisons.stream()
                .limit(MAX_COMPARISONS_IN_PROMPT)
                .forEach(c -> sb.append("- ")
                        .append(c.firstSubmission()).append(" ↔ ").append(c.secondSubmission())
                        .append(" | max=").append(formatPercent(c.maxSimilarity()))
                        .append(", śr=").append(formatPercent(c.averageSimilarity()))
                        .append('\n'));
        sb.append("Nie korzystaj z tabel, bo się ładnie nie chcą formatować na froncie. " +
                "Możesz korzystać z formatowania tekstu w Markdown ale nei stosuj tabel!!!")
                .append("\n");

        return sb.toString();
    }

    private String formatPercent(Double value) {
        if (value == null) {
            return "b/d";
        }
        return String.format("%.1f%%", value * 100);
    }

    static class HttpClientErrorTooManyRequestsException extends RuntimeException {
        HttpClientErrorTooManyRequestsException(String message) {
            super(message);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ChatCompletionResponse(List<Choice> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Choice(Message message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Message(String role, String content) {
    }
}