package com.jetbrains.crucible.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
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
import com.jetbrains.crucible.ui.toolWindow.details.CommentBalloonBuilder;
import com.jetbrains.crucible.ui.toolWindow.details.CommentForm;
import com.jetbrains.crucible.ui.toolWindow.details.CommentsTree;
import com.jetbrains.crucible.ui.toolWindow.details.CommentNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * User: ktisha
 * <p/>
 * Reply to comment
 */
@SuppressWarnings("ComponentNotRegistered")
public class ReplyToCommentAction extends AnActionButton implements DumbAware {

  private final Editor myEditor;
  private final VirtualFile myVFile;
  private final String myName;
  private final Review myReview;

  public ReplyToCommentAction(@NotNull final Review review, @Nullable final Editor editor,
                              @Nullable final VirtualFile vFile, @NotNull final String description,
                              @NotNull final String name) {
    super(description, description, PlatformIcons.EDIT_IN_SECTION_ICON);
    myReview = review;
    myEditor = editor;
    myVFile = vFile;
    myName = name;
  }

  public void actionPerformed(AnActionEvent e) {
    final Project project = e.getData(PlatformDataKeys.PROJECT);
    if (project == null) return;
    final ToolWindow toolWindow = e.getData(PlatformDataKeys.TOOL_WINDOW);
    if (toolWindow != null) {
      addReplyToGeneral(project, toolWindow);
    }
    else {
      addReplyToVersionedComment(project);
    }
  }

  private void addReplyToGeneral(@NotNull final Project project, @NotNull final ToolWindow toolWindow) {
    final CommentBalloonBuilder builder = new CommentBalloonBuilder();
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
    final Balloon balloon = builder.getNewCommentBalloon(replyForm);
    replyForm.setBalloon(balloon);
    balloon.showInCenterOf(toolWindow.getComponent());
    replyForm.requestFocus();
  }

  private void addReplyToVersionedComment(@NotNull final Project project) {
    if (myEditor == null || myVFile == null) return;
    final CommentBalloonBuilder builder = new CommentBalloonBuilder();
    final CommentForm replyForm = new CommentForm(project, myName, false);
    replyForm.setReview(myReview);

    final CommentsTree contextComponent = (CommentsTree)getContextComponent();
    final Object selected = contextComponent.getLastSelectedPathComponent();
    if (selected instanceof DefaultMutableTreeNode) {
      Object userObject = ((DefaultMutableTreeNode)selected).getUserObject();
      if (userObject instanceof CommentNode) {
        final Comment comment = ((CommentNode)userObject).getComment();
        replyForm.setParentCommentId(comment.getPermId());
      }
    }
    replyForm.setEditor(myEditor);
    replyForm.setVirtualFile(myVFile);
    final Balloon balloon = builder.getNewCommentBalloon(replyForm);
    replyForm.setBalloon(balloon);

    balloon.showInCenterOf(contextComponent);
    replyForm.requestFocus();
  }
}
