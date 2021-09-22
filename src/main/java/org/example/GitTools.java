package org.example;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;

public class GitTools {
    public static String getLastCommitHash() {
        return getLastCommitHash("./.git");
    }

    public static String getLastCommitHash(String path) {
        final FileRepositoryBuilder fileRepositoryBuilder = new FileRepositoryBuilder();
        final Repository repository;
        try {
            repository = fileRepositoryBuilder.setGitDir(new File(path)).findGitDir().build();
            try {
                final ObjectId head = repository.resolve("HEAD");
                final RevWalk revWalk = new RevWalk(repository);
                final RevCommit revCommit = revWalk.parseCommit(head);
                return revCommit.name();
            } finally {
                repository.close();
            }
        } catch (Throwable e) {
            return "";
        }
    }
}
