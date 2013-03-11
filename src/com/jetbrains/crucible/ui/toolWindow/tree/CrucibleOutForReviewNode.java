package com.jetbrains.crucible.ui.toolWindow.tree;

import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.jetbrains.crucible.connection.CrucibleFilter;
import com.jetbrains.crucible.ui.toolWindow.CrucibleReviewModel;

/**
 * User : ktisha
 */
public class CrucibleOutForReviewNode extends SimpleNode {
  private static final String NAME = "Out For Review";
  private final CrucibleReviewModel myReviewModel;

  public CrucibleOutForReviewNode(CrucibleReviewModel reviewModel) {
    myReviewModel = reviewModel;
  }

  public String toString() {
    return NAME;
  }

  @Override
  public SimpleNode[] getChildren() {
    return new SimpleNode[0];
  }

  @Override
  public boolean isAlwaysLeaf() {
    return true;
  }

  @Override
  public void handleSelection(SimpleTree tree) {
    super.handleSelection(tree);
    myReviewModel.updateModel(CrucibleFilter.OutForReview);
  }
}
