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

  public ReviewItem(@NotNull String id, @NotNull String path, @Nullable String repo) {
    myId = id;
    myPath = path;
    myRepo = repo;
  }

  public String getRepo() {
    return myRepo;
  }

  public void setRepo(String repo) {
    myRepo = repo;
  }

  public String getPath() {
    return myPath;
  }

  public void setPath(String path) {
    myPath = path;
  }

  public String getId() {
    return myId;
  }

  public void setId(String id) {
    myId = id;
  }
  public void addRevision(String revision) {
    myRevisions.add(revision);
  }

  public Set<String> getRevisions() {
    return myRevisions;
  }

  public void setRevisions(Set<String> revisions) {
    myRevisions = revisions;
  }
}