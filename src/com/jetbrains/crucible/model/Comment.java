package com.jetbrains.crucible.model;

import java.util.Date;

/**
 * User : ktisha
 */
public class Comment {

  private String myPermId;
  private String myMessage;
  private User myAuthor;
  private Date myCreateDate = new Date();

  private final Review myReview;

  public Comment(Review review) {
    myReview = review;
  }

  public Review getReview() {
    return myReview;
  }

  public String getPermId() {
    return myPermId;
  }

  public void setPermId(String permId) {
    myPermId = permId;
  }

  public String getMessage() {
    return myMessage;
  }

  public void setMessage(String message) {
    myMessage = message;
  }

  public User getAuthor() {
    return myAuthor;
  }

  public void setAuthor(User author) {
    this.myAuthor = author;
  }

  public Date getCreateDate() {
    return new Date(myCreateDate.getTime());
  }

  public void setCreateDate(Date createDate) {
    if (createDate != null) {
      this.myCreateDate = new Date(createDate.getTime());
    }
  }

  @Override
  public String toString() {
    return getMessage();
  }
}
