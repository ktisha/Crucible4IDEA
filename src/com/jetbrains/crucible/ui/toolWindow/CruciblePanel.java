package com.jetbrains.crucible.ui.toolWindow;

import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import com.jetbrains.crucible.connection.CrucibleManager;
import com.jetbrains.crucible.connection.exceptions.CrucibleApiException;
import com.jetbrains.crucible.model.Review;
import com.jetbrains.crucible.ui.toolWindow.tree.CrucibleRootNode;
import com.jetbrains.crucible.ui.toolWindow.tree.CrucibleTreeModel;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: ktisha
 * <p/>
 * Main code review panel
 */
public class CruciblePanel extends SimpleToolWindowPanel {
  private static final Logger LOG = Logger.getInstance(CruciblePanel.class.getName());

  private final Project myProject;
  private final CrucibleReviewModel myReviewModel;
  private final JBTable myReviewTable;

  public CruciblePanel(Project project) {
    super(false);
    myProject = project;

    JBSplitter splitter = new JBSplitter(false);

    myReviewModel = new CrucibleReviewModel(project);
    myReviewTable = new JBTable(myReviewModel);
    myReviewTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    myReviewTable.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          final int viewRow = myReviewTable.getSelectedRow();
          if (viewRow >= 0 &&  viewRow < myReviewTable.getRowCount()) {
            ApplicationManager.getApplication().invokeLater(new Runnable() {
              @Override
              public void run() {
                try {
                  final Review review =
                    CrucibleManager.getInstance(myProject).getDetailsForReview((String)myReviewTable.getValueAt(viewRow, 0));
                  if (review != null)
                    openDetailsToolWindow(review);
                }
                catch (CrucibleApiException e1) {
                  LOG.warn(e1.getMessage());
                }
                catch (JDOMException e1) {
                  LOG.warn(e1.getMessage());
                }
              }
            }, ModalityState.stateForComponent(myReviewTable));

          }
        }
    }});

    myReviewTable.setRowSorter(new TableRowSorter<TableModel>(myReviewModel));
    final JScrollPane detailsScrollPane = ScrollPaneFactory.createScrollPane(myReviewTable);

    final SimpleTreeStructure reviewTreeStructure = createTreeStructure();
    final DefaultTreeModel model = new CrucibleTreeModel(project);
    SimpleTree reviewTree = new SimpleTree(model);

    AbstractTreeBuilder reviewTreeBuilder =
      new AbstractTreeBuilder(reviewTree, model, reviewTreeStructure, null);
    reviewTree.invalidate();

    final JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(reviewTree);
    splitter.setFirstComponent(scrollPane);
    splitter.setSecondComponent(detailsScrollPane);
    setContent(splitter);
  }

  public void openDetailsToolWindow(@NotNull final Review review) {
    final ToolWindow toolWindow = ToolWindowManager.getInstance(myProject).getToolWindow("Crucible connector");
    final ContentManager contentManager = toolWindow.getContentManager();
    final Content foundContent = contentManager.findContent("Details for " + review.getPermaId());
    if (foundContent == null) {
      final DetailsPanel details = new DetailsPanel(myProject, review);
      final Content content = ContentFactory.SERVICE.getInstance().createContent(details,
                                                                                 "Details for " + review.getPermaId(), false);
      contentManager.addContent(content);
      contentManager.setSelectedContent(content);
      details.setBusy(true);
      details.updateComments(review.getGeneralComments());
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        @Override
        public void run() {
          List<CommittedChangeList> list = new ArrayList<CommittedChangeList>();
          final ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(myProject);
          final VirtualFile virtualFile = myProject.getBaseDir();
          final AbstractVcs vcsFor = vcsManager.getVcsFor(virtualFile);
          if (vcsFor == null) return;
          final Map<String,VirtualFile> reviewRevisions = review.getRevisions();
          for (Map.Entry<String, VirtualFile> revision : reviewRevisions.entrySet()) {
            try {
              final VcsRevisionNumber revisionNumber = vcsFor.parseRevisionNumber(revision.getKey());
              final CommittedChangeList changeList = vcsFor.loadRevisions(revision.getValue(), revisionNumber);
              if (changeList != null)
                list.add(changeList);
            }
            catch (VcsException e) {
              LOG.warn(e.getMessage());
            }

          }
          details.updateList(list);
          details.setBusy(false);

        }
      }, ModalityState.stateForComponent(toolWindow.getComponent()));
    }
  }

  private SimpleTreeStructure createTreeStructure() {
    final CrucibleRootNode rootNode = new CrucibleRootNode(myReviewModel);
    return new CrucibleTreeStructure(myProject, rootNode);
  }
}
