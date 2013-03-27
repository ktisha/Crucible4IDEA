package com.jetbrains.crucible.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.awt.RelativePoint;
import com.jetbrains.crucible.ui.ReviewBalloonBuilder;
import com.jetbrains.crucible.ui.toolWindow.CommentForm;

import java.awt.*;

/**
 * User: ktisha
 * <p/>
 * Add comment
 */
@SuppressWarnings("ComponentNotRegistered")
public class AddCommentAction extends AnAction implements DumbAware {

  private final String myName;

  public AddCommentAction(String s, String name) {
    super(s, s, IconLoader.getIcon("/images/note.png"));
    myName = name;
  }
  public void actionPerformed(AnActionEvent e) {
    Project project = e.getData(PlatformDataKeys.PROJECT);
    if (project == null) return;
    final ToolWindow toolWindow = e.getData(PlatformDataKeys.TOOL_WINDOW);
    if (toolWindow != null) {
      ReviewBalloonBuilder builder = new ReviewBalloonBuilder();
      final CommentForm commentForm = new CommentForm(project, myName);
      final Balloon balloon = builder.getCommentBalloon(commentForm);
      commentForm.setBalloon(balloon);
      balloon.showInCenterOf(toolWindow.getComponent());
      commentForm.requestFocus();
    }
    else {
      final Editor editor = e.getData(PlatformDataKeys.EDITOR);
      addCommentToFile(editor, project);
    }
  }

  private void addCommentToFile(Editor editor, Project project) {
    ReviewBalloonBuilder builder = new ReviewBalloonBuilder();
    final CommentForm commentForm = new CommentForm(project, myName);
    final Balloon balloon = builder.getCommentBalloon(commentForm);
    commentForm.setBalloon(balloon);
    final Point targetPoint = editor.visualPositionToXY(editor.getCaretModel().getVisualPosition());
    balloon.show(new RelativePoint(editor.getContentComponent(), targetPoint), Balloon.Position.below);
    commentForm.requestFocus();
  }
}
