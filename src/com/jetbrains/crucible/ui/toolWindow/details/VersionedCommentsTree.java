package com.jetbrains.crucible.ui.toolWindow.details;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * @author Kirill Likhodedov
 */
public class VersionedCommentsTree extends CommentsTree {

  private final Runnable myUpdater;

  private VersionedCommentsTree(@NotNull Project project, @NotNull Review review, @NotNull DefaultTreeModel model,
                                @Nullable Editor editor, @Nullable FilePath filePath, Runnable runnable) {
    super(project, review, model, editor, filePath);
    myUpdater = runnable;
  }

  @NotNull
  public static CommentsTree create(@NotNull Project project, @NotNull Review review, @NotNull Comment comment,
                                    @NotNull Editor editor, @NotNull FilePath filePath, Runnable runnable) {
    DefaultTreeModel model = createModel(comment);
    return new VersionedCommentsTree(project, review, model, editor, filePath, runnable);
  }

  private static DefaultTreeModel createModel(Comment comment) {
    DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(comment);
    DefaultTreeModel model = new DefaultTreeModel(rootNode);
    addReplies(comment, rootNode);
    return model;
  }

  @Override
  public void refresh() {
    myUpdater.run();
  }
}
