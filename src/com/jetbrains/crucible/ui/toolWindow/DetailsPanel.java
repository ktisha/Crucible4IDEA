package com.jetbrains.crucible.ui.toolWindow;

import com.intellij.openapi.actionSystem.CommonShortcuts;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.committed.RepositoryChangesBrowser;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.SideBorder;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: ktisha
 * <p/>
 * Main code review panel
 */
public class DetailsPanel extends SimpleToolWindowPanel {
  private static final Logger LOG = Logger.getInstance(DetailsPanel.class.getName());

  private final Project myProject;
  private final Splitter mySplitter;
  private RepositoryChangesBrowser myRepositoryChangesBrowser;
  private JBList myCommitList;
  private DefaultListModel myListModel;
  private JScrollPane myTableScrollPane;

  public DetailsPanel(Project project) {
    super(false);
    myProject = project;
    myListModel = new DefaultListModel();

    //for (CommittedChangeList committedChangeList : list) {
    //  myListModel.addElement(committedChangeList);
    //}
    mySplitter = new Splitter(false, 0.7f);

    final JPanel wrapper = createMainTable();
    mySplitter.setFirstComponent(wrapper);

    final JComponent component = createRepositoryBrowserDetails();
    mySplitter.setSecondComponent(component);

    setContent(mySplitter);
  }

  public void add(CommittedChangeList changeList) {
    myListModel.addElement(changeList);
  }

  public void updateList(List<CommittedChangeList> list) {
    for (CommittedChangeList committedChangeList : list) {
      myListModel.addElement(committedChangeList);
    }
  }

  public void setBusy(boolean busy) {
    myCommitList.setPaintBusy(busy);
  }

  private JPanel createMainTable() {
    myCommitList = new JBList(myListModel);
    myCommitList.setCellRenderer(new MyCellRenderer());
    myCommitList.setModel(myListModel);
    myCommitList.setBorder(null);

    myTableScrollPane = ScrollPaneFactory.createScrollPane(myCommitList);
    myTableScrollPane.setBorder(IdeBorderFactory.createBorder(SideBorder.TOP | SideBorder.RIGHT | SideBorder.BOTTOM));

    final JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.add(myTableScrollPane, BorderLayout.CENTER);

    return wrapper;
  }

  private JComponent createRepositoryBrowserDetails() {
    myRepositoryChangesBrowser = new RepositoryChangesBrowser(myProject, Collections.<CommittedChangeList>emptyList(), Collections.<Change>emptyList(), null);
    myRepositoryChangesBrowser.getDiffAction().registerCustomShortcutSet(CommonShortcuts.getDiff(), myCommitList);
    myRepositoryChangesBrowser.getViewer().setScrollPaneBorder(IdeBorderFactory.createBorder(SideBorder.LEFT | SideBorder.TOP));
    myCommitList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        final int[] indices = myCommitList.getSelectedIndices();
        List<Change> changes = new ArrayList<Change>();
        for (int i : indices) {
          changes.addAll(((CommittedChangeList)myListModel.getElementAt(i)).getChanges());
        }
        myRepositoryChangesBrowser.setChangesToDisplay(changes);
      }
    });
    return myRepositoryChangesBrowser;
  }


  class MyCellRenderer extends JLabel implements ListCellRenderer {
    public MyCellRenderer() {
      setOpaque(true);
    }

    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
      if (value instanceof CommittedChangeList) {
        setText(((CommittedChangeList)value).getName() + "(" + ((CommittedChangeList)value).getChanges().size() + " changes)");
      }
      final Color bg = isSelected ? UIUtil.getListSelectionBackground() : UIUtil.getListBackground();
      setBackground(bg);
      setBorder(BorderFactory.createLineBorder(bg));
      return this;
    }
  }
}
