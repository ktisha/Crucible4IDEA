package com.jetbrains.crucible.ui;

import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import com.jetbrains.crucible.actions.ReplyToCommentAction;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import com.jetbrains.crucible.ui.toolWindow.CommentNode;
import com.jetbrains.crucible.ui.toolWindow.CrucibleTreeStructure;

import java.awt.*;

/**
 * User: ktisha
 */
public class CommentsTree extends SimpleTree {

  private static final int ourBalloonWidth = 400;
  private static final int ourBalloonHeight = 400;

  public CommentsTree(Review review, final Comment comment, final Project project, Editor editor, VirtualFile vFile) {
    final CommentNode root = new CommentNode(comment);
    SimpleTreeStructure structure = new CrucibleTreeStructure(project, root);
    AbstractTreeBuilder reviewTreeBuilder =
      new AbstractTreeBuilder(this, getBuilderModel(), structure, null);
    invalidate();

    setPreferredSize(new Dimension(ourBalloonWidth, ourBalloonHeight));

    DefaultActionGroup group = new DefaultActionGroup();
    final ReplyToCommentAction replyToComment = new ReplyToCommentAction(review, editor, vFile, "Add Reply", "Comment");
    replyToComment.setContextComponent(this);
    group.add(replyToComment);
    PopupHandler.installUnknownPopupHandler(this, group, ActionManager.getInstance());
  }
}
