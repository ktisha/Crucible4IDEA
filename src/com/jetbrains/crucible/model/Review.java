package com.jetbrains.crucible.model;

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
  private Set<String> myRevisions = new HashSet<String>();
  private Map<String, String> myIdToPath = new HashMap<String, String>();

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

  public void addRevision(@NotNull final String revision) {
    myRevisions.add(revision);
  }

  @NotNull
  public Set<String> getRevisions() {
    return myRevisions;
  }

  public void addIdToFile(@NotNull final String id, @NotNull final String path) {
    myIdToPath.put(id, path);
  }

  public String getPathById(@NotNull final String id) {
    return myIdToPath.get(id);
  }
}