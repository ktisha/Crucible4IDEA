package com.jetbrains.crucible.ui.toolWindow.tree;

import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.jetbrains.crucible.connection.CrucibleFilter;
import com.jetbrains.crucible.ui.toolWindow.CrucibleReviewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * User : ktisha
 */
public class CrucibleRequireApprovalNode extends SimpleNode {
  private static final String NAME = "Require My Approval";
  private final CrucibleReviewModel myReviewModel;
  private List<SimpleNode> myChildren = new ArrayList<SimpleNode>();

  public CrucibleRequireApprovalNode(CrucibleReviewModel reviewModel) {
    myReviewModel = reviewModel;
  }

  public String toString() {
    return NAME;
  }

  @Override
  public SimpleNode[] getChildren() {
    return new SimpleNode[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean isAlwaysLeaf() {
    return true;
  }


  @Override
  public void handleSelection(SimpleTree tree) {
    super.handleSelection(tree);
    myReviewModel.updateModel(CrucibleFilter.RequireApprovalReview);
  }
}
