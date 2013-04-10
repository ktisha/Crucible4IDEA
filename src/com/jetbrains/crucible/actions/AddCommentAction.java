package com.jetbrains.crucible.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupAdapter;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.awt.RelativePoint;
import com.jetbrains.crucible.model.Review;
import com.jetbrains.crucible.ui.toolWindow.details.CommentBalloonBuilder;
import com.jetbrains.crucible.ui.toolWindow.details.CommentForm;
import com.jetbrains.crucible.ui.toolWindow.details.CommentsTreeTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * User: ktisha
 * <p/>
 * Add comment
 */
@SuppressWarnings("ComponentNotRegistered")
public class AddCommentAction extends AnActionButton implements DumbAware {

  private final String myName;
  private final VirtualFile myVirtualFile;
  private final Review myReview;

  public AddCommentAction(@NotNull final String s, @NotNull final String name,
                          @Nullable final VirtualFile virtualFile,
                          @NotNull final Review review) {
    super(s, s, IconLoader.getIcon("/images/comment.png"));
    myName = name;
    myVirtualFile = virtualFile;
    myReview = review;
  }

  public void actionPerformed(AnActionEvent e) {
    Project project = e.getData(PlatformDataKeys.PROJECT);
    if (project == null) return;
    final ToolWindow toolWindow = e.getData(PlatformDataKeys.TOOL_WINDOW);
    if (toolWindow != null) {
      final CommentBalloonBuilder builder = new CommentBalloonBuilder();
      final CommentForm commentForm = new CommentForm(project, myName, true);
      commentForm.setReview(myReview);
      final Balloon balloon = builder.getNewCommentBalloon(commentForm);
      commentForm.setBalloon(balloon);
      balloon.addListener(new JBPopupAdapter() {
        @Override
        public void onClosed(LightweightWindowEvent event) {
          final JComponent component = getContextComponent();
          if (component instanceof CommentsTreeTable) {
            ((CommentsTreeTable)component).updateModel(commentForm.getReview());
          }
        }
      });
      balloon.showInCenterOf(toolWindow.getComponent());
      commentForm.requestFocus();
    }
    else {
      final Editor editor = e.getData(PlatformDataKeys.EDITOR);
      if (editor != null)
        addCommentToFile(editor, project);
    }
  }

  private void addCommentToFile(@NotNull final Editor editor, @NotNull final Project project) {
    if (myVirtualFile == null) return;
    final CommentBalloonBuilder builder = new CommentBalloonBuilder();
    final CommentForm commentForm = new CommentForm(project, myName, false);
    commentForm.setEditor(editor);
    commentForm.setVirtualFile(myVirtualFile);
    commentForm.setReview(myReview);
    final Balloon balloon = builder.getNewCommentBalloon(commentForm);
    commentForm.setBalloon(balloon);
    final Point targetPoint = editor.visualPositionToXY(editor.getCaretModel().getVisualPosition());
    balloon.show(new RelativePoint(editor.getContentComponent(), targetPoint), Balloon.Position.below);
    commentForm.requestFocus();
  }
}
