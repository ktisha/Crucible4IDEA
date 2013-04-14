package com.jetbrains.crucible.ui.toolWindow.details;

import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.tree.TreeUtil;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import com.jetbrains.crucible.model.User;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.util.*;

/**
 * User: ktisha
 */
public class CommentsTreeTable extends TreeTable {

  public CommentsTreeTable() {
    super(new ListTreeTableModel(new DefaultMutableTreeNode(), new ColumnInfo[]{}));
    updateView();
  }

  private void updateView() {
    final TreeTableTree tree = getTree();
    tree.setShowsRootHandles(true);
    tree.setCellRenderer(new MyTreeCellRenderer());

    TreeUtil.expandAll(tree);
    setRootVisible(false);

    setStriped(true);
  }

  public void updateModel(@NotNull final Review review) {
    final CommentTreeNode root = updateCommentsTree(review);
    ListTreeTableModel commentsModel = new ListTreeTableModel(root, new ColumnInfo[]{COMMENT_COLUMN, AUTHOR_COLUMN, DATE_COLUMN });
    setModel(commentsModel);
    updateView();
    DetailsPanel.setUpColumnWidths(this);
  }


  public static CommentTreeNode updateCommentsTree(Review review) {
    final java.util.List<Comment> comments = review.getGeneralComments();
    final CommentTreeNode root = new CommentTreeNode(new Comment(new User("Root"), "Root message"));
    for (Comment comment : comments) {
      final CommentTreeNode commentNode = createNode(comment);
      root.add(commentNode);
    }
    return root;
  }


  private static CommentTreeNode createNode(@NotNull final Comment comment) {
    final CommentTreeNode commentNode = new CommentTreeNode(comment);
    for (Comment c : comment.getReplies()) {
      final CommentTreeNode node = createNode(c);
      commentNode.add(node);
    }
    return commentNode;
  }

  private static class MyTreeCellRenderer extends JLabel implements TreeCellRenderer {
    public Component getTreeCellRendererComponent(final JTree tree,
                                                  final Object value,
                                                  final boolean selected,
                                                  final boolean expanded,
                                                  final boolean leaf,
                                                  final int row,
                                                  final boolean hasFocus) {

      if (value instanceof CommentTreeNode) {
        final CommentTreeNode node = (CommentTreeNode)value;
        final Comment comment = node.getComment();
        setText(comment.getMessage());
        setToolTipText(comment.getMessage());
        setOpaque(true);
        Color background = tree.getBackground();
        setBackground(background);
      }
      setIcon(null);
      return this;
    }
  }


  private static final ColumnInfo<CommentTreeNode, Comment> COMMENT_COLUMN = new ColumnInfo<CommentTreeNode, Comment>("Message"){
    public Comment valueOf(final CommentTreeNode node) {
      return node.getComment();
    }

    @Override
    public Class getColumnClass() {
      return TreeTableModel.class;
    }
  };

  private static final ColumnInfo<CommentTreeNode, Date> DATE_COLUMN = new ColumnInfo<CommentTreeNode, Date>("Date"){
    public Date valueOf(final CommentTreeNode object) {
      Comment comment = object.getComment();
      return comment.getCreateDate();
    }

    public final Class getColumnClass() {
      return Date.class;
    }
  };

  private static final ColumnInfo<CommentTreeNode, String> AUTHOR_COLUMN = new ColumnInfo<CommentTreeNode, String>("Author"){
    public String valueOf(final CommentTreeNode object) {
      Comment comment = object.getComment();
      return comment != null ? comment.getAuthor().getUserName() : "";
    }

    public final Class getColumnClass() {
      return String.class;
    }
  };
}
