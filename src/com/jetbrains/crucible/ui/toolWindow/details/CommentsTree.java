package com.jetbrains.crucible.ui.toolWindow.details;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.changes.issueLinks.LinkMouseListenerBase;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.Consumer;
import com.intellij.util.ui.tree.TreeUtil;
import com.jetbrains.crucible.actions.AddCommentAction;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import com.jetbrains.crucible.utils.CrucibleBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

/**
 * User: ktisha
 */
public abstract class CommentsTree extends Tree {

  private static final Logger LOG = Logger.getInstance(CommentsTree.class);
  @NotNull protected final Review myReview;

  protected CommentsTree(@NotNull final Project project, @NotNull final Review review,
                         @Nullable final Editor editor, @Nullable final FilePath filePath) {
    super();
    myReview = review;
    setExpandableItemsEnabled(false);
    setRowHeight(0);
    final CommentNodeRenderer renderer = new CommentNodeRenderer(this);
    setCellRenderer(renderer);
    getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    final DefaultActionGroup group = new DefaultActionGroup();
    final AddCommentAction replyToComment = new AddCommentAction(review, editor, filePath,
                                                                 CrucibleBundle.message("crucible.add.reply"), true);
    replyToComment.setContextComponent(this);
    group.add(replyToComment);
    PopupHandler.installUnknownPopupHandler(this, group, ActionManager.getInstance());
    TreeUtil.expandAll(this);

    new MyLinkMouseListener(renderer, project, review).installOn(this);
  }

  protected static void addReplies(@NotNull Comment comment, @NotNull DefaultMutableTreeNode parentNode) {
    for (Comment reply : comment.getReplies()) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(reply);
      parentNode.add(node);
      addReplies(reply, node);
    }
  }

  @Nullable
  public Comment getSelectedComment() {
    Object selected = getLastSelectedPathComponent();
    if (selected == null) {
      return null;
    }
    assert selected instanceof DefaultMutableTreeNode;
    Object userObject = ((DefaultMutableTreeNode)selected).getUserObject();
    assert userObject instanceof Comment;
    return (Comment)userObject;
  }

  public abstract void refresh(@NotNull Comment comment);

  protected void reloadModel(Comment comment) {
    setModel(createModel());
    TreePath path = findPath(comment);
    if (path != null) {
      scrollPathToVisible(path);
    }
  }

  protected abstract TreeModel createModel();

  @Nullable
  private TreePath findPath(@NotNull Comment comment) {
    for (Enumeration e = ((DefaultMutableTreeNode)getModel().getRoot()).depthFirstEnumeration(); e.hasMoreElements(); ) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.nextElement();
      if (node.getUserObject().equals(comment)) {
        return new TreePath(node.getPath());
      }
    }
    return null;
  }

  private class MyLinkMouseListener extends LinkMouseListenerBase {
    private final CommentNodeRenderer myRenderer;
    private final Project myProject;
    private final Review myReview;

    public MyLinkMouseListener(CommentNodeRenderer renderer, Project project, Review review) {
      myRenderer = renderer;
      myProject = project;
      myReview = review;
    }

    @Override
    protected void handleTagClick(Object tag, MouseEvent event) {
      if (tag == null) {
        return;
      }
      CommentAction action = (CommentAction)tag;
      action.execute(DataManager.getInstance().getDataContext(CommentsTree.this), new Consumer<Comment>() {
        @Override
        public void consume(Comment comment) {
          refresh(comment);
        }
      });
    }

    @Nullable
    @Override
    protected Object getTagAt(MouseEvent e) {
      JTree tree = (JTree) e.getSource();
      final TreePath path = tree.getPathForLocation(e.getX(), e.getY());
      if (path == null) {
        return null;
      }
      Rectangle bounds = tree.getPathBounds(path);
      if (bounds == null) {
        return null;
      }
      int dx = e.getX() - bounds.x;
      int dy = e.getY() - bounds.y;

      CommentAction.Type linkType = myRenderer.getActionLink(dx, dy);
      if (linkType == null) {
        return null;
      }
      DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)path.getLastPathComponent();
      Comment comment = (Comment)treeNode.getUserObject();

      return linkType.createAction(myProject, myReview, comment);
    }
  }
}
