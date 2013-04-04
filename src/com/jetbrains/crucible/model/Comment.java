package com.jetbrains.crucible.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User : ktisha
 */
public class Comment {

  private String myMessage;
  private String myLine;
  private String myReviewItemId;
  private String myPermId;
  private String myRevision;
  private User myAuthor;
  private Date myCreateDate = new Date();
  private List<Comment> myReplies = new ArrayList<Comment>();
  private String myParentCommentId;

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

  public String getLine() {
    return myLine;
  }

  public void setLine(String line) {
    myLine = line;
  }

  public String getReviewItemId() {
    return myReviewItemId;
  }

  public void setReviewItemId(String reviewItemId) {
    myReviewItemId = reviewItemId;
  }

  public String getRevision() {
    return myRevision;
  }

  public void setRevision(String revision) {
    myRevision = revision;
  }

  public void addReply(Comment reply) {
    myReplies.add(reply);
  }

  public List<Comment> getReplies() {
    return myReplies;
  }

  public void setParentCommentId(String parentCommentId) {
    myParentCommentId = parentCommentId;
  }

  public String getParentCommentId() {
    return myParentCommentId;
  }

  public String getPermId() {
    return myPermId;
  }

  public void setPermId(String id) {
    myPermId = id;
  }
}
