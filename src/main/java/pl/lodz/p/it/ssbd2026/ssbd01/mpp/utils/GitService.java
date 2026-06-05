package pl.lodz.p.it.ssbd2026.ssbd01.mpp.utils;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;

@Slf4j
public class GitService {

    private GitService(){}

    public static boolean cloneTag(String repoUrl, String tagName, File localPath, String giteaToken) {
        try (Git git = Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(localPath)
                .setBranch("refs/tags/" + tagName)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("token", giteaToken))
                .call()) {

            return true;

        } catch (GitAPIException e) {
            log.warn("Cannot clone repository {} at tag {} because tag is not present in the repository", repoUrl, tagName);
            return false;

        } catch (JGitInternalException e) {
            log.warn("Cannot clone repository {} at tag {} because it is already present in the folder", repoUrl, tagName);
            return true;
        }
    }


}
