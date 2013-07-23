package com.jetbrains.crucible.ui.toolWindow.details;

import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.ui.JBDefaultTreeCellRenderer;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import com.jetbrains.crucible.actions.AddCommentAction;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import com.jetbrains.crucible.ui.toolWindow.CrucibleTreeStructure;
import com.jetbrains.crucible.utils.CrucibleBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * User: ktisha
 */
public class CommentsTree extends SimpleTree {

  private static int ourBalloonWidth = 550;
  private static int ourBalloonHeight = 200;

  public CommentsTree(@NotNull final Review review, @NotNull final Comment comment,
                      @NotNull final Editor editor, @NotNull final FilePath filePath) {
    final CommentNode root = new CommentNode(comment);
    setExpandableItemsEnabled(false);

    setCellRenderer(new JBDefaultTreeCellRenderer(this) {
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
        label.setText("<html>" + label.getText() + "</html>");
        setIcon(null);
        String[] lines = UIUtil.splitText(label.getText(), getFontMetrics(UIUtil.getLabelFont()), ourBalloonWidth, ',');
        Font f = label.getFont();
        FontMetrics metrics = label.getFontMetrics(f);
        int hgt = (metrics.getHeight() + 10) * lines.length;
        Dimension size = new Dimension(ourBalloonWidth - 50, hgt);
        label.setPreferredSize(size);
        label.setBorder(BorderFactory.createEtchedBorder());
        label.setVerticalAlignment(TOP);
        return label;
      }
    });
    addComponentListener(new ComponentListener() {
      @Override
      public void componentResized(ComponentEvent e) {
        ourBalloonHeight = e.getComponent().getHeight();
        ourBalloonWidth = e.getComponent().getWidth();
        TreeUtil.collapseAll(CommentsTree.this, -1);
        TreeUtil.expandAll(CommentsTree.this);
      }

      @Override
      public void componentMoved(ComponentEvent e) {
      }

      @Override
      public void componentShown(ComponentEvent e) {
      }

      @Override
      public void componentHidden(ComponentEvent e) {
      }
    });
    final SimpleTreeStructure structure = new CrucibleTreeStructure(root);

    new AbstractTreeBuilder(this, getBuilderModel(), structure, null);
    invalidate();
    setPreferredSize(new Dimension(ourBalloonWidth, ourBalloonHeight));

    final DefaultActionGroup group = new DefaultActionGroup();
    final AddCommentAction replyToComment =
      new AddCommentAction(review, editor, filePath, CrucibleBundle.message("crucible.add.reply"),true);
    replyToComment.setContextComponent(this);
    group.add(replyToComment);
    PopupHandler.installUnknownPopupHandler(this, group, ActionManager.getInstance());
    TreeUtil.expandAll(this);
  }
}
