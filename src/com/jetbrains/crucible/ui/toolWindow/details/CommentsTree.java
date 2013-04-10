package com.jetbrains.crucible.ui.toolWindow.details;

import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import com.jetbrains.crucible.actions.ReplyToCommentAction;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import com.jetbrains.crucible.ui.toolWindow.CrucibleTreeStructure;
import com.jetbrains.crucible.utils.CrucibleBundle;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * User: ktisha
 */
public class CommentsTree extends SimpleTree {

  private static final int ourBalloonWidth = 400;
  private static final int ourBalloonHeight = 400;

  public CommentsTree(@NotNull final Review review, @NotNull final Comment comment,
                      @NotNull final Editor editor, @NotNull final VirtualFile vFile) {
    final CommentNode root = new CommentNode(comment);
    final SimpleTreeStructure structure = new CrucibleTreeStructure(root);

    new AbstractTreeBuilder(this, getBuilderModel(), structure, null);
    invalidate();
    setPreferredSize(new Dimension(ourBalloonWidth, ourBalloonHeight));

    final DefaultActionGroup group = new DefaultActionGroup();
    final ReplyToCommentAction replyToComment =
      new ReplyToCommentAction(review, editor, vFile, CrucibleBundle.message("crucible.add.reply"),
                               CrucibleBundle.message("crucible.comment"));
    replyToComment.setContextComponent(this);
    group.add(replyToComment);
    PopupHandler.installUnknownPopupHandler(this, group, ActionManager.getInstance());
  }
}
