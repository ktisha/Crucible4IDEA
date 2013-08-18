package com.jetbrains.crucible.ui.toolWindow.details;

import com.intellij.openapi.editor.Editor;
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

  private VersionedCommentsTree(@NotNull Review review, @NotNull DefaultTreeModel model,
                                @Nullable Editor editor,
                                @Nullable FilePath filePath) {
    super(review, model, editor, filePath);
  }

  @NotNull
  public static CommentsTree create(@NotNull Review review, @NotNull Comment comment,
                                    @NotNull Editor editor, @NotNull FilePath filePath) {
    DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(comment);
    DefaultTreeModel model = new DefaultTreeModel(rootNode);
    addReplies(comment, rootNode);

    return new VersionedCommentsTree(review, model, editor, filePath);
  }

}
