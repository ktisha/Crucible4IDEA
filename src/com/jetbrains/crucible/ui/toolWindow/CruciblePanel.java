package com.jetbrains.crucible.ui.toolWindow;

import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import com.jetbrains.crucible.ui.toolWindow.tree.CrucibleRootNode;
import com.jetbrains.crucible.ui.toolWindow.tree.CrucibleTreeModel;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

/**
 * User: ktisha
 * <p/>
 * Main code review panel
 */
public class CruciblePanel extends SimpleToolWindowPanel {
  private final Project myProject;
  private final CrucibleReviewModel myReviewModel;
  private SimpleTree myReviewTree;
  private JPanel myMainPanel;
  private JSplitPane mySplitter;
  private JList myReviewList;

  public CruciblePanel(Project project) {
    super(false);
    myProject = project;

    myReviewModel = new CrucibleReviewModel(project);
    myReviewList.setModel(myReviewModel);

    SimpleTreeStructure reviewTreeStructure = createTreeStructure();
    final DefaultTreeModel model = new CrucibleTreeModel(project);
    myReviewTree = new SimpleTree(model);
    myReviewTree.setPreferredSize(new Dimension(200, 200));

    AbstractTreeBuilder reviewTreeBuilder =
      new AbstractTreeBuilder(myReviewTree, model, reviewTreeStructure, null);
    myReviewTree.invalidate();

    JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(myReviewTree);
    scrollPane.setPreferredSize(new Dimension(250, 250));
    mySplitter.setLeftComponent(scrollPane);
    setContent(mySplitter);
  }


  private SimpleTreeStructure createTreeStructure() {
    final VirtualFile virtualFile = myProject.getBaseDir();
    final CrucibleRootNode rootNode = new CrucibleRootNode(myReviewModel);
    return new CrucibleTreeStructure(myProject, rootNode);
  }
}
