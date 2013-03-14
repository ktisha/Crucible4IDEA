package com.jetbrains.crucible.ui.toolWindow;

import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.actionSystem.CommonShortcuts;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.committed.RepositoryChangesBrowser;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.SideBorder;
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
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * User: ktisha
 * <p/>
 * Main code review panel
 */
public class CruciblePanel extends SimpleToolWindowPanel {
  private static final Logger LOG = Logger.getInstance(CruciblePanel.class.getName());

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
            openDetailsToolWindow(review);
          }
          catch (CrucibleApiException e1) {
            LOG.warn(e1.getMessage());
          }
          catch (JDOMException e1) {
            LOG.warn(e1.getMessage());
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

  public void openDetailsToolWindow(Review review) {
    final ToolWindow toolWindow = ToolWindowManager.getInstance(myProject).getToolWindow("Crucible connector");
    final ContentManager contentManager = toolWindow.getContentManager();
    final Content foundContent = contentManager.findContent("Details for " + review.getPermaId());
    if (foundContent == null) {
      final Content content = ContentFactory.SERVICE.getInstance().createContent(createRepositoryBrowserDetails(toolWindow, review),
                                                                                 "Details for " + review.getPermaId(), false);
      contentManager.addContent(content);
      contentManager.setSelectedContent(content);
    }
  }

  private JComponent createRepositoryBrowserDetails(@NotNull final ToolWindow toolWindow, @NotNull final Review review) {

    final CommittedChangeList myChangeList = getChangeList(myProject, review.getRevisionNumber());
    RepositoryChangesBrowser changesBrowser =
      new RepositoryChangesBrowser(myProject, Collections.singletonList(myChangeList), new ArrayList<Change>(myChangeList.getChanges()),
                                   myChangeList);

    changesBrowser.getDiffAction().registerCustomShortcutSet(CommonShortcuts.getDiff(), toolWindow.getComponent());
    changesBrowser.getViewer().setScrollPaneBorder(IdeBorderFactory.createBorder(SideBorder.LEFT | SideBorder.TOP));
    return changesBrowser;
  }

  @Nullable
  private CommittedChangeList getChangeList(@NotNull final Project project, @NotNull final String revision) {
    final ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(myProject);
    final VirtualFile virtualFile = project.getProjectFile();
    if (virtualFile == null) return null;
    final AbstractVcs vcsFor = vcsManager.getVcsFor(virtualFile);

    VcsRevisionNumber revisionNumber;
    if (vcsFor != null) {
      try {
        revisionNumber = vcsFor.parseRevisionNumber(revision);
        final VcsKey vcsKey = vcsFor.getKeyInstanceMethod();
        final AbstractVcs vcs = ProjectLevelVcsManager.getInstance(project).findVcsByName(vcsKey.getName());
        if (vcs == null) return null;

        final CommittedChangesProvider provider = vcs.getCommittedChangesProvider();
        if (provider != null) {
          @SuppressWarnings("unchecked")
          final Pair<CommittedChangeList, FilePath> pair = provider.getOneList(virtualFile, revisionNumber);
          if (pair != null) {
            return pair.getFirst();
          }
        }
      }
      catch (VcsException e) {
        LOG.warn(e.getMessage());
      }
    }

    return null;
  }



  private SimpleTreeStructure createTreeStructure() {
    final CrucibleRootNode rootNode = new CrucibleRootNode(myReviewModel);
    return new CrucibleTreeStructure(myProject, rootNode);
  }
}
