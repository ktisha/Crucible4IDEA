package com.jetbrains.crucible.model;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.patch.AbstractFilePatchInProgress;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeListImpl;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author Kirill Likhodedov
 */
public class PatchReviewItem extends ReviewItem {

  @NotNull private final List<AbstractFilePatchInProgress> myPatches;
  @NotNull private String myName;
  @NotNull private String myComment;
  @NotNull private String myAuthorName;
  @NotNull private Date myDate;

  public PatchReviewItem(@NotNull String id, @NotNull String path,
                         @Nullable String repoName, @NotNull List<AbstractFilePatchInProgress> patches,
                         @NotNull String patchName, @NotNull String comment, @NotNull String author, @NotNull Date date) {
    super(id, path, repoName);
    myPatches = patches;
    myName = patchName;
    myAuthorName = author;
    myComment = comment;
    myDate = date;
  }

  @NotNull
  @Override
  public List<CommittedChangeList> loadChangeLists(@NotNull Project project, @NotNull AbstractVcs vcsFor,
                                                   @NotNull Set<String> loadedRevisions, FilePath path) throws VcsException {
    if (loadedRevisions.contains(myName)) {
      return Collections.emptyList();
    }
    loadedRevisions.add(myName);
    return Collections.<CommittedChangeList>singletonList(
      new CommittedChangeListImpl(myName, myComment, myAuthorName, -1, myDate, getChanges(myPatches)));
  }

  @Override
  public boolean isPatch() {
    return true;
  }

  @NotNull
  private List<Change> getChanges(@NotNull List<AbstractFilePatchInProgress> patches) {
    return ContainerUtil.map(patches, new Function<AbstractFilePatchInProgress, Change>() {
      @Override
      public Change fun(AbstractFilePatchInProgress patch) {
        return patch.getChange();
      }
    });
  }

}
