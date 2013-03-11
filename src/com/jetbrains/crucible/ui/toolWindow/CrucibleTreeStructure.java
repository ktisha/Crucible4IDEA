package com.jetbrains.crucible.ui.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTreeStructure;

/**
 * User: ktisha
 */

public class CrucibleTreeStructure extends SimpleTreeStructure {
  private SimpleNode myRootElement;
  private Project myProject;

  public CrucibleTreeStructure(Project project, SimpleNode root) {
    super();
    myProject = project;
    myRootElement = root;
  }

  public SimpleNode getRootElement() {
    return myRootElement;
  }
}
