package com.jetbrains.crucible.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.wm.ToolWindow;
import com.jetbrains.crucible.ui.ReviewBalloonBuilder;
import com.jetbrains.crucible.ui.toolWindow.CommentForm;

/**
 * User: ktisha
 * <p/>
 * Add comment
 */
@SuppressWarnings("ComponentNotRegistered")
public class AddCommentAction extends AnAction implements DumbAware {

  private final String myReviewId;

  public AddCommentAction(String s, String reviewId) {
    super(s);
    myReviewId = reviewId;
  }

  public void actionPerformed(AnActionEvent e) {
    Project project = e.getData(PlatformDataKeys.PROJECT);
    if (project == null) return;
    final ToolWindow toolWindow = e.getData(PlatformDataKeys.TOOL_WINDOW);
    if (toolWindow == null) return;
    ReviewBalloonBuilder builder = new ReviewBalloonBuilder();
    final CommentForm commentForm = new CommentForm(project, myReviewId);
    final Balloon balloon = builder.getCommentBalloon(commentForm/*, myReviewId, project*/);
    balloon.showInCenterOf(toolWindow.getComponent());
    commentForm.requestFocus();
  }
}
