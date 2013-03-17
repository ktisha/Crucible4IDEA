package com.jetbrains.crucible.model;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * User : ktisha
 */
public class Comment {

  private String myMessage;
  private User myAuthor;
  private Date myCreateDate = new Date();

  public Comment(@NotNull final User commentAuthor, @NotNull final String message) {
    myAuthor = commentAuthor;
    myMessage = message;
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
    myAuthor = author;
  }

  public Date getCreateDate() {
    return new Date(myCreateDate.getTime());
  }

  public void setCreateDate(Date createDate) {
    if (createDate != null) {
      myCreateDate = new Date(createDate.getTime());
    }
  }

  @Override
  public String toString() {
    return getMessage();
  }
}
