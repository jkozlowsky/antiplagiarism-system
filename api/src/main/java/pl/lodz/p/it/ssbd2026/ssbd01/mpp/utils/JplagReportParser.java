package pl.lodz.p.it.ssbd2026.ssbd01.mpp.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.TeacherComparisonDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.TeacherMatchedFileDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ReportFileException;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.reportObjects.TopComparison;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JplagReportParser {

    private static final int MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;


    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static List<TeacherComparisonDTO> parseFullTeacherReport(String pathToJplagFile) {
        try (ZipFile zipFile = new ZipFile(pathToJplagFile)) {

            ZipEntry topEntry = zipFile.getEntry("topComparisons.json");
            if (topEntry == null) throw new RuntimeException("Brak topComparisons.json w archiwum.");

            List<TopComparison> topComparisons;
            try (InputStream is = zipFile.getInputStream(topEntry)) {
                String jsonContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                topComparisons = objectMapper.readValue(jsonContent, new TypeReference<>() {});
            }

            List<TeacherComparisonDTO> result = new ArrayList<>();

            for (TopComparison comp : topComparisons) {
                String sub1 = comp.firstSubmission();
                String sub2 = comp.secondSubmission();

                ZipEntry detailEntry = zipFile.getEntry("comparisons/" + sub1 + "-" + sub2 + ".json");
                if (detailEntry == null) {
                    detailEntry = zipFile.getEntry("comparisons/" + sub2 + "-" + sub1 + ".json");
                }

                String fileA = null, fileB = null, codeA = null, codeB = null;
                List<Integer> linesA = List.of();
                List<Integer> linesB = List.of();

                List<TeacherMatchedFileDTO> matchedFiles = new ArrayList<>();

                if (detailEntry != null) {
                    try (InputStream is = zipFile.getInputStream(detailEntry)) {
                        String detailJson = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                        JsonNode rootNode = objectMapper.readTree(detailJson);
                        JsonNode matchesNode = rootNode.get("matches");

                        if (matchesNode != null && matchesNode.isArray() && matchesNode.size() > 0) {

                            for (int i = 0; i < matchesNode.size(); i++) {
                                JsonNode match = matchesNode.get(i);
                                String fA = null, fB = null, cA = null, cB = null;
                                List<Integer> lA = List.of(), lB = List.of();

                                int t = 0;
                                if (match.has("firstFileName") && match.has("secondFileName")) {
                                    fA = match.get("firstFileName").asText();
                                    fB = match.get("secondFileName").asText();
                                    int s1 = match.path("startInFirst").path("line").asInt();
                                    int e1 = match.path("endInFirst").path("line").asInt();
                                    int s2 = match.path("startInSecond").path("line").asInt();
                                    int e2 = match.path("endInSecond").path("line").asInt();
                                    t = match.path("lengthOfFirst").asInt(0);
                                    lA = IntStream.rangeClosed(s1, e1).boxed().toList();
                                    lB = IntStream.rangeClosed(s2, e2).boxed().toList();
                                } else if (match.has("match1") && match.has("match2")) {
                                    JsonNode m1 = match.get("match1");
                                    JsonNode m2 = match.get("match2");
                                    fA = m1.has("file") ? m1.get("file").asText() : m1.get("name").asText();
                                    fB = m2.has("file") ? m2.get("file").asText() : m2.get("name").asText();
                                    int s1 = m1.has("start") ? m1.get("start").asInt() : m1.get("startLine").asInt();
                                    int l1 = m1.has("length") ? m1.get("length").asInt() : m1.get("tokens").asInt();
                                    int s2 = m2.has("start") ? m2.get("start").asInt() : m2.get("startLine").asInt();
                                    int l2 = m2.has("length") ? m2.get("length").asInt() : m2.get("tokens").asInt();
                                    t = m1.has("length") ? m1.get("length").asInt() : m1.get("tokens").asInt();
                                    lA = IntStream.range(s1, s1 + l1).boxed().toList();
                                    lB = IntStream.range(s2, s2 + l2).boxed().toList();
                                }

                                if (fA != null && fB != null) {
                                    cA = readFileFromZipSmart(zipFile, sub1, fA);
                                    cB = readFileFromZipSmart(zipFile, sub2, fB);
                                }

                                if (i == 0) {
                                    fileA = fA; fileB = fB; codeA = cA; codeB = cB;
                                    linesA = lA; linesB = lB;
                                }

                                matchedFiles.add(new TeacherMatchedFileDTO(fA, fB, cA, cB, lA, lB, t));
                            }
                        }
                    }
                }

                result.add(new TeacherComparisonDTO(
                        sub1, sub2,
                        comp.similarities().max(),
                        comp.similarities().avg(),
                        comp.similarities().longestMatch(),
                        fileA, fileB, codeA, codeB, linesA, linesB,
                        matchedFiles
                ));
            }

            return result;

        } catch (Exception e) {
            throw new ReportFileException(e);
        }
    }

    private static String readFileFromZipSmart(ZipFile zipFile, String submission, String fileName) {
        if (fileName == null || fileName.isBlank()) return "// Brak pliku w JSON.";

        String normalizedFileName = fileName.replace("\\", "/");
        if (normalizedFileName.startsWith("/")) normalizedFileName = normalizedFileName.substring(1);

        if (normalizedFileName.contains("..")) {
            return "// Odmowa dostępu: Niedozwolona ścieżka pliku (Zip Slip protection).";
        }

        if (normalizedFileName.startsWith(submission + "/")) {
            normalizedFileName = normalizedFileName.substring(submission.length() + 1);
        }


        String exactPath = "files/" + submission + "/" + normalizedFileName;
        ZipEntry entry = zipFile.getEntry(exactPath);
        if (entry != null) {
            try (InputStream is = zipFile.getInputStream(entry)) {
                return readStreamSafely(is);
            } catch (Exception ignored) {}
        }

        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();
            String eName = e.getName();
            if (eName.contains("..")) continue;
            if (!e.isDirectory() && eName.startsWith("files/" + submission + "/") && eName.endsWith(normalizedFileName)) {
                try (InputStream is = zipFile.getInputStream(e)) {
                    return readStreamSafely(is);
                } catch (Exception ignored) {}
            }
        }
        return "// Nie odczytano kodu z archiwum.\n// Próbowano znaleźć: " + exactPath;
    }


    private static String readStreamSafely(InputStream is) throws IOException {
        byte[] buffer = new byte[8192];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int totalBytesRead = 0;
        int bytesRead;

        while ((bytesRead = is.read(buffer)) != -1) {
            totalBytesRead += bytesRead;

            if (totalBytesRead > MAX_FILE_SIZE_BYTES) {
                throw new IOException("Przekroczono limit rozmiaru pliku. (Zip Bomb protection)");
            }
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toString(StandardCharsets.UTF_8);
    }
}