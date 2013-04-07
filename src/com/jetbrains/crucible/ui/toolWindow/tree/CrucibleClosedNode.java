package com.jetbrains.crucible.ui.toolWindow.tree;

import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.jetbrains.crucible.model.CrucibleFilter;
import com.jetbrains.crucible.ui.toolWindow.CrucibleReviewModel;
import org.jetbrains.annotations.NotNull;

/**
 * User : ktisha
 */
public class CrucibleClosedNode extends SimpleNode {
  private static final String NAME = "Closed";
  private final CrucibleReviewModel myReviewModel;

  public CrucibleClosedNode(@NotNull final CrucibleReviewModel reviewModel) {
    myReviewModel = reviewModel;
  }

  @NotNull
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
    myReviewModel.updateModel(CrucibleFilter.Closed);
  }
}
