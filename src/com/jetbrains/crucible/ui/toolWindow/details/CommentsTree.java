package com.jetbrains.crucible.ui.toolWindow.details;

import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import com.intellij.util.ui.tree.TreeUtil;
import com.jetbrains.crucible.actions.AddCommentAction;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import com.jetbrains.crucible.ui.toolWindow.CrucibleTreeStructure;
import com.jetbrains.crucible.utils.CrucibleBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * User: ktisha
 */
public class CommentsTree extends SimpleTree {

  private static int ourBalloonWidth = 550;
  private static int ourBalloonHeight = 200;

  private CommentsTree(@NotNull final Review review, @NotNull SimpleNode root,
                       @Nullable final Editor editor, @Nullable final FilePath filePath) {

    setExpandableItemsEnabled(false);
    setRowHeight(0);
    setCellRenderer(new CommentNodeRenderer(this));

    final SimpleTreeStructure structure = new CrucibleTreeStructure(root);

    new AbstractTreeBuilder(this, getBuilderModel(), structure, null);
    invalidate();
    setPreferredSize(new Dimension(ourBalloonWidth, ourBalloonHeight));

    final DefaultActionGroup group = new DefaultActionGroup();
    final AddCommentAction replyToComment = new AddCommentAction(review, editor, filePath,
                                                                 CrucibleBundle.message("crucible.add.reply"), true);
    replyToComment.setContextComponent(this);
    group.add(replyToComment);
    PopupHandler.installUnknownPopupHandler(this, group, ActionManager.getInstance());
    TreeUtil.expandAll(this);
  }

  public static CommentsTree createForGeneralComments(@NotNull final Review review) {
    SimpleNode root = new SimpleNode() {
      @Override
      public SimpleNode[] getChildren() {
        return CommentNode.getNodesFromComments(review.getGeneralComments());
      }
    };

    CommentsTree tree = new CommentsTree(review, root, null, null);
    tree.setRootVisible(false);
    return tree;
  }

  public static CommentsTree createForComment(@NotNull Review review, @NotNull Comment comment,
                                              @NotNull Editor editor, @NotNull FilePath filePath) {
    return new CommentsTree(review, new CommentNode(comment), editor, filePath);
  }
}
