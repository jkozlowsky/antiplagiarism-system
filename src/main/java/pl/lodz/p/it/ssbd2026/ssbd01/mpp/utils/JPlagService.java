package pl.lodz.p.it.ssbd2026.ssbd01.mpp.utils;

import de.jplag.JPlag;
import de.jplag.JPlagResult;
import de.jplag.exceptions.ExitException;
import de.jplag.java.JavaLanguage;
import de.jplag.options.JPlagOptions;
import de.jplag.reporting.reportobject.ReportObjectFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.JplagScanException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ReportFileException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
public class JPlagService {
    public JPlagResult runPlagiarismScanOnOrganisation(
            String organizationName,
            String tag,
            Integer minimumTokensMatch,
            Boolean normalize,
            String reportPath
    ) {
        JavaLanguage language = new JavaLanguage();

        File absoluteSubmissionDir = new File("repos/submissions/" + organizationName + "/" + tag + "/").getAbsoluteFile();

        Set<File> submissionDirectories = Set.of(absoluteSubmissionDir);
        log.info("dupa");
        submissionDirectories.forEach(s -> {
            log.info(s.getPath());
        });
        File teamADir = new File(absoluteSubmissionDir, "Team_A");
        log.info("Legacy IO check on Team_A: {}",
                teamADir.listFiles() != null ? teamADir.listFiles().length : "NULL");
        try {
            long fileCount = java.nio.file.Files.walk(absoluteSubmissionDir.toPath())
                    .filter(path -> path.toString().endsWith(".java"))
                    .count();
            log.info("Java NIO found {} .java files before JPlag scan.", fileCount);
        } catch (IOException e) {
            log.error("Java NIO failed to walk the directory!", e);
        }

        JPlagOptions options = new JPlagOptions(language, submissionDirectories, Set.of())
                .withMinimumTokenMatch(minimumTokensMatch)
                .withNormalize(normalize);

        try {
            JPlagResult result = JPlag.run(options);
            ReportObjectFactory reportObjectFactory = new ReportObjectFactory(
                    new File(reportPath)
            );
            reportObjectFactory.createAndSaveReport(result);
            return result;
        } catch (FileNotFoundException e) {
            throw new ReportFileException(e);
        } catch (ExitException e) {
            log.error("JPlag Exit Reason: {}", e.getMessage());
            log.error("JPlag Exit Reason: {}", reportPath);
            throw new JplagScanException(e);
        }

    }
}
