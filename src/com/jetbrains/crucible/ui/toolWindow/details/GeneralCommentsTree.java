package com.jetbrains.crucible.ui.toolWindow.details;

import com.intellij.openapi.project.Project;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * @author Kirill Likhodedov
 */
public class GeneralCommentsTree extends CommentsTree {

  public GeneralCommentsTree(@NotNull Project project, @NotNull Review review, @NotNull DefaultTreeModel model) {
    super(project, review, model, null, null);
  }

  @NotNull
  public static CommentsTree create(@NotNull Project project, @NotNull final Review review) {
    DefaultTreeModel model = createModel(review);
    GeneralCommentsTree tree = new GeneralCommentsTree(project, review, model);
    tree.setRootVisible(false);
    return tree;
  }

  private static DefaultTreeModel createModel(Review review) {
    DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
    DefaultTreeModel model = new DefaultTreeModel(rootNode);
    for (Comment comment : review.getGeneralComments()) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(comment);
      rootNode.add(node);
      addReplies(comment, node);
    }
    return model;
  }

  @Override
  public void refresh() {
    setModel(createModel(myReview));
  }
}
