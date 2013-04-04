package com.jetbrains.crucible.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.table.JBTable;
import com.intellij.util.PlatformIcons;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import com.jetbrains.crucible.ui.CommentForm;
import com.jetbrains.crucible.ui.ReviewBalloonBuilder;

/**
 * User: ktisha
 * <p/>
 * Reply to comment
 */
@SuppressWarnings("ComponentNotRegistered")
public class ReplyToCommentAction extends AnActionButton implements DumbAware {

  private final String myName;
  private VirtualFile myVirtualFile;
  private Review myReview;

  public ReplyToCommentAction(Review review, String s, String name) {
    super(s, s, PlatformIcons.EDIT_IN_SECTION_ICON);
    myReview = review;
    myName = name;
  }

  public void actionPerformed(AnActionEvent e) {
    Project project = e.getData(PlatformDataKeys.PROJECT);
    if (project == null) return;
    final ToolWindow toolWindow = e.getData(PlatformDataKeys.TOOL_WINDOW);
    if (toolWindow != null) {
      ReviewBalloonBuilder builder = new ReviewBalloonBuilder();
      final CommentForm replyForm = new CommentForm(project, myName, true);
      replyForm.setReview(myReview);

      final JBTable contextComponent = (JBTable)getContextComponent();
      final int selectedRow = contextComponent.getSelectedRow();
      if (selectedRow >= 0) {
        final Object parentComment = contextComponent.getValueAt(selectedRow, 0);
        if (parentComment instanceof Comment) {
          replyForm.setParentCommentId(((Comment)parentComment).getPermId());
        }
      }
      final Balloon balloon = builder.getCommentBalloon(replyForm);
      replyForm.setBalloon(balloon);
      balloon.showInCenterOf(toolWindow.getComponent());
      replyForm.requestFocus();
    }
  }
}
