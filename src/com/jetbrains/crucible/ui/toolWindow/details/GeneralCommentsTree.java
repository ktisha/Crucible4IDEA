package com.jetbrains.crucible.ui.toolWindow.details;

import com.intellij.openapi.project.Project;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

/**
 * @author Kirill Likhodedov
 */
public class GeneralCommentsTree extends CommentsTree {

  public GeneralCommentsTree(@NotNull Project project, @NotNull Review review) {
    super(project, review, null, null);
  }

  @NotNull
  public static CommentsTree create(@NotNull Project project, @NotNull final Review review) {
    GeneralCommentsTree tree = new GeneralCommentsTree(project, review);
    tree.setModel(tree.createModel());
    tree.setRootVisible(false);
    return tree;
  }

  @Override
  public void refresh(@NotNull Comment comment) {
    reloadModel(comment);
  }

  @Override
  protected TreeModel createModel() {
    DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
    DefaultTreeModel model = new DefaultTreeModel(rootNode);
    for (Comment comment : myReview.getGeneralComments()) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(comment);
      rootNode.add(node);
      addReplies(comment, node);
    }
    return model;
  }

}
