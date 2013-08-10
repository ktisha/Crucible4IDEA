package com.jetbrains.crucible.ui.toolWindow.details;

import com.intellij.ui.treeStructure.SimpleNode;
import com.jetbrains.crucible.model.Comment;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * User: ktisha
 */
public class CommentNode extends SimpleNode {
  private final Comment myComment;

  public CommentNode(@NotNull final Comment comment) {
    myComment = comment;
  }

  @NotNull
  public Comment getComment() {
    return myComment;
  }

  @Override
  public String getName() {
    return myComment.getAuthor() + " : " + myComment.getMessage();
  }

  @Override
  public SimpleNode[] getChildren() {
    return getNodesFromComments(myComment.getReplies());
  }

  @NotNull
  public static SimpleNode[] getNodesFromComments(@NotNull List<Comment> replies) {
    final List<SimpleNode> children = new ArrayList<SimpleNode>();
    for (Comment reply : replies) {
      children.add(new CommentNode(reply));
    }
    return children.toArray(new SimpleNode[children.size()]);
  }
}