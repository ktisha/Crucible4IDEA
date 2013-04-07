package com.jetbrains.crucible.ui.toolWindow.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * User: ktisha
 */
public class CrucibleTreeModel extends DefaultTreeModel {

  public CrucibleTreeModel() {
    super(new DefaultMutableTreeNode(), false);
  }
}
