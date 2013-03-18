package com.jetbrains.crucible.ui.toolWindow;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diff.*;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.impl.DocumentMarkupModel;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.ObjectsConvertor;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.changes.actions.*;
import com.intellij.openapi.vcs.changes.ui.ChangesBrowser;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.SideBorder;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    protected void showDiffForChanges(Change[] changes, int indexInSelection) {
      final ShowDiffUIContext context = new ShowDiffUIContext(false);
      final List<DiffRequestPresentable> changeList =
        ObjectsConvertor.convert(Arrays.asList(changes), new ChangeForDiffConvertor(myProject, true), ObjectsConvertor.NOT_NULL);
      final ChangeDiffRequest request = new ChangeDiffRequest(myProject, changeList, context.getActionsFactory(), context.isShowFrame());
      final DiffTool tool = DiffManager.getInstance().getDiffTool();
      final DiffRequest simpleRequest;
      try {
        request.quickCheckHaveStuff();
        simpleRequest = request.init(indexInSelection);
      }
      catch (VcsException e) {
        Messages.showWarningDialog(e.getMessage(), "Show Diff");
        return;
      }

      if (simpleRequest != null) {
        final DiffContent content = simpleRequest.getContents()[1];
        final ContentRevision revision = changes[indexInSelection].getAfterRevision();

        addGutter(content, myReview, revision);

        final DiffNavigationContext navigationContext = context.getDiffNavigationContext();
        if (navigationContext != null) {
          simpleRequest.passForDataContext(DiffTool.SCROLL_TO_LINE, navigationContext);
        }
        tool.show(simpleRequest);
      }
    }

    private void addGutter(DiffContent content, Review review, @Nullable ContentRevision revision) {
      final List<Comment> comments = review.getComments();
      final FilePath filePath = revision == null? null : revision.getFile();
      for (Comment comment : comments) {
        final String id = comment.getReviewItemId();
        final VirtualFile vFile = review.getFileById(id);
        if (filePath != null && vFile.getPath().equals(filePath.getPath()) &&
          revision.getRevisionNumber().asString().equals(comment.getRevision())) {
          final MarkupModelEx markup = (MarkupModelEx)DocumentMarkupModel.forDocument(content.getDocument(), myProject, true);

          final RangeHighlighter highlighter = markup.addPersistentLineHighlighter(Integer.parseInt(comment.getLine()),
                                                                                   HighlighterLayer.ERROR + 1, null);
          if(highlighter == null) return;
          final ReviewGutterIconRenderer gutterIconRenderer = new ReviewGutterIconRenderer(comment.getMessage());
          highlighter.setGutterIconRenderer(gutterIconRenderer);
        }
      }
    }
  }

  private class ReviewGutterIconRenderer extends GutterIconRenderer {
    private final Icon icon = IconLoader.getIcon("/images/note.png");
    private final String myTooltip;

    ReviewGutterIconRenderer(String tooltip) {
      myTooltip = tooltip;
    }
    @NotNull
    @Override
    public Icon getIcon() {
      return icon;
    }

    @Override
    public boolean isNavigateAction() {
      return true;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ReviewGutterIconRenderer that = (ReviewGutterIconRenderer) o;
      return icon.equals(that.getIcon());
    }

    @Override
    public AnAction getClickAction() {
      return null;
    }

    @Override
    public String getTooltipText() {
      return myTooltip;
    }

    @Override
    public int hashCode() {
      return getIcon().hashCode();
    }
  }

}
