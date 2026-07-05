package pl.lodz.p.it.ssbd2026.mcpserver;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.lodz.p.it.ssbd2026.mcpserver.client.ReportApiClient;
import pl.lodz.p.it.ssbd2026.mcpserver.dto.ComparisonDTO;
import pl.lodz.p.it.ssbd2026.mcpserver.dto.ReportDetailsDTO;
import pl.lodz.p.it.ssbd2026.mcpserver.tools.ReportTools;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class McpServerIntegrationTest {

    private static final HttpServer GROQ_STUB;
    private static volatile int groqStatus = 200;
    private static volatile String groqBody = "{}";
    private static final AtomicInteger groqRequests = new AtomicInteger();

    static {
        try {
            GROQ_STUB = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            GROQ_STUB.createContext("/v1/chat/completions", exchange -> {
                groqRequests.incrementAndGet();
                byte[] resp = groqBody.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(groqStatus, resp.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(resp);
                }
            });
            GROQ_STUB.start();
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @DynamicPropertySource
    static void groqProps(DynamicPropertyRegistry registry) {
        int port = GROQ_STUB.getAddress().getPort();
        registry.add("groq.api.url", () -> "http://localhost:" + port + "/v1/chat/completions");
        registry.add("groq.api.key", () -> "test-key");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private ReportTools reportTools;

    @MockitoBean
    private ReportApiClient reportApiClient;

    private static final UUID ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @BeforeEach
    void reset() {
        groqStatus = 200;
        groqBody = "{}";
        groqRequests.set(0);
    }

    private ReportDetailsDTO sampleReport() {
        return new ReportDetailsDTO(ID, "lab1", "Programowanie", LocalDateTime.now(), 0.5,
                List.of(new ComparisonDTO("a", "b", 0.95, 0.8, 20.0)));
    }

    @Test
    void mcpEndpoint_exposesThreeTools() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String base = "http://localhost:" + port + "/mcp";

        HttpResponse<String> init = client.send(
                mcp(base, """
                        {"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"it","version":"1"}}}""", null),
                HttpResponse.BodyHandlers.ofString());
        String sid = init.headers().firstValue("mcp-session-id").orElseThrow();

        client.send(mcp(base, "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}", sid),
                HttpResponse.BodyHandlers.ofString());

        HttpResponse<String> tools = client.send(
                mcp(base, "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\",\"params\":{}}", sid),
                HttpResponse.BodyHandlers.ofString());

        assertThat(tools.body())
                .contains("summarize_report")
                .contains("list_reports_for_subject")
                .contains("get_report_details");
    }

    @Test
    void summarize_happyPath_returnsGroqContent() {
        given(reportApiClient.getReportDetails(ID)).willReturn(sampleReport());
        groqStatus = 200;
        groqBody = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"PODSUMOWANIE Z GROQ\"}}]}";

        String result = reportTools.summarizeReport(ID.toString());

        assertThat(result).isEqualTo("PODSUMOWANIE Z GROQ");
        assertThat(groqRequests.get()).isEqualTo(1);
    }

    @Test
    void summarize_groqFailing_retriesThenRecovers() {
        given(reportApiClient.getReportDetails(ID)).willReturn(sampleReport());
        groqStatus = 500;
        groqBody = "{\"error\":\"boom\"}";

        String result = reportTools.summarizeReport(ID.toString());

        assertThat(result).contains("Groq").contains("niedostępna");
        assertThat(groqRequests.get()).isEqualTo(3); // maxAttempts=3 -> @Recover
    }

    private static HttpRequest mcp(String url, String body, String sessionId) {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (sessionId != null) {
            b.header("mcp-session-id", sessionId);
        }
        return b.build();
    }
}
