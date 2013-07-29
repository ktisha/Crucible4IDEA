package com.jetbrains.crucible.model;

import com.intellij.openapi.diff.impl.patch.PatchReader;
import com.intellij.openapi.diff.impl.patch.PatchSyntaxException;
import com.intellij.openapi.diff.impl.patch.TextFilePatch;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.patch.FilePatchInProgress;
import com.intellij.openapi.vcs.changes.patch.PatchBaseDirectoryDetector;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeListImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Kirill Likhodedov
 */
public class PatchReviewItem extends ReviewItem {

  @NotNull private final VirtualFile myRoot;
  @NotNull private final String myPatch;
  @NotNull private String myName;
  @NotNull private String myComment;
  @NotNull private String myAuthorName;
  @NotNull private Date myDate;

  public PatchReviewItem(@NotNull String id, @NotNull String path, @NotNull VirtualFile root, @Nullable String repoName,
                         @NotNull String patchContent,
                         @NotNull String patchName, @NotNull String comment, @NotNull String author, @NotNull Date date) {
    super(id, path, repoName);
    myRoot = root;
    myPatch = patchContent;
    myName = patchName;
    myAuthorName = author;
    myComment = comment;
    myDate = date;
  }

  @NotNull
  @Override
  public List<CommittedChangeList> loadChangeLists(@NotNull Project project, @NotNull AbstractVcs vcsFor, @NotNull VirtualFile root,
                                                   @NotNull Set<String> loadedRevisions) throws VcsException {
    if (loadedRevisions.contains(myName)) {
      return Collections.emptyList();
    }

    List<TextFilePatch> patches;
    try {
      patches = new PatchReader(myPatch).readAllPatches();
    }
    catch (PatchSyntaxException e) {
      throw new VcsException(e);
    }
    loadedRevisions.add(myName);
    return Collections.<CommittedChangeList>singletonList(
      new CommittedChangeListImpl(myName, myComment, myAuthorName, -1, myDate, getChanges(project, patches)));
  }

  @NotNull
  private List<Change> getChanges(@NotNull final Project project, @NotNull List<TextFilePatch> patches) {
    return ContainerUtil.map(patches, new Function<TextFilePatch, Change>() {
      @Override
      public Change fun(TextFilePatch patch) {
        adjustPatchPaths(project, patch);
        return new FilePatchInProgress(patch, null, myRoot).getChange();
      }
    });
  }

  private void adjustPatchPaths(@NotNull Project project, @NotNull TextFilePatch patch) {
    String origBeforeName = patch.getBeforeName();
    PatchBaseDirectoryDetector.Result before = PatchBaseDirectoryDetector.getInstance(project).detectBaseDirectory(origBeforeName);
    if (before != null) {
      patch.setBeforeName(createDetectedRelativePath(before, origBeforeName));
    }
    String origAfterName = patch.getAfterName();
    PatchBaseDirectoryDetector.Result after = PatchBaseDirectoryDetector.getInstance(project).detectBaseDirectory(origAfterName);
    if (after != null) {
      patch.setAfterName(createDetectedRelativePath(after, origAfterName));
    }
  }

  private String createDetectedRelativePath(@NotNull PatchBaseDirectoryDetector.Result before, @NotNull String origName) {
    String[] split = origName.split("/");
    List<String> subList = new ArrayList<String>(Arrays.asList(split).subList(before.stripDirs, split.length));
    return StringUtil.join(ArrayUtil.toObjectArray(subList, String.class), "/");
  }

}
