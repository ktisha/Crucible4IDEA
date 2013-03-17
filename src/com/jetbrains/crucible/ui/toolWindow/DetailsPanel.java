package com.jetbrains.crucible.ui.toolWindow;

import com.intellij.openapi.actionSystem.CommonShortcuts;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.committed.RepositoryChangesBrowser;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.SideBorder;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * User: ktisha
 * <p/>
 * Main code review panel
 */
public class DetailsPanel extends SimpleToolWindowPanel {

  private final Project myProject;
  private RepositoryChangesBrowser myRepositoryChangesBrowser;
  private JBTable myCommitsTable;
  private DefaultTableModel myCommitsModel;

  public DetailsPanel(Project project) {
    super(false);
    myProject = project;
    @SuppressWarnings("UseOfObsoleteCollectionType")
    final Vector<String> columnNames = new Vector<String>();
    columnNames.add("Commit");
    columnNames.add("Author");
    myCommitsModel = new DefaultTableModel(new Vector(), columnNames);

    Splitter splitter = new Splitter(false, 0.7f);
    final JPanel wrapper = createMainTable();
    splitter.setFirstComponent(wrapper);

    final JComponent component = createRepositoryBrowserDetails();
    splitter.setSecondComponent(component);

    setContent(splitter);
  }

  public void updateList(List<CommittedChangeList> list) {
    for (CommittedChangeList committedChangeList : list) {
      myCommitsModel.addRow(new Object[]{committedChangeList, committedChangeList.getCommitterName()});
    }
  }

  public void setBusy(boolean busy) {
    myCommitsTable.setPaintBusy(busy);
  }

  private JPanel createMainTable() {
    myCommitsTable = new JBTable(myCommitsModel) {
      @Override
      public TableCellRenderer getCellRenderer(int row, int column) {
        if (column == 0)
          return new MyCellRenderer();
        return super.getCellRenderer(row, column);
      }
    };
    myCommitsTable.setModel(myCommitsModel);
    myCommitsTable.setBorder(null);

    JScrollPane tableScrollPane = ScrollPaneFactory.createScrollPane(myCommitsTable);
    tableScrollPane.setBorder(IdeBorderFactory.createBorder(SideBorder.TOP | SideBorder.RIGHT | SideBorder.BOTTOM));

    final JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.add(tableScrollPane, BorderLayout.CENTER);

    return wrapper;
  }

  private JComponent createRepositoryBrowserDetails() {
    myRepositoryChangesBrowser = new RepositoryChangesBrowser(myProject, Collections.<CommittedChangeList>emptyList(), Collections.<Change>emptyList(), null);
    myRepositoryChangesBrowser.getDiffAction().registerCustomShortcutSet(CommonShortcuts.getDiff(), myCommitsTable);
    myRepositoryChangesBrowser.getViewer().setScrollPaneBorder(IdeBorderFactory.createBorder(SideBorder.LEFT | SideBorder.TOP));
    myCommitsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        final int[] indices = myCommitsTable.getSelectedRows();
        List<Change> changes = new ArrayList<Change>();
        for (int i : indices) {
          changes.addAll(((CommittedChangeList)myCommitsModel.getValueAt(i, 0)).getChanges());
        }
        myRepositoryChangesBrowser.setChangesToDisplay(changes);
      }
    });
    return myRepositoryChangesBrowser;
  }


  static class MyCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      if (value instanceof CommittedChangeList) {
        setText(((CommittedChangeList)value).getName());
      }
      final Color bg = isSelected ? UIUtil.getListSelectionBackground() : UIUtil.getListBackground();
      setBackground(bg);
      setBorder(BorderFactory.createLineBorder(bg));
      return this;
    }
  }
}
