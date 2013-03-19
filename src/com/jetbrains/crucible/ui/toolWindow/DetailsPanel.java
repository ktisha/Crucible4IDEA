package com.jetbrains.crucible.ui.toolWindow;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.actions.OpenRepositoryVersionAction;
import com.intellij.openapi.vcs.changes.actions.ShowDiffWithLocalAction;
import com.intellij.openapi.vcs.changes.ui.ChangesBrowser;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.SideBorder;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.crucible.CrucibleDataKeys;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * User: ktisha
 * <p/>
 * Show changes and comments for review
 */
public class DetailsPanel extends SimpleToolWindowPanel {

  private final Project myProject;
  private final Review myReview;
  private ChangesBrowser myChangesBrowser;
  private JBTable myCommitsTable;
  private DefaultTableModel myCommitsModel;
  private DefaultTableModel myCommentsModel;

  public DetailsPanel(Project project, Review review) {
    super(false);
    myProject = project;
    myReview = review;
    @SuppressWarnings("UseOfObsoleteCollectionType")
    final Vector<String> commitColumnNames = new Vector<String>();
    commitColumnNames.add("Commit");
    commitColumnNames.add("Author");
    commitColumnNames.add("Date");

    @SuppressWarnings("UseOfObsoleteCollectionType")
    final Vector<String> commentColumnNames = new Vector<String>();
    commentColumnNames.add("Message");
    commentColumnNames.add("Author");
    commentColumnNames.add("Date");

    myCommitsModel = new DefaultTableModel(new Vector(), commitColumnNames) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }

      @Override
      public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) return CommittedChangeList.class;
        if (columnIndex == 2) return Date.class;
        return String.class;
      }
    };
    myCommentsModel = new DefaultTableModel(new Vector(), commentColumnNames) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }

      @Override
      public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 2) return Date.class;
        return String.class;
      }
    };

    Splitter splitter = new Splitter(false, 0.7f);
    final JPanel wrapper = createMainTable();
    splitter.setFirstComponent(wrapper);

    final JComponent component = createRepositoryBrowserDetails();
    splitter.setSecondComponent(component);

    setContent(splitter);
  }

  public void updateList(List<CommittedChangeList> list) {
    for (CommittedChangeList committedChangeList : list) {
      myCommitsModel.addRow(new Object[]{committedChangeList, committedChangeList.getCommitterName(), committedChangeList.getCommitDate()});
    }
  }

  public void updateComments(List<Comment> list) {
    for (Comment comment : list) {
      myCommentsModel.addRow(new Object[]{comment.getMessage(), comment.getAuthor().getUserName(), comment.getCreateDate()});
    }
  }

  public void setBusy(boolean busy) {
    myCommitsTable.setPaintBusy(busy);
  }

  private JPanel createMainTable() {
    JBSplitter splitter = new JBSplitter(true);

    myCommitsTable = new JBTable(myCommitsModel) {
      @Override
      public TableCellRenderer getCellRenderer(int row, int column) {
        if (column == 0)
          return new MyCellRenderer();
        return super.getCellRenderer(row, column);
      }
    };
    myCommitsTable.setAutoCreateRowSorter(true);
    JScrollPane tableScrollPane = ScrollPaneFactory.createScrollPane(myCommitsTable);
    JBTable generalComments = new JBTable(myCommentsModel);
    generalComments.setAutoCreateRowSorter(true);
    JScrollPane commentsScrollPane = ScrollPaneFactory.createScrollPane(generalComments);
    final Border border = IdeBorderFactory.createTitledBorder("General Comments", false);
    commentsScrollPane.setBorder(border);

    splitter.setFirstComponent(tableScrollPane);
    splitter.setSecondComponent(commentsScrollPane);
    return splitter;
  }

  private JComponent createRepositoryBrowserDetails() {
    myChangesBrowser = new MyChangesBrowser(myProject);

    myChangesBrowser.getDiffAction().registerCustomShortcutSet(CommonShortcuts.getDiff(), myCommitsTable);
    myChangesBrowser.getViewer().setScrollPaneBorder(IdeBorderFactory.createBorder(SideBorder.LEFT | SideBorder.TOP));

    myCommitsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        final int[] indices = myCommitsTable.getSelectedRows();
        List<Change> changes = new ArrayList<Change>();
        for (int i : indices) {
          changes.addAll(((CommittedChangeList)myCommitsModel.getValueAt(i, 0)).getChanges());
        }
        myChangesBrowser.setChangesToDisplay(changes);
      }
    });
    return myChangesBrowser;
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

  class MyChangesBrowser extends ChangesBrowser {
    public MyChangesBrowser(Project project) {
      super(project, Collections.<CommittedChangeList>emptyList(),
            Collections.<Change>emptyList(), null, false, false, null,
            ChangesBrowser.MyUseCase.COMMITTED_CHANGES, null);
    }

    protected void buildToolBar(final DefaultActionGroup toolBarGroup) {
      super.buildToolBar(toolBarGroup);
      toolBarGroup.add(new ShowDiffWithLocalAction());
      OpenRepositoryVersionAction action = new OpenRepositoryVersionAction();
      toolBarGroup.add(action);

      ActionGroup group = (ActionGroup) ActionManager.getInstance().getAction("RepositoryChangesBrowserToolbar");
      final AnAction[] actions = group.getChildren(null);
      for (AnAction anAction : actions) {
        toolBarGroup.add(anAction);
      }
    }


    @Override
    public void calcData(DataKey key, DataSink sink) {
      if (key == CrucibleDataKeys.REVIEW)
        sink.put(CrucibleDataKeys.REVIEW, myReview);
      super.calcData(key, sink);
    }
  }

}
