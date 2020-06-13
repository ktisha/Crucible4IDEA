package com.jetbrains.crucible.ui.toolWindow;

import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.LocalFilePath;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
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
import com.intellij.vcsUtil.VcsUtil;
import com.jetbrains.crucible.connection.CrucibleManager;
import com.jetbrains.crucible.model.Review;
import com.jetbrains.crucible.model.ReviewItem;
import com.jetbrains.crucible.ui.DescriptionCellRenderer;
import com.jetbrains.crucible.ui.toolWindow.details.DetailsPanel;
import com.jetbrains.crucible.ui.toolWindow.tree.CrucibleRootNode;
import com.jetbrains.crucible.ui.toolWindow.tree.CrucibleTreeModel;
import com.jetbrains.crucible.utils.CrucibleBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

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

  public CrucibleReviewModel getReviewModel() {
    return myReviewModel;
  }

  public CruciblePanel(@NotNull final Project project) {
    super(false);
    myProject = project;

    final JBSplitter splitter = new JBSplitter(false, 0.2f);

    myReviewModel = new CrucibleReviewModel(project);
    myReviewTable = new JBTable(myReviewModel);
    myReviewTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    myReviewTable.setStriped(true);
    myReviewTable.setExpandableItemsEnabled(false);

    final TableColumnModel columnModel = myReviewTable.getColumnModel();
    columnModel.getColumn(1).setCellRenderer(new DescriptionCellRenderer());

    setUpColumnWidths(myReviewTable);
    myReviewTable.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          final int viewRow = myReviewTable.getSelectedRow();
          if (viewRow >= 0 &&  viewRow < myReviewTable.getRowCount()) {
            ApplicationManager.getApplication().invokeLater(new Runnable() {
              @Override
              public void run() {
                final Review review =
                  CrucibleManager.getInstance(myProject).getDetailsForReview((String)myReviewTable.
                    getValueAt(viewRow, myReviewTable.getColumnModel().getColumnIndex(CrucibleBundle.message("crucible.id"))));
                if (review != null) {
                  openDetailsToolWindow(review);
                  myReviewTable.clearSelection();
                }
              }
            }, ModalityState.stateForComponent(myReviewTable));

          }
        }
    }});

    final TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(myReviewModel);
    rowSorter.setSortKeys(Collections.singletonList(new RowSorter.SortKey(4, SortOrder.ASCENDING)));
    rowSorter.sort();
    myReviewTable.setRowSorter(rowSorter);

    final JScrollPane detailsScrollPane = ScrollPaneFactory.createScrollPane(myReviewTable);

    final SimpleTreeStructure reviewTreeStructure = createTreeStructure();
    final DefaultTreeModel model = new CrucibleTreeModel();
    final SimpleTree reviewTree = new SimpleTree(model);

    new AbstractTreeBuilder(reviewTree, model, reviewTreeStructure, null);
    reviewTree.invalidate();

    final JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(reviewTree);
    splitter.setFirstComponent(scrollPane);
    splitter.setSecondComponent(detailsScrollPane);
    setContent(splitter);
  }

  private static void setUpColumnWidths(@NotNull final JBTable table) {
    table.getColumnModel().getColumn(0).setMinWidth(130);     //ID
    table.getColumnModel().getColumn(0).setMaxWidth(130);     //ID
    table.getColumnModel().getColumn(1).setMinWidth(400);          //message
    table.getColumnModel().getColumn(1).setPreferredWidth(400);    //message
    table.getColumnModel().getColumn(2).setMinWidth(130);     //State
    table.getColumnModel().getColumn(2).setMaxWidth(130);     //State
    table.getColumnModel().getColumn(3).setMinWidth(200);     //Author
    table.getColumnModel().getColumn(3).setMaxWidth(200);     //Author
    table.getColumnModel().getColumn(4).setMinWidth(130);     //Date
    table.getColumnModel().getColumn(4).setMaxWidth(130);     //Date
  }

  public void openDetailsToolWindow(@NotNull final Review review) {
    final ToolWindow toolWindow = ToolWindowManager.getInstance(myProject).getToolWindow(CrucibleBundle.message("crucible.toolwindow.id"));
    final ContentManager contentManager = toolWindow.getContentManager();
    final Content foundContent = contentManager.findContent("Details for " + review.getPermaId());
    if (foundContent != null) {
      contentManager.setSelectedContent(foundContent);
      return;
    }

    final DetailsPanel details = new DetailsPanel(myProject, review);
    final Content content = ContentFactory.SERVICE.getInstance().createContent(details,
                                                                               "Details for " + review.getPermaId(), false);
    contentManager.addContent(content);
    contentManager.setSelectedContent(content);
    details.setBusy(true);

    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        final List<CommittedChangeList> list = new ArrayList<CommittedChangeList>();
        final ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(myProject);
        final String projectDir = myProject.getBasePath();
        final AbstractVcs vcsFor = vcsManager.getVcsFor(new LocalFilePath(Objects.requireNonNull(projectDir), true));
        if (vcsFor == null) return;
        final Set<ReviewItem> reviewItems = review.getReviewItems();
        final Set<String> loadedRevisions = new HashSet<String>();

        final Map<String, VirtualFile> hash = CrucibleManager.getInstance(myProject).getRepoHash();
        for (ReviewItem reviewItem : reviewItems) {
          final String root = hash.containsKey(reviewItem.getRepo()) ? hash.get(reviewItem.getRepo()).getPath() : projectDir;
          try {
            list.addAll(reviewItem.loadChangeLists(myProject, vcsFor, loadedRevisions, VcsUtil.getFilePath(root)));
          }
          catch (VcsException e) {
            LOG.error(e);
          }
        }
        details.updateCommitsList(list);
        details.setBusy(false);

      }
    }, ModalityState.stateForComponent(toolWindow.getComponent()));
  }

  private SimpleTreeStructure createTreeStructure() {
    final CrucibleRootNode rootNode = new CrucibleRootNode(myReviewModel);
    return new CrucibleTreeStructure(rootNode);
  }
}
