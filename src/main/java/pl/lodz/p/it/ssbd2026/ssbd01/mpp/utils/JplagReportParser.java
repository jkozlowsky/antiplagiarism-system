package pl.lodz.p.it.ssbd2026.ssbd01.mpp.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ReportFileException;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.reportObjects.TopComparison;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JplagReportParser {

    public static List<TopComparison> readJplagArchive(String pathToJplagFile) {
        try (ZipFile zipFile = new ZipFile(pathToJplagFile)) {
            ZipEntry topComparisonsEntry = zipFile.getEntry("topComparisons.json");

            if (topComparisonsEntry != null) {
                try (InputStream inputStream = zipFile.getInputStream(topComparisonsEntry)) {
                    String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    ObjectMapper objectMapper = new ObjectMapper();
                    return objectMapper.readValue(jsonContent, new TypeReference<List<TopComparison>>() {});
                }
            } else {
                throw new RuntimeException("Nie znaleziono pliku topComparisons.json w archiwum.");
            }
        } catch (Exception e) {
            throw new ReportFileException(e);
        }
    }
}
