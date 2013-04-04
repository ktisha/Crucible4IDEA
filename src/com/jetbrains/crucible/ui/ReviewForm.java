package com.jetbrains.crucible.ui;

import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import com.jetbrains.crucible.actions.ReplyToCommentAction;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import com.jetbrains.crucible.ui.toolWindow.CrucibleTreeStructure;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: ktisha
 */
public class ReviewForm extends JTree {

  private static final int ourBalloonWidth = 400;
  private static final int ourBalloonHeight = 400;

  public ReviewForm(Review review, final Comment comment, final Project project, boolean editable) {
    final CommentNode root = new CommentNode(comment);
    SimpleTreeStructure structure = new CrucibleTreeStructure(project, root);
    final DefaultTreeModel model = new DefaultTreeModel(new DefaultMutableTreeNode());
    AbstractTreeBuilder reviewTreeBuilder =
      new AbstractTreeBuilder(this, model, structure, null);
    invalidate();

    setPreferredSize(new Dimension(ourBalloonWidth, ourBalloonHeight));

    DefaultActionGroup group = new DefaultActionGroup();
    final ReplyToCommentAction addCommentAction = new ReplyToCommentAction(review, "Add Reply", "Comment");
    addCommentAction.setContextComponent(this);
    group.add(addCommentAction);
    PopupHandler.installUnknownPopupHandler(this, group, ActionManager.getInstance());
  }

  public class CommentNode extends SimpleNode {
    private final Comment myComment;

    CommentNode(Comment comment) {
      myComment = comment;
    }

    public Comment getComment() {
      return myComment;
    }

    @Override
    public String getName() {
      return myComment.getAuthor() + " : " + myComment.getMessage();
    }

    @Override
    public SimpleNode[] getChildren() {
      final List<Comment> replies = myComment.getReplies();
      final List<SimpleNode> children = new ArrayList<SimpleNode>();
      for (Comment reply : replies) {
        children.add(new CommentNode(reply));
      }
      return children.toArray(new SimpleNode[children.size()]);
    }
  }
}
