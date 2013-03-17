package com.jetbrains.crucible.model;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 *
 * User : ktisha
 */
public class Review extends BasicReview {
  private List<Comment> myGeneralComments = new ArrayList<Comment>();
  private Map<String, VirtualFile > myRevisions = new HashMap<String, VirtualFile>();

  public Review(@NotNull String serverUrl, @NotNull String id, @NotNull User author,
                @Nullable User moderator) {
    super(serverUrl, id, author, moderator);
  }

  public void addGeneralComment(@NotNull Comment generalComment) {
    myGeneralComments.add(generalComment);
  }

  @NotNull
  public List<Comment> getGeneralComments() {
    return myGeneralComments;
  }

  public void addRevision(String revision, VirtualFile virtualFile) {
    myRevisions.put(revision, virtualFile);
  }

  public Map<String, VirtualFile> getRevisions() {
    return myRevisions;
  }
}