package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;

@Slf4j
public class GitService {
    public static void cloneTag(String repoUrl, String tagName, File localPath) {
        try (Git git = Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(localPath)
                .setBranch("refs/tags/" + tagName)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("token", "bda43f3d99674914c59753bb04d306b1426aa54e"))
                .call()) { // TODO: remove git token from here!!!!!!!!!

            System.out.println("Successfully cloned tag: " + tagName);

        } catch (GitAPIException e) {
//            throw new RuntimeException("Failed to clone repository at tag " + tagName, e);
        } catch (JGitInternalException e){
            log.info("Cannot clone repository {} at tag {} because it is already present in the folder", repoUrl, tagName);
        }
    }


}
