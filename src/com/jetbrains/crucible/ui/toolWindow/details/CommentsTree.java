package com.jetbrains.crucible.ui.toolWindow.details;

import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import com.jetbrains.crucible.actions.AddCommentAction;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import com.jetbrains.crucible.ui.toolWindow.CrucibleTreeStructure;
import com.jetbrains.crucible.utils.CrucibleBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * User: ktisha
 */
public class CommentsTree extends SimpleTree {

  private static final int ourBalloonWidth = 450;
  private static final int ourBalloonHeight = 400;

  public CommentsTree(@NotNull final Review review, @NotNull final Comment comment,
                      @NotNull final Editor editor, @NotNull final VirtualFile vFile) {
    final CommentNode root = new CommentNode(comment);
    setExpandableItemsEnabled(false);
    setCellRenderer(new DefaultTreeCellRenderer() {
      @Override
      public Component getTreeCellRendererComponent(JTree tree,
                                                    Object value,
                                                    boolean sel,
                                                    boolean expanded,
                                                    boolean leaf,
                                                    int row,
                                                    boolean hasFocus) {
        final JLabel label = (JLabel)super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        label.setToolTipText(label.getText());
        setIcon(null);
        return label;
      }
    });
    final SimpleTreeStructure structure = new CrucibleTreeStructure(root);

    new AbstractTreeBuilder(this, getBuilderModel(), structure, null);
    invalidate();
    setPreferredSize(new Dimension(ourBalloonWidth, ourBalloonHeight));

    final DefaultActionGroup group = new DefaultActionGroup();
    final AddCommentAction replyToComment =
      new AddCommentAction(review, editor, vFile, CrucibleBundle.message("crucible.add.reply"),true);
    replyToComment.setContextComponent(this);
    group.add(replyToComment);
    PopupHandler.installUnknownPopupHandler(this, group, ActionManager.getInstance());
  }
}
