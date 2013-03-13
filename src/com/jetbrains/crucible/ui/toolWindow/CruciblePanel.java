package com.jetbrains.crucible.ui.toolWindow;

import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import com.jetbrains.crucible.connection.CrucibleManager;
import com.jetbrains.crucible.connection.exceptions.CrucibleApiException;
import com.jetbrains.crucible.model.Review;
import com.jetbrains.crucible.ui.toolWindow.tree.CrucibleRootNode;
import com.jetbrains.crucible.ui.toolWindow.tree.CrucibleTreeModel;
import org.jdom.JDOMException;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
  private JPanel myReviewPanel;
  private JBTable myReviewTable;

  public CruciblePanel(Project project) {
    super(false);
    myProject = project;

    myReviewModel = new CrucibleReviewModel(project);
    myReviewTable = new JBTable(myReviewModel);
    myReviewTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    myReviewTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        int viewRow = myReviewTable.getSelectedRow();
        if (viewRow >= 0 &&  viewRow < myReviewTable.getRowCount()) {
          try {
            final Review review = CrucibleManager.getInstance(myProject).getDetailsForReview((String)myReviewTable.getValueAt(viewRow, 0));
            openDetailsToolWindow(myProject, "REVIEW_TOOL_WINDOW", review);
          }
          catch (CrucibleApiException e1) {
            e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
          }
          catch (JDOMException e1) {
            e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
          }

        }
      }
    });

    final JScrollPane detailsScrollPane = ScrollPaneFactory.createScrollPane(myReviewTable);
    myReviewPanel.add(detailsScrollPane);

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

  public static void openDetailsToolWindow(Project project, String id, Review review) {
    // TODO: open new tab in toolwindow
  }


  private SimpleTreeStructure createTreeStructure() {
    final CrucibleRootNode rootNode = new CrucibleRootNode(myReviewModel);
    return new CrucibleTreeStructure(myProject, rootNode);
  }
}
