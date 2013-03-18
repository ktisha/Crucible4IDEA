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
  private List<Comment> myComments = new ArrayList<Comment>();
  private Map<String, VirtualFile > myRevisions = new HashMap<String, VirtualFile>();
  private Map<String, VirtualFile > myIdToFile = new HashMap<String, VirtualFile>();

  public Review(@NotNull String serverUrl, @NotNull String id, @NotNull User author,
                @Nullable User moderator) {
    super(serverUrl, id, author, moderator);
  }

  public void addGeneralComment(@NotNull Comment generalComment) {
    myGeneralComments.add(generalComment);
  }

  public void addComment(@NotNull Comment comment) {
    myComments.add(comment);
  }

  @NotNull
  public List<Comment> getGeneralComments() {
    return myGeneralComments;
  }

  @NotNull
  public List<Comment> getComments() {
    return myComments;
  }

  public void addRevision(String revision, VirtualFile virtualFile) {
    myRevisions.put(revision, virtualFile);
  }

  public Map<String, VirtualFile> getRevisions() {
    return myRevisions;
  }

  public void addIdToFile(@NotNull final String id, @NotNull final VirtualFile virtualFile) {
    myIdToFile.put(id, virtualFile);
  }

  public VirtualFile getFileById(@NotNull final String id) {
    return myIdToFile.get(id);
  }
}