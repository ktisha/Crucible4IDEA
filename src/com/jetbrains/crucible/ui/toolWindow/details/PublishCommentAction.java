package com.jetbrains.crucible.ui.toolWindow.details;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.jetbrains.crucible.connection.CrucibleManager;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import org.jetbrains.annotations.NotNull;

/**
 * @author Kirill Likhodedov
 */
public class PublishCommentAction extends CommentAction {

  private static final Logger LOG = Logger.getInstance(CommentAction.class);

  public PublishCommentAction(@NotNull Project project, @NotNull Review review, @NotNull Comment comment) {
    super(project, review, comment, Type.PUBLISH);
  }

  @Override
  public void execute(@NotNull Runnable onSuccess) {
    try {
      CrucibleManager.getInstance(myProject).publishComment(myReview, myComment);
      myComment.setDraft(false);
      onSuccess.run();
    }
    catch (Exception e) {
      Messages.showErrorDialog(myProject, "Couldn't publish comment: " + e.getMessage(), "Comment Publish Failed");
      LOG.warn(e);
    }
  }

}
