package com.jetbrains.crucible.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User : ktisha
 */
public class Comment {

  @NotNull private final User myAuthor;
  @NotNull private final String myMessage;
  private boolean myDraft;

  private String myLine;
  private String myReviewItemId;
  private String myPermId;
  private String myRevision;
  private Date myCreateDate = new Date();
  private final List<Comment> myReplies = new ArrayList<Comment>();
  private String myParentCommentId;

  public Comment(@NotNull final User commentAuthor, @NotNull final String message, boolean draft) {
    myAuthor = commentAuthor;
    myMessage = message;
    myDraft = draft;
  }

  @NotNull
  public String getMessage() {
    return myMessage;
  }

  @NotNull
  public User getAuthor() {
    return myAuthor;
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

  public boolean isDraft() {
    return myDraft;
  }

  public void setDraft(boolean draft) {
    myDraft = draft;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Comment comment = (Comment)o;

    if (myDraft != comment.myDraft) return false;
    if (!myAuthor.equals(comment.myAuthor)) return false;
    if (myCreateDate != null ? !myCreateDate.equals(comment.myCreateDate) : comment.myCreateDate != null) return false;
    if (myLine != null ? !myLine.equals(comment.myLine) : comment.myLine != null) return false;
    if (!myMessage.equals(comment.myMessage)) return false;
    if (myParentCommentId != null ? !myParentCommentId.equals(comment.myParentCommentId) : comment.myParentCommentId != null) return false;
    if (myPermId != null ? !myPermId.equals(comment.myPermId) : comment.myPermId != null) return false;
    if (myReplies != null ? !myReplies.equals(comment.myReplies) : comment.myReplies != null) return false;
    if (myReviewItemId != null ? !myReviewItemId.equals(comment.myReviewItemId) : comment.myReviewItemId != null) return false;
    if (myRevision != null ? !myRevision.equals(comment.myRevision) : comment.myRevision != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myAuthor.hashCode();
    result = 31 * result + myMessage.hashCode();
    result = 31 * result + (myDraft ? 1 : 0);
    result = 31 * result + (myLine != null ? myLine.hashCode() : 0);
    result = 31 * result + (myReviewItemId != null ? myReviewItemId.hashCode() : 0);
    result = 31 * result + (myPermId != null ? myPermId.hashCode() : 0);
    result = 31 * result + (myRevision != null ? myRevision.hashCode() : 0);
    result = 31 * result + (myCreateDate != null ? myCreateDate.hashCode() : 0);
    result = 31 * result + (myReplies != null ? myReplies.hashCode() : 0);
    result = 31 * result + (myParentCommentId != null ? myParentCommentId.hashCode() : 0);
    return result;
  }
}
