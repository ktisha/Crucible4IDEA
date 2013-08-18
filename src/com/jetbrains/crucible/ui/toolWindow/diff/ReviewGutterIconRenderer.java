package com.jetbrains.crucible.ui.toolWindow.diff;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vcs.FilePath;
import com.jetbrains.crucible.actions.ShowFileCommentsAction;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User: ktisha
 */
public class ReviewGutterIconRenderer extends GutterIconRenderer {

  private static final Icon COMMENT_ICON = IconLoader.getIcon("/images/comment.png");
  private static final Icon DRAFT_COMMENT_ICON = IconLoader.getIcon("/images/comment_draft.png");

  private final Review myReview;
  private final Comment myComment;
  private final FilePath myFilePath;

  public ReviewGutterIconRenderer(@NotNull final Review review, @NotNull final FilePath filePath, @NotNull final Comment comment) {
    myReview = review;
    myFilePath = filePath;
    myComment = comment;
  }
  @NotNull
  @Override
  public Icon getIcon() {
    return myComment.isDraft() ? DRAFT_COMMENT_ICON : COMMENT_ICON;
  }

  @Override
  public boolean isNavigateAction() {
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ReviewGutterIconRenderer that = (ReviewGutterIconRenderer) o;
    return getIcon().equals(that.getIcon());
  }

  @Override
  public AnAction getClickAction() {
    return new ShowFileCommentsAction(myComment, myFilePath, myReview);
  }

  @Override
  public String getTooltipText() {
    return myComment.getAuthor().getUserName();
  }

  @Override
  public int hashCode() {
    return getIcon().hashCode();
  }
}
