package com.jetbrains.crucible.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.awt.RelativePoint;
import com.jetbrains.crucible.model.Review;
import com.jetbrains.crucible.ui.ReviewBalloonBuilder;
import com.jetbrains.crucible.ui.CommentForm;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * User: ktisha
 * <p/>
 * Add comment
 */
@SuppressWarnings("ComponentNotRegistered")
public class AddCommentAction extends AnActionButton implements DumbAware {

  private final String myName;
  private VirtualFile myVirtualFile;
  private Review myReview;

  public AddCommentAction(String s, String name) {
    super(s, s, IconLoader.getIcon("/images/comment.png"));
    myName = name;
  }

  public void actionPerformed(AnActionEvent e) {
    Project project = e.getData(PlatformDataKeys.PROJECT);
    if (project == null) return;
    final ToolWindow toolWindow = e.getData(PlatformDataKeys.TOOL_WINDOW);
    if (toolWindow != null) {
      ReviewBalloonBuilder builder = new ReviewBalloonBuilder();
      final CommentForm commentForm = new CommentForm(project, myName, true);
      commentForm.setReview(myReview);
      final Balloon balloon = builder.getCommentBalloon(commentForm);
      commentForm.setBalloon(balloon);
      balloon.showInCenterOf(toolWindow.getComponent());
      commentForm.requestFocus();
    }
    else {
      final Editor editor = e.getData(PlatformDataKeys.EDITOR);
      if (editor != null)
        addCommentToFile(editor, project);
    }
  }

  private void addCommentToFile(@NotNull Editor editor, Project project) {
    ReviewBalloonBuilder builder = new ReviewBalloonBuilder();
    final CommentForm commentForm = new CommentForm(project, myName, false);
    commentForm.setEditor(editor);
    commentForm.setVirtualFile(myVirtualFile);
    commentForm.setReview(myReview);
    final Balloon balloon = builder.getCommentBalloon(commentForm);
    commentForm.setBalloon(balloon);
    final Point targetPoint = editor.visualPositionToXY(editor.getCaretModel().getVisualPosition());
    balloon.show(new RelativePoint(editor.getContentComponent(), targetPoint), Balloon.Position.below);
    commentForm.requestFocus();
  }

  public void setVirtualFile(VirtualFile virtualFile) {
    myVirtualFile = virtualFile;
  }

  public void setReview(Review review) {
    myReview = review;
  }
}
