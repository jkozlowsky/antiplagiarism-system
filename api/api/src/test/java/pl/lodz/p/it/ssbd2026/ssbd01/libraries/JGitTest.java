package pl.lodz.p.it.ssbd2026.ssbd01.libraries;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class JGitTest {

    @TempDir
    Path tempDir;

    private Git git;
    private File repoPath;

    @BeforeEach
    void setUp() throws GitAPIException {
        repoPath = tempDir.toFile();
        git = Git.init().setDirectory(repoPath).call();
    }

    @AfterEach
    void tearDown() {
        if (git != null) {
            git.close();
        }
    }

    @Test
    void testInitRepository() {
        Repository repository = git.getRepository();
        assertNotNull(repository);
        assertTrue(new File(repoPath, ".git").exists());
    }

    @Test
    void testAddAndCommit() throws IOException, GitAPIException {
        String fileName = "hello.txt";
        File file = new File(repoPath, fileName);
        Files.writeString(file.toPath(), "Hello JGit!");

        git.add().addFilepattern(fileName).call();

        RevCommit commit = git.commit()
                .setMessage("Initial commit")
                .setAuthor("Tester", "test@example.com")
                .call();

        assertNotNull(commit);
        assertEquals("Initial commit", commit.getFullMessage());

        Iterable<RevCommit> logs = git.log().call();
        Iterator<RevCommit> iterator = logs.iterator();

        assertTrue(iterator.hasNext());
        RevCommit loggedCommit = iterator.next();
        assertEquals(commit.getId(), loggedCommit.getId());
    }

    @Test
    void testBranchCreation() throws GitAPIException, IOException {
        createInitialCommit();

        git.branchCreate().setName("feature-branch").call();

        git.checkout().setName("feature-branch").call();

        assertEquals("refs/heads/feature-branch", git.getRepository().getFullBranch());
    }

    private void createInitialCommit() throws GitAPIException {
        try {
            File file = new File(repoPath, "init.txt");
            Files.writeString(file.toPath(), "init");
            git.add().addFilepattern("init.txt").call();
            git.commit().setMessage("Base commit").call();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}