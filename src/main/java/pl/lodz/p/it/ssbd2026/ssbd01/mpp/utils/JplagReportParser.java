package pl.lodz.p.it.ssbd2026.ssbd01.mpp.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JplagReportParser {

    public static void readJplagArchive(String pathToJplagFile) {

        try (ZipFile zipFile = new ZipFile(pathToJplagFile)) {

            ZipEntry topComparisonsEntry = zipFile.getEntry("topComparisons.json");

            if (topComparisonsEntry != null) {
                try (InputStream inputStream = zipFile.getInputStream(topComparisonsEntry)) {

                    // Read the raw JSON bytes into a String
                    String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

                    System.out.println("Found topComparisons.json! Content:");
                    System.out.println(jsonContent);
                    ObjectMapper objectMapper = new ObjectMapper();

//                    List<TopComparison> results = mapper.readValue(
//                            jsonString,
//                            new TypeReference<List<TopComparison>>() {}
//                    );
                    // 4. TODO: Use Jackson's ObjectMapper or Gson here to map
                    // the jsonContent string into your own Java DTO objects.
                    // ObjectMapper mapper = new ObjectMapper();
                    // TopComparisonsReport report = mapper.readValue(jsonContent, TopComparisonsReport.class);
                }
            } else {
                System.out.println("Could not find topComparisons.json in the archive.");
            }

            // Example of iterating through ALL files in the archive if needed:
            /*
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                System.out.println("Found file in archive: " + entry.getName());
            }
            */

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JPlag report archive", e);
        }
    }
}
