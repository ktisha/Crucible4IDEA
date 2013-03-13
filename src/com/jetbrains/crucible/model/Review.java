package com.jetbrains.crucible.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 *
 * User : ktisha
 */
public class Review extends BasicReview {
  private List<String> myFiles = new ArrayList<String>();
  private List<Comment> myGeneralComments = Collections.emptyList();

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

  public void addFile(String file) {
    myFiles.add(file);
  }

  public List<String> getFiles() {
    return myFiles;
  }
}