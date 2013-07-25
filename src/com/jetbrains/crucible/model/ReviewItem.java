package com.jetbrains.crucible.model;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.crucible.connection.CrucibleManager;
import com.jetbrains.crucible.vcs.VcsUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 *
 * User : ktisha
 */
public class ReviewItem {

  private static final Logger LOG = Logger.getInstance(ReviewItem.class);

  private String myId;
  private String myPath;
  private String myRepo;
  private Set<String> myRevisions = new HashSet<String>();

  public ReviewItem(@NotNull final String id, @NotNull final String path,
                    @Nullable final String repo) {
    myId = id;
    myPath = path;
    myRepo = repo;
  }

  public List<CommittedChangeList> loadChangeLists(Project project,
                                                   AbstractVcs vcsFor,
                                                   VirtualFile virtualFile,
                                                   Set<String> loadedRevisions) {
    final Set<String> revisions = getRevisions();
    final String repoName = getRepo();
    final Map<String, VirtualFile> hash = CrucibleManager.getInstance(project).getRepoHash();
    final VirtualFile root = hash.containsKey(repoName) ? hash.get(repoName) : virtualFile;

    List<CommittedChangeList> changeLists = new ArrayList<CommittedChangeList>();

    for (String revision : revisions) {
      if (!loadedRevisions.contains(revision)) {
        try {
          final VcsRevisionNumber revisionNumber = vcsFor.parseRevisionNumber(revision);
          if (revisionNumber != null && root != null) {
            final CommittedChangeList changeList = VcsUtils.loadRevisionsFromGit(project, root, revisionNumber);
            if (changeList != null) changeLists.add(changeList);
          }
        }
        catch (VcsException e) {
          LOG.warn(e.getMessage());
        }
        loadedRevisions.add(revision);
      }
    }

    return changeLists;
  }

  @NotNull
  public String getRepo() {
    return myRepo;
  }

  public void setRepo(@NotNull final String repo) {
    myRepo = repo;
  }

  @NotNull
  public String getPath() {
    return myPath;
  }

  public void setPath(@NotNull final String path) {
    myPath = path;
  }

  @NotNull
  public String getId() {
    return myId;
  }

  public void setId(@NotNull final String id) {
    myId = id;
  }
  public void addRevision(@NotNull final String revision) {
    myRevisions.add(revision);
  }

  @NotNull
  public Set<String> getRevisions() {
    return myRevisions;
  }

  public void setRevisions(@NotNull final Set<String> revisions) {
    myRevisions = revisions;
  }
}