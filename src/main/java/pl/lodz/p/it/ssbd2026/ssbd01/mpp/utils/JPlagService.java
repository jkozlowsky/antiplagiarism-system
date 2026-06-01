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


//        JPlagOptions options = new JPlagOptions(language, submissionDirectories, Set.of())
//                .withMinimumTokenMatch(minimumTokensMatch)
//                .withNormalize(normalize);

        JPlagOptions options = new JPlagOptions(language, submissionDirectories, Set.of())
                .withMinimumTokenMatch(minimumTokensMatch)
                .withNormalize(normalize)
                // THIS IS THE CRITICAL LINE:
                .withSubdirectoryName("src/main/java");
        log.info(submissionDirectories.toString());
        for (int i = 0; i < absoluteSubmissionDir.listFiles().length; i++) {
            log.info(absoluteSubmissionDir.listFiles()[i].getPath());
        }

        // =========================================================
// JPLAG DIAGNOSTIC TRAP
// =========================================================
        try {
            // TEST 1: The Canonical Path Trap
            File teamADir = new File(absoluteSubmissionDir, "Team_A");
            File canonicalA = teamADir.getCanonicalFile();
            File[] legacyFiles = canonicalA.listFiles();

            log.info("Canonical Path Resolved To: {}", canonicalA.getAbsolutePath());
            log.info("Files found via canonical legacy IO: {}", legacyFiles == null ? "NULL" : legacyFiles.length);

            // TEST 2: The Fatal Error Trap
            File testJava = new File(absoluteSubmissionDir, "Team_A/src/main/java/com/example/Main.java");
            if (testJava.exists()) {
                log.info("Forcing JavaParser to run directly to trap hidden Errors...");
                // This will bypass JPlag's parallel streams and force the Error to print to your Spring Boot logs
                com.github.javaparser.StaticJavaParser.parse(testJava);
                log.info("JavaParser engine parsed the file SUCCESSFULLY!");
            } else {
                log.warn("Main.java not found where expected: {}", testJava.getAbsolutePath());
            }

        } catch (Throwable t) {
            // Notice we are catching Throwable, not Exception! This traps java.lang.Error.
            log.error(">>> TRAPPED FATAL ENGINE CRASH: ", t);
        }
// =========================================================

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
            log.error("JPlag Exit Reason: {}", absoluteSubmissionDir.getPath());
            throw new JplagScanException(e);
        }

    }
}
