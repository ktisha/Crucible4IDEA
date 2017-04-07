package com.jetbrains.crucible.vcs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ThrowableRunnable;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.VcsSynchronousProgressWrapper;
import com.intellij.vcs.log.VcsFullCommitDetails;
import com.intellij.vcs.log.util.VcsUserUtil;
import com.intellij.vcsUtil.VcsUtil;
import git4idea.GitRevisionNumber;
import git4idea.GitVcs;
import git4idea.changes.GitCommittedChangeList;
import git4idea.history.GitHistoryUtils;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.intellij.util.ObjectUtils.assertNotNull;

/**
 * User: ktisha
 */
public class VcsUtils {
  private VcsUtils() {
  }

  @Nullable
  public static CommittedChangeList loadRevisionsFromGit(@NotNull final Project project,
                                                         final VirtualFile virtualFile,
                                                         final VcsRevisionNumber number) {
    final CommittedChangeList[] list = new CommittedChangeList[1];
    final ThrowableRunnable<VcsException> runnable = () -> {
      FilePath filePath = VcsUtil.getFilePath(virtualFile);

      FilePath lastCommitName = GitHistoryUtils.getLastCommitName(project, filePath);
      GitRepository repository = GitRepositoryManager.getInstance(project).getRepositoryForFile(lastCommitName);
      if (repository == null) {
        return;
      }
      VirtualFile root = repository.getRoot();

      List<VcsFullCommitDetails> gitCommits = ContainerUtil.newArrayList();
      GitHistoryUtils.loadDetails(project, root, gitCommits::add,
                                  GitHistoryUtils.formHashParameters(repository.getVcs(), Collections.singleton(number.asString())));
      if (gitCommits.size() != 1) {
        return;
      }
      VcsFullCommitDetails gitCommit = gitCommits.get(0);
      CommittedChangeList commit = new GitCommittedChangeList(gitCommit.getFullMessage() + " (" + gitCommit.getId().toShortString() + ")",
                                                              gitCommit.getFullMessage(), VcsUserUtil.toExactString(gitCommit.getAuthor()),
                                                              (GitRevisionNumber)number,
                                                              new Date(gitCommit.getAuthorTime()), gitCommit.getChanges(),
                                                              assertNotNull(GitVcs.getInstance(project)), true);

      list[0] = commit;
    };
    final boolean success = VcsSynchronousProgressWrapper.wrap(runnable, project, "Load revision contents");
    return success ? list[0] : null;
  }
}