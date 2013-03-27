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

  private Set<ReviewItem> myItems = new HashSet<ReviewItem>();

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

  @NotNull
  public Set<ReviewItem> getReviewItems() {
    return myItems;
  }

  public void addReviewItem(@NotNull final ReviewItem item) {
    myItems.add(item);
  }

  @Nullable
  public String getPathById(@NotNull final String id) {
    for (ReviewItem item : myItems) {
      if (item.getId().equals(id))
        return item.getPath();
    }
    return null;
  }
}