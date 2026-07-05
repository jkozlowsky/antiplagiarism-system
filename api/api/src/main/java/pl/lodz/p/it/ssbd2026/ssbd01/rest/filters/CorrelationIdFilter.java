package pl.lodz.p.it.ssbd2026.ssbd01.rest.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.UUID;

@RestControllerAdvice
public class CorrelationIdFilter implements Filter {

    public static final String CORRELATION_HEADER = "X-Correlation-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String correlationId = httpRequest.getHeader(CORRELATION_HEADER);

        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }
        try {
            MDC.put(CORRELATION_HEADER, correlationId);
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}