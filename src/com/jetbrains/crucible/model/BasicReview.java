
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
  private User myCreator;
  private String myDescription;
  @Nullable
  private User myModerator;
  private String myName;

  @NotNull
  private String myProjectKey;
  private String myRepoName;
  private Date myCreateDate;
  private Date myCloseDate;
  private String mySummary;
  private final String myServerUrl;
  private String myState;

  public BasicReview(@NotNull String serverUrl, @NotNull String projectKey, @NotNull User author,
                     @Nullable User moderator) {
    myServerUrl = serverUrl;
    myProjectKey = projectKey;
    myAuthor = author;
    myModerator = moderator;
  }

  public void setCreator(User creator) {
    myCreator = creator;
  }

  @Override
  public String toString() {
    return myProjectKey + "  " + myDescription + "  " + myState + "   " + myAuthor.getUserName();
  }

  public void setDescription(String description) {
    myDescription = description;
  }

  public void setState(String state) {
    myState = state;
  }
}