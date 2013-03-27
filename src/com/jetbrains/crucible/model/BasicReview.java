
package com.jetbrains.crucible.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.Set;

/**
 * User : ktisha
 */
public class BasicReview {
  private Set<Reviewer> myReviewers;
  @NotNull
  private User myAuthor;
  private String myDescription;
  @Nullable
  private User myModerator;

  @NotNull
  private String myPermaId;
  private Date myCreateDate;
  private final String myServerUrl;
  private String myState;

  public BasicReview(@NotNull String serverUrl, @NotNull String permaId, @NotNull User author,
                     @Nullable User moderator) {
    myServerUrl = serverUrl;
    myPermaId = permaId;
    myAuthor = author;
    myModerator = moderator;
  }

  @Override
  public String toString() {
    return myPermaId + "  " + myDescription + "  " + myState + "   " + myAuthor.getUserName();
  }

  public void setDescription(String description) {
    myDescription = description;
  }

  public void setState(String state) {
    myState = state;
  }

  @NotNull
  public String getPermaId() {
    return myPermaId;
  }

  public String getDescription() {
    return myDescription;
  }

  public String getState() {
    return myState;
  }

  @NotNull
  public User getAuthor() {
    return myAuthor;
  }

  public Date getCreateDate() {
    return myCreateDate;
  }

  public void setCreateDate(Date createDate) {
    myCreateDate = createDate;
  }
}