package com.jetbrains.crucible.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupAdapter;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.table.JBTable;
import com.intellij.util.PlatformIcons;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import com.jetbrains.crucible.ui.toolWindow.details.*;
import com.jetbrains.crucible.ui.toolWindow.diff.ReviewGutterIconRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.*;

/**
 * User: ktisha
 * <p/>
 * Reply to comment
 */
@SuppressWarnings("ComponentNotRegistered")
public class AddCommentAction extends AnActionButton implements DumbAware {

  private final Editor myEditor;
  private final VirtualFile myVirtualFile;
  private final String myName;
  private final Review myReview;
  private final boolean myIsReply;

  public AddCommentAction(@NotNull final Review review, @Nullable final Editor editor,
                          @Nullable final VirtualFile vFile, @NotNull final String description,
                          @NotNull final String name, boolean isReply) {
    super(description, description, PlatformIcons.EDIT_IN_SECTION_ICON);
    myIsReply = isReply;
    myReview = review;
    myEditor = editor;
    myVirtualFile = vFile;
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
    final CommentForm commentForm = new CommentForm(project, myName, true, myIsReply);
    commentForm.setReview(myReview);

    if (myIsReply) {
      final JBTable contextComponent = (JBTable)getContextComponent();
      final int selectedRow = contextComponent.getSelectedRow();
      if (selectedRow >= 0) {
        final Object parentComment = contextComponent.getValueAt(selectedRow, 0);
        if (parentComment instanceof Comment) {
          commentForm.setParentComment(((Comment)parentComment));
        }
      }
    }

    final Balloon balloon = builder.getNewCommentBalloon(commentForm);
    balloon.addListener(new JBPopupAdapter() {
      @Override
      public void onClosed(LightweightWindowEvent event) {
        final JComponent component = getContextComponent();
        if (component instanceof CommentsTreeTable) {
          ((CommentsTreeTable)component).updateModel(commentForm.getReview());
        }
      }
    });
    commentForm.setBalloon(balloon);
    balloon.showInCenterOf(toolWindow.getComponent());
    commentForm.requestFocus();
  }

  private void addReplyToVersionedComment(@NotNull final Project project) {
    if (myEditor == null || myVirtualFile == null) return;
    final CommentBalloonBuilder builder = new CommentBalloonBuilder();
    final CommentForm commentForm = new CommentForm(project, myName, false, myIsReply);
    commentForm.setReview(myReview);

    final JComponent contextComponent = getContextComponent();
    if (myIsReply && contextComponent instanceof CommentsTree) {
      final Object selected = ((CommentsTree)contextComponent).getLastSelectedPathComponent();
      if (selected instanceof DefaultMutableTreeNode) {
        Object userObject = ((DefaultMutableTreeNode)selected).getUserObject();
        if (userObject instanceof CommentNode) {
          final Comment comment = ((CommentNode)userObject).getComment();
          commentForm.setParentComment(comment);
        }
      }
    }
    commentForm.setEditor(myEditor);
    commentForm.setVirtualFile(myVirtualFile);
    final Balloon balloon = builder.getNewCommentBalloon(commentForm);
    balloon.addListener(new JBPopupAdapter() {
      @Override
      public void onClosed(LightweightWindowEvent event) {
        if (!myIsReply) {
          final MarkupModel markup = myEditor.getMarkupModel();
          final Comment comment = commentForm.getComment();
          if (comment != null) {
            final RangeHighlighter highlighter = markup.addLineHighlighter(Integer.parseInt(comment.getLine()),
                                                                           HighlighterLayer.ERROR + 1, null);

            final ReviewGutterIconRenderer gutterIconRenderer =
              new ReviewGutterIconRenderer(myReview, myVirtualFile, comment);
            highlighter.setGutterIconRenderer(gutterIconRenderer);
          }
        }
        else {
          final JComponent contextComponent = getContextComponent();
          if (contextComponent instanceof CommentsTree) {
            final TreePath selectionPath = ((JTree)contextComponent).getSelectionPath();
            final Object component = selectionPath.getLastPathComponent();
            if (component instanceof DefaultMutableTreeNode){
              ((DefaultMutableTreeNode)component).add(
                new DefaultMutableTreeNode(new CommentNode(commentForm.getComment())));
              final TreeModel model = ((JTree)contextComponent).getModel();
              if (model instanceof DefaultTreeModel) {
                ((DefaultTreeModel)model).reload();
              }
            }
          }
        }
      }
    });
    commentForm.setBalloon(balloon);

    balloon.showInCenterOf(contextComponent);
    commentForm.requestFocus();
  }
}
