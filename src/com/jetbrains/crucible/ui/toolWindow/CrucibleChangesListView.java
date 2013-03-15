package com.jetbrains.crucible.ui.toolWindow;

import com.intellij.ide.util.treeView.TreeState;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.issueLinks.TreeLinkMouseListener;
import com.intellij.openapi.vcs.changes.ui.ChangesBrowserChangeListNode;
import com.intellij.openapi.vcs.changes.ui.ChangesBrowserNode;
import com.intellij.openapi.vcs.changes.ui.ChangesBrowserNodeRenderer;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.SmartExpander;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.EditSourceOnEnterKeyHandler;
import com.intellij.util.containers.Convertor;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * User : ktisha
 */
public class CrucibleChangesListView extends Tree implements TypeSafeDataProvider {
  private final Project myProject;
  private TreeState myTreeState;

  private ActionGroup myMenuGroup;

  public CrucibleChangesListView(final Project project) {
    myProject = project;

    getModel().setRoot(ChangesBrowserNode.create(myProject, TreeModelBuilder.ROOT_NODE_VALUE));

    setShowsRootHandles(true);
    setRootVisible(false);

    new TreeSpeedSearch(this, new NodeToTextConvertor());
    SmartExpander.installOn(this);
    new TreeLinkMouseListener(new ChangesBrowserNodeRenderer(myProject, false, false)).installOn(this);


    DefaultActionGroup menuGroup = (DefaultActionGroup) ActionManager.getInstance().getAction("CrucibleViewPopupMenu");
    setMenuActions(menuGroup);
  }

  @Override
  public DefaultTreeModel getModel() {
    return (DefaultTreeModel)super.getModel();
  }


  private void storeState() {
    myTreeState = TreeState.createOn(this, (ChangesBrowserNode)getModel().getRoot());
  }

  private void restoreState() {
    myTreeState.applyTo(this, (ChangesBrowserNode)getModel().getRoot());
  }

  public void updateModel(List<? extends ChangeList> changeLists) {
    TreeModelBuilder builder = new TreeModelBuilder(myProject, false);
    final DefaultTreeModel model = builder.buildModel(changeLists);

    storeState();
    setModel(model);
    setCellRenderer(new ChangesBrowserNodeRenderer(myProject, false, true));
    ChangesBrowserNode root = (ChangesBrowserNode)model.getRoot();
    expandPath(new TreePath(root.getPath()));
    restoreState();
  }

  public void setMenuActions(final ActionGroup menuGroup) {
    myMenuGroup = menuGroup;
    updateMenu();
    editSourceRegistration();
  }

  protected void editSourceRegistration() {
    EditSourceOnDoubleClickHandler.install(this);
    EditSourceOnEnterKeyHandler.install(this);
  }

  private void updateMenu() {
    PopupHandler.installPopupHandler(this, myMenuGroup, ActionPlaces.CHANGES_VIEW_POPUP, ActionManager.getInstance());
  }

  private static class NodeToTextConvertor implements Convertor<TreePath, String> {
    @Override
    public String convert(final TreePath path) {
      ChangesBrowserNode node = (ChangesBrowserNode)path.getLastPathComponent();
      return node.getTextPresentation();
    }
  }


  @NotNull
  public Change[] getSelectedChanges() {
    Set<Change> changes = new LinkedHashSet<Change>();

    final TreePath[] paths = getSelectionPaths();
    if (paths == null) {
      return new Change[0];
    }

    for (TreePath path : paths) {
      ChangesBrowserNode<?> node = (ChangesBrowserNode)path.getLastPathComponent();
      changes.addAll(node.getAllChangesUnder());
    }


    return changes.toArray(new Change[changes.size()]);
  }

  private Change[] getLeadSelection() {
    final Set<Change> changes = new LinkedHashSet<Change>();

    final TreePath[] paths = getSelectionPaths();
    if (paths == null) return new Change[0];

    for (TreePath path : paths) {
      ChangesBrowserNode node = (ChangesBrowserNode) path.getLastPathComponent();
      if (node instanceof com.intellij.openapi.vcs.changes.ui.ChangesBrowserChangeNode) {
        changes.add(((com.intellij.openapi.vcs.changes.ui.ChangesBrowserChangeNode) node).getUserObject());
      }
    }
    return changes.toArray(new Change[changes.size()]);
  }

  @NotNull
  private ChangeList[] getSelectedChangeLists() {
    Set<ChangeList> lists = new HashSet<ChangeList>();

    final TreePath[] paths = getSelectionPaths();
    if (paths == null) return new ChangeList[0];

    for (TreePath path : paths) {
      ChangesBrowserNode node = (ChangesBrowserNode)path.getLastPathComponent();
      final Object userObject = node.getUserObject();
      if (userObject instanceof ChangeList) {
        lists.add((ChangeList)userObject);
      }
    }

    return lists.toArray(new ChangeList[lists.size()]);
  }


  @Override
  public void calcData(DataKey key, DataSink sink) {
    if (key == VcsDataKeys.CHANGES) {
      sink.put(VcsDataKeys.CHANGES, getSelectedChanges());
    }
    else if (key == VcsDataKeys.CHANGE_LEAD_SELECTION) {
      sink.put(VcsDataKeys.CHANGE_LEAD_SELECTION, getLeadSelection());
    }
    else if (key == VcsDataKeys.CHANGE_LISTS) {
      sink.put(VcsDataKeys.CHANGE_LISTS, getSelectedChangeLists());
    }
    else if (key == VcsDataKeys.CHANGES_IN_LIST_KEY) {
      final TreePath selectionPath = getSelectionPath();
      if (selectionPath != null && selectionPath.getPathCount() > 1) {
        ChangesBrowserNode<?> firstNode = (ChangesBrowserNode)selectionPath.getPathComponent(1);
        if (firstNode instanceof ChangesBrowserChangeListNode) {
          final List<Change> list = firstNode.getAllChangesUnder();
          sink.put(VcsDataKeys.CHANGES_IN_LIST_KEY, list);
        }
      }
    }
  }
}
