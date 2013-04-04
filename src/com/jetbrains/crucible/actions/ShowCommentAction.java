package com.jetbrains.crucible.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.ui.ReviewBalloonBuilder;
import com.jetbrains.crucible.ui.ReviewForm;

/**
 * User: ktisha
 * <p/>
 * Show comments for file
 */
@SuppressWarnings("ComponentNotRegistered")
public class ShowCommentAction extends AnAction implements DumbAware {
  private final Comment myComment;

  public ShowCommentAction (Comment comment) {
    myComment = comment;
  }

  public void actionPerformed(AnActionEvent e) {
    Project project = e.getData(PlatformDataKeys.PROJECT);
    if (project == null) return;

    final Editor editor = PlatformDataKeys.EDITOR.getData(e.getDataContext());
    if (editor == null) return;

    final ReviewForm reviewForm = new ReviewForm(myComment, editor.getProject(), false);
    final ReviewBalloonBuilder reviewBalloonBuilder = new ReviewBalloonBuilder();
    reviewBalloonBuilder.showBalloon(myComment, editor, reviewForm, myComment.getAuthor().getUserName());
  }
}
