package pl.lodz.p.it.ssbd2026.mcpserver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RestPage<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int number,
        int size
) {
    public List<T> contentOrEmpty() {
        return content == null ? List.of() : content;
    }
}
