package com.jetbrains.crucible.ui.toolWindow.details;

import com.jetbrains.crucible.model.Comment;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * User: ktisha
 */

class CommentTreeNode extends DefaultMutableTreeNode {

  public Comment getComment() {
    return myComment;
  }

  private final Comment myComment;

  public CommentTreeNode(Comment comment) {
    myComment = comment;
  }
}
