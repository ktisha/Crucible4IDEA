package com.jetbrains.crucible.ui.toolWindow.details;

import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * @author Kirill Likhodedov
 */
public class GeneralCommentsTree extends CommentsTree {

  public GeneralCommentsTree(Review review, DefaultTreeModel model) {
    super(review, model, null, null);
  }

  @NotNull
  public static CommentsTree create(@NotNull final Review review) {
    DefaultTreeModel model = createModel(review);

    GeneralCommentsTree tree = new GeneralCommentsTree(review, model);
    tree.setRootVisible(false);
    return tree;
  }

  @NotNull
  private static DefaultTreeModel createModel(@NotNull Review review) {
    DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
    DefaultTreeModel model = new DefaultTreeModel(rootNode);
    for (Comment comment : review.getGeneralComments()) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(comment);
      rootNode.add(node);
      addReplies(comment, node);
    }
    return model;
  }

  public void updateModel(@NotNull Review review) {
    setModel(createModel(review));
  }

}
