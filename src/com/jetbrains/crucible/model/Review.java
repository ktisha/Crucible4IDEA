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
  private List<Comment> myGeneralComments = Collections.emptyList();
  private Map<String, VirtualFile > myRevisions = new HashMap<String, VirtualFile>();

  public Review(@NotNull String serverUrl, @NotNull String id, @NotNull User author,
                @Nullable User moderator) {
    super(serverUrl, id, author, moderator);
  }

  public void setGeneralComments(@NotNull List<Comment> generalComments) {
    this.myGeneralComments = generalComments;
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