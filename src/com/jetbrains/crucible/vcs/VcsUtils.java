package com.jetbrains.crucible.vcs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePathImpl;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ThrowableRunnable;
import com.intellij.util.ui.VcsSynchronousProgressWrapper;
import git4idea.GitRevisionNumber;
import git4idea.changes.GitCommittedChangeList;
import git4idea.history.GitHistoryUtils;
import git4idea.history.browser.GitHeavyCommit;
import git4idea.history.browser.SymbolicRefs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * User: ktisha
 */
public class VcsUtils {
  private VcsUtils() {}

  @Nullable
  public static CommittedChangeList loadRevisionsFromGit(@NotNull final Project project, final VirtualFile vf, final VcsRevisionNumber number) {
    final CommittedChangeList[] list = new CommittedChangeList[1];
    final ThrowableRunnable<VcsException> runnable = new ThrowableRunnable<VcsException>() {
      @Override
      public void run() throws VcsException {
        final FilePathImpl filePath = new FilePathImpl(vf);
        final List<GitHeavyCommit> gitCommits =
          GitHistoryUtils.commitsDetails(project, filePath, new SymbolicRefs(), Collections.singletonList(number.asString()));
        if (gitCommits.size() != 1) {
          return;
        }
        final GitHeavyCommit gitCommit = gitCommits.get(0);
        CommittedChangeList commit = new GitCommittedChangeList(gitCommit.getDescription() + " (" + gitCommit.getShortHash().getString() + ")",
                                                                gitCommit.getDescription(), gitCommit.getAuthor(), (GitRevisionNumber)number,
                                                                new Date(gitCommit.getAuthorTime()), gitCommit.getChanges(), true);
        list[0] = commit;
      }
    };
    final boolean success = VcsSynchronousProgressWrapper.wrap(runnable, project, "Load revision contents");
    return success ? list[0] : null;
  }
}
