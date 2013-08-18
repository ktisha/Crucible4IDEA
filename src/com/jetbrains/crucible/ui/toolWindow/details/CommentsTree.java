package com.jetbrains.crucible.ui.toolWindow.details;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.tree.TreeUtil;
import com.jetbrains.crucible.actions.AddCommentAction;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import com.jetbrains.crucible.utils.CrucibleBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * User: ktisha
 */
public abstract class CommentsTree extends Tree {

  protected CommentsTree(@NotNull final Review review, @NotNull DefaultTreeModel model,
                       @Nullable final Editor editor, @Nullable final FilePath filePath) {
    super(model);
    setExpandableItemsEnabled(false);
    setRowHeight(0);
    setCellRenderer(new CommentNodeRenderer(this));

    final DefaultActionGroup group = new DefaultActionGroup();
    final AddCommentAction replyToComment = new AddCommentAction(review, editor, filePath,
                                                                 CrucibleBundle.message("crucible.add.reply"), true);
    replyToComment.setContextComponent(this);
    group.add(replyToComment);
    PopupHandler.installUnknownPopupHandler(this, group, ActionManager.getInstance());
    TreeUtil.expandAll(this);
  }

  protected static void addReplies(@NotNull Comment comment, @NotNull DefaultMutableTreeNode parentNode) {
    for (Comment reply : comment.getReplies()) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(reply);
      parentNode.add(node);
      addReplies(reply, node);
    }
  }

  @Nullable
  public Comment getSelectedComment() {
    Object selected = getLastSelectedPathComponent();
    if (selected == null) {
      return null;
    }
    assert selected instanceof DefaultMutableTreeNode;
    Object userObject = ((DefaultMutableTreeNode)selected).getUserObject();
    assert userObject instanceof Comment;
    return (Comment)userObject;
  }

}
