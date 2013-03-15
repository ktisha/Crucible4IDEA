package com.jetbrains.crucible.ui.toolWindow;

import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
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
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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
      final Content content = ContentFactory.SERVICE.getInstance().createContent(createRepositoryBrowserDetails(review),
                                                                                 "Details for " + review.getPermaId(), false);
      contentManager.addContent(content);
      contentManager.setSelectedContent(content);
    }
  }

  private JComponent createRepositoryBrowserDetails(@NotNull final Review review) {
    List<CommittedChangeList> list = new ArrayList<CommittedChangeList>();

    for (String revision : review.getRevisions()) {
      final CommittedChangeList changeList = getChangeList(myProject, revision);
      if (changeList != null)
        list.add(changeList);
    }
    DetailsPanel panel = new DetailsPanel(myProject, list);

    return panel;
  }

  @Nullable
  private CommittedChangeList getChangeList(@NotNull final Project project, @NotNull final String revision) {
    final ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(myProject);
    final VirtualFile virtualFile = project.getBaseDir();
    if (virtualFile == null) return null;
    final AbstractVcs vcsFor = vcsManager.getVcsFor(virtualFile);

    VcsRevisionNumber revisionNumber;
    if (vcsFor != null) {
      try {
        revisionNumber = vcsFor.parseRevisionNumber(revision);

        final CommittedChangesProvider provider = vcsFor.getCommittedChangesProvider();
        if (provider != null) {
          final VirtualFile[] rootsUnderVcs = vcsManager.getRootsUnderVcs(vcsFor);
          for (VirtualFile root : rootsUnderVcs) {
            CommittedChangeList changeList = findInRoot(provider, root, revisionNumber);
            if (changeList != null) return changeList;
          }
        }
      }
      catch (VcsException e) {
        LOG.warn(e.getMessage());
      }
    }

    return null;
  }

  @Nullable
  private static CommittedChangeList findInRoot(CommittedChangesProvider provider, VirtualFile virtualFile,
                                                VcsRevisionNumber revisionNumber) {
    try {
      @SuppressWarnings("unchecked")
      final Pair<CommittedChangeList, FilePath> pair = provider.getOneList(virtualFile, revisionNumber);
      if (pair != null) {
        return pair.getFirst();
      }
    }
    catch (VcsException e) {
      LOG.warn(e.getMessage());
    }
    return null;
  }



  private SimpleTreeStructure createTreeStructure() {
    final CrucibleRootNode rootNode = new CrucibleRootNode(myReviewModel);
    return new CrucibleTreeStructure(myProject, rootNode);
  }
}
