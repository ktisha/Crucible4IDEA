package com.jetbrains.crucible.ui.toolWindow.tree;

import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.jetbrains.crucible.model.CrucibleFilter;
import com.jetbrains.crucible.ui.toolWindow.CrucibleReviewModel;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Dmitry on 14.12.2014.
 */
public class CrucibleFilterNode extends SimpleNode {

  private final CrucibleReviewModel myReviewModel;
  private final CrucibleFilter myFilter;

  public CrucibleFilterNode(@NotNull final CrucibleReviewModel reviewModel, CrucibleFilter filter) {
    myReviewModel = reviewModel;
    myFilter = filter;
  }

  @NotNull
  public String toString() {
    return myFilter.getFilterName();
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
    myReviewModel.updateModel(myFilter);
  }
}