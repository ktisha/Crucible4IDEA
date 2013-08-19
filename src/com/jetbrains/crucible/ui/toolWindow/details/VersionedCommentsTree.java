package com.jetbrains.crucible.ui.toolWindow.details;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

/**
 * @author Kirill Likhodedov
 */
public class VersionedCommentsTree extends CommentsTree {

  @NotNull private final Comment myComment;
  @NotNull private final Runnable myUpdater;

  private VersionedCommentsTree(@NotNull Project project, @NotNull Review review, @NotNull Comment comment,
                                @NotNull Editor editor, @NotNull FilePath filePath, @NotNull Runnable updater) {
    super(project, review, editor, filePath);
    myComment = comment;
    myUpdater = updater;
  }

  @NotNull
  public static CommentsTree create(@NotNull Project project, @NotNull Review review, @NotNull Comment comment,
                                    @NotNull Editor editor, @NotNull FilePath filePath, Runnable runnable) {
    VersionedCommentsTree tree = new VersionedCommentsTree(project, review, comment, editor, filePath, runnable);
    tree.setModel(tree.createModel());
    return tree;
  }

  @Override
  public void refresh(@NotNull Comment comment) {
    reloadModel(comment);
    myUpdater.run();
  }

  @Override
  protected TreeModel createModel() {
    DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(myComment);
    DefaultTreeModel model = new DefaultTreeModel(rootNode);
    addReplies(myComment, rootNode);
    return model;
  }

}
