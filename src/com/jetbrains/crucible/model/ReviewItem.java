package com.jetbrains.crucible.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * User : ktisha
 */
public class ReviewItem {
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