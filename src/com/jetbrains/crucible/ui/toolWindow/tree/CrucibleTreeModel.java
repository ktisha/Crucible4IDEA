package com.jetbrains.crucible.ui.toolWindow.tree;

import com.intellij.openapi.project.Project;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * User: ktisha
 */
public class CrucibleTreeModel extends DefaultTreeModel {
  private Project myProject;

  public CrucibleTreeModel(Project project) {
    super(new DefaultMutableTreeNode(), false);
    this.myProject = project;
  }
}
