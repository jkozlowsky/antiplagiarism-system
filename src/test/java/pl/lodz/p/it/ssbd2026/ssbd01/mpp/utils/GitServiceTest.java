package pl.lodz.p.it.ssbd2026.ssbd01.mpp.utils;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.GitCloneException;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.JGitException;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GitServiceTest {

    @Mock
    private CloneCommand cloneCommand;

    @Mock
    private Git gitMock;

    @Test
    void cloneTag_Success() throws Exception {
        String repoUrl = "https://github.com/test/repo.git";
        String tagName = "v1.0";
        File localPath = new File("test-folder");

        try (MockedStatic<Git> mockedGit = mockStatic(Git.class)) {

            mockedGit.when(Git::cloneRepository).thenReturn(cloneCommand);

            when(cloneCommand.setURI(repoUrl)).thenReturn(cloneCommand);
            when(cloneCommand.setDirectory(localPath)).thenReturn(cloneCommand);
            when(cloneCommand.setBranch("refs/tags/" + tagName)).thenReturn(cloneCommand);
            when(cloneCommand.setCredentialsProvider(any(UsernamePasswordCredentialsProvider.class))).thenReturn(cloneCommand);

            when(cloneCommand.call()).thenReturn(gitMock);

            assertDoesNotThrow(() -> GitService.cloneTag(repoUrl, tagName, localPath));

            verify(cloneCommand).setURI(repoUrl);
            verify(cloneCommand).setDirectory(localPath);
            verify(cloneCommand).setBranch("refs/tags/v1.0");
            verify(cloneCommand).call();
        }
    }

    @Test
    void cloneTag_ThrowsGitCloneException_WhenGitAPIFails() throws Exception {
        String repoUrl = "https://github.com/test/repo.git";
        String tagName = "v1.0";
        File localPath = new File("test-folder");

        try (MockedStatic<Git> mockedGit = mockStatic(Git.class)) {
            mockedGit.when(Git::cloneRepository).thenReturn(cloneCommand);

            when(cloneCommand.setURI(any())).thenReturn(cloneCommand);
            when(cloneCommand.setDirectory(any())).thenReturn(cloneCommand);
            when(cloneCommand.setBranch(any())).thenReturn(cloneCommand);
            when(cloneCommand.setCredentialsProvider(any())).thenReturn(cloneCommand);

            when(cloneCommand.call()).thenThrow(new InvalidRemoteException("Remote not found"));

            assertThrows(GitCloneException.class, () ->
                    GitService.cloneTag(repoUrl, tagName, localPath)
            );
        }
    }

    @Test
    void cloneTag_ThrowsJGitException_WhenDirectoryAlreadyExists() throws Exception {
        String repoUrl = "https://github.com/test/repo.git";
        String tagName = "v1.0";
        File localPath = new File("test-folder");

        try (MockedStatic<Git> mockedGit = mockStatic(Git.class)) {
            mockedGit.when(Git::cloneRepository).thenReturn(cloneCommand);

            when(cloneCommand.setURI(any())).thenReturn(cloneCommand);
            when(cloneCommand.setDirectory(any())).thenReturn(cloneCommand);
            when(cloneCommand.setBranch(any())).thenReturn(cloneCommand);
            when(cloneCommand.setCredentialsProvider(any())).thenReturn(cloneCommand);

            when(cloneCommand.call()).thenThrow(new JGitInternalException("Destination already exists", new Exception()));

            assertThrows(JGitException.class, () ->
                    GitService.cloneTag(repoUrl, tagName, localPath)
            );
        }
    }
}