package com.jetbrains.crucible.ui.toolWindow;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.actions.OpenRepositoryVersionAction;
import com.intellij.openapi.vcs.changes.actions.ShowDiffWithLocalAction;
import com.intellij.openapi.vcs.changes.ui.ChangesBrowser;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.ui.*;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.tree.TreeUtil;
import com.jetbrains.crucible.actions.AddCommentAction;
import com.jetbrains.crucible.actions.ReplyToCommentAction;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import com.jetbrains.crucible.model.User;
import com.jetbrains.crucible.utils.CrucibleBundle;
import com.jetbrains.crucible.utils.CrucibleDataKeys;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * User: ktisha
 * <p/>
 * Show changes and general comments for review
 */
public class DetailsPanel extends SimpleToolWindowPanel {

  private final Project myProject;
  private final Review myReview;
  private ChangesBrowser myChangesBrowser;
  private JBTable myCommitsTable;
  private DefaultTableModel myCommitsModel;
  private ListTreeTableModel myCommentsModel;
  private TreeTable myGeneralComments;

  @SuppressWarnings("UseOfObsoleteCollectionType")
  public DetailsPanel(@NotNull final Project project, @NotNull final Review review) {
    super(false);
    myProject = project;
    myReview = review;

    final Splitter splitter = new Splitter(false, 0.7f);
    final JPanel mainTable = createMainTable();
    splitter.setFirstComponent(mainTable);

    final JComponent repoBrowser = createRepositoryBrowserDetails();
    splitter.setSecondComponent(repoBrowser);

    setContent(splitter);
  }

  public void updateCommitsList(final @NotNull List<CommittedChangeList> changeLists) {
    for (CommittedChangeList committedChangeList : changeLists) {
      myCommitsModel.addRow(new Object[]{committedChangeList, committedChangeList.getCommitterName(), committedChangeList.getCommitDate()});
    }
  }

  public void setBusy(boolean busy) {
    myCommitsTable.setPaintBusy(busy);
  }

  @NotNull
  private JPanel createMainTable() {
    final JBSplitter splitter = new JBSplitter(true, 0.65f);
    final JScrollPane commitsPane = createCommitsPane();

    final JPanel commentsPane = createCommentsPane();
    splitter.setFirstComponent(commitsPane);
    splitter.setSecondComponent(commentsPane);
    return splitter;
  }

  @NotNull
  private JPanel createCommentsPane() {
    final List<Comment> comments = myReview.getGeneralComments();
    final MyTreeNode root = new MyTreeNode(new Comment(new User("Root"), "Root message"));
    for (Comment comment : comments) {
      final MyTreeNode commentNode = createNode(comment);
      root.add(commentNode);
    }

    myCommentsModel = new ListTreeTableModel(root, new ColumnInfo[]{COMMENT_COLUMN, AUTHOR_COLUMN, DATE_COLUMN });

    myGeneralComments = new TreeTable(myCommentsModel);

    final TreeTableTree tree = myGeneralComments.getTree();
    tree.setShowsRootHandles(true);
    tree.setCellRenderer(new MyTreeCellRenderer());

    TreeUtil.expandAll(myGeneralComments.getTree());
    myGeneralComments.setRootVisible(false);

    myGeneralComments.setStriped(true);
    setUpColumnWidths(myGeneralComments);

    return installActions();
  }

  private static MyTreeNode createNode(@NotNull final Comment comment) {
    final MyTreeNode commentNode = new MyTreeNode(comment);
    for (Comment c : comment.getReplies()) {
      final MyTreeNode node = createNode(c);
      commentNode.add(node);
    }
    return commentNode;
  }

  @NotNull
  private JPanel installActions() {
    final DefaultActionGroup actionGroup = new DefaultActionGroup();
    final AddCommentAction addCommentAction = new AddCommentAction(CrucibleBundle.message("crucible.add.comment"),
                                                                   myReview.getPermaId(), null, myReview);
    addCommentAction.setContextComponent(myGeneralComments);
    actionGroup.add(addCommentAction);

    final ReplyToCommentAction replyToCommentAction =
      new ReplyToCommentAction(myReview, null, null, CrucibleBundle.message("crucible.reply"), myReview.getPermaId());

    replyToCommentAction.setContextComponent(myGeneralComments);
    actionGroup.add(replyToCommentAction);

    final ActionPopupMenu actionPopupMenu = ActionManager.getInstance()
      .createActionPopupMenu(CrucibleBundle.message("crucible.main.name"), actionGroup);
    final JPopupMenu popupMenu = actionPopupMenu.getComponent();
    myGeneralComments.setComponentPopupMenu(popupMenu);

    final ToolbarDecorator decorator = ToolbarDecorator.createDecorator(myGeneralComments).
      setToolbarPosition(ActionToolbarPosition.LEFT);
    decorator.addExtraAction(addCommentAction);
    decorator.addExtraAction(replyToCommentAction);

    final Border border = IdeBorderFactory.createTitledBorder(CrucibleBundle.message("crucible.general.comments"),
                                                              false);
    final JPanel decoratedPanel = decorator.createPanel();
    decoratedPanel.setBorder(border);
    return decoratedPanel;
  }

  @NotNull
  private JScrollPane createCommitsPane() {
    @SuppressWarnings("UseOfObsoleteCollectionType")
    final Vector<String> commitColumnNames = new Vector<String>();
    commitColumnNames.add(CrucibleBundle.message("crucible.commit"));
    commitColumnNames.add(CrucibleBundle.message("crucible.author"));
    commitColumnNames.add(CrucibleBundle.message("crucible.date"));

    //noinspection UseOfObsoleteCollectionType
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

    myCommitsTable = new JBTable(myCommitsModel) {
      @Override
      public TableCellRenderer getCellRenderer(int row, int column) {
        if (column == 0)
          return new MyCommitsCellRenderer();
        return super.getCellRenderer(row, column);
      }
    };
    myCommitsTable.setStriped(true);
    myCommitsTable.setAutoCreateRowSorter(true);

    setUpColumnWidths(myCommitsTable);

    return ScrollPaneFactory.createScrollPane(myCommitsTable);
  }

  private static void setUpColumnWidths(@NotNull final JBTable table) {
    table.getColumnModel().getColumn(0).setMinWidth(400);          //message
    table.getColumnModel().getColumn(0).setPreferredWidth(400);    //message
    table.getColumnModel().getColumn(1).setMinWidth(200);     //Author
    table.getColumnModel().getColumn(1).setMaxWidth(200);     //Author
    table.getColumnModel().getColumn(2).setMinWidth(130);     //Date
    table.getColumnModel().getColumn(2).setMaxWidth(130);     //Date
  }

  @NotNull
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


  static class MyCommitsCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      final Component orig = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      final Color bg = orig.getBackground();
      if (value instanceof CommittedChangeList) {
        setText(((CommittedChangeList)value).getName());
      }
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
      final OpenRepositoryVersionAction action = new OpenRepositoryVersionAction();
      toolBarGroup.add(action);

      final ActionGroup group = (ActionGroup) ActionManager.getInstance().getAction("RepositoryChangesBrowserToolbar");
      final AnAction[] actions = group.getChildren(null);
      for (AnAction anAction : actions) {
        toolBarGroup.add(anAction);
      }
    }


    @Override
    public void calcData(DataKey key, DataSink sink) {
      if (key == CrucibleDataKeys.REVIEW)
        sink.put(CrucibleDataKeys.REVIEW, myReview);
      if (key == CrucibleDataKeys.SELECTED_COMMENT)
        sink.put(CrucibleDataKeys.SELECTED_COMMENT, (Comment)myCommentsModel.getValueAt(myGeneralComments.getSelectedRow(), 0));
      if (key == VcsDataKeys.SELECTED_CHANGES) {
        final List<Change> list = myViewer.getSelectedChanges();
        sink.put(VcsDataKeys.SELECTED_CHANGES, list.toArray(new Change [list.size()]));
      }
      super.calcData(key, sink);
    }
  }

  private static class MyTreeNode extends DefaultMutableTreeNode {

    private Comment getComment() {
      return myComment;
    }

    private final Comment myComment;

    public MyTreeNode(Comment comment) {
      myComment = comment;
    }
  }


  private static final ColumnInfo<MyTreeNode, Comment> COMMENT_COLUMN = new ColumnInfo<MyTreeNode, Comment>("Message"){
    public Comment valueOf(final MyTreeNode node) {
      return node.getComment();
    }

    @Override
    public Class getColumnClass() {
      return TreeTableModel.class;
    }
  };

  private static final ColumnInfo<MyTreeNode, Date> DATE_COLUMN = new ColumnInfo<MyTreeNode, Date>("Date"){
    public Date valueOf(final MyTreeNode object) {
      Comment comment = object.getComment();
      return comment.getCreateDate();
    }

    public final Class getColumnClass() {
      return Date.class;
    }
  };

  private static final ColumnInfo<MyTreeNode, String> AUTHOR_COLUMN = new ColumnInfo<MyTreeNode, String>("Author"){
    public String valueOf(final MyTreeNode object) {
      Comment comment = object.getComment();
      return comment != null ? comment.getAuthor().getUserName() : "";
    }

    public final Class getColumnClass() {
      return String.class;
    }
  };

  private static class MyTreeCellRenderer extends JLabel implements TreeCellRenderer {
    public Component getTreeCellRendererComponent(final JTree tree,
                                                  final Object value,
                                                  final boolean selected,
                                                  final boolean expanded,
                                                  final boolean leaf,
                                                  final int row,
                                                  final boolean hasFocus) {

      if (value instanceof MyTreeNode) {
        final MyTreeNode node = (MyTreeNode)value;
        final Comment comment = node.getComment();
        setText(comment.getMessage());
        setOpaque(true);
        Color background = tree.getBackground();
        setBackground(background);
      }
      setIcon(null);
      return this;
    }
  }
}
