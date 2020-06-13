package com.jetbrains.crucible.vcs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.util.ThrowableRunnable;
import com.intellij.util.ui.VcsSynchronousProgressWrapper;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: ktisha
 */
public class VcsUtils {

  private VcsUtils() {
  }

  @Nullable
  public static CommittedChangeList loadRevisions(@NotNull final Project project, final VcsRevisionNumber number, final FilePath filePath) {
    final CommittedChangeList[] list = new CommittedChangeList[1];
    final ThrowableRunnable<VcsException> runnable = () -> {
      final AbstractVcs vcs = VcsUtil.getVcsFor(project, filePath);

      if (vcs == null) {
        return;
      }

      list[0] = vcs.loadRevisions(filePath.getVirtualFile(), number);
    };

    final boolean success = VcsSynchronousProgressWrapper.wrap(runnable, project, "Load Revision Contents");

    return success ? list[0] : null;
  }
}