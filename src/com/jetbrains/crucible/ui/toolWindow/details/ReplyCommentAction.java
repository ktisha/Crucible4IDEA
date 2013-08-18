package com.jetbrains.crucible.ui.toolWindow.details;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupAdapter;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import com.jetbrains.crucible.utils.CrucibleBundle;
import org.jetbrains.annotations.NotNull;

/**
 * @author Kirill Likhodedov
 */
public class ReplyCommentAction extends PublishCommentAction {

  public ReplyCommentAction(@NotNull Project project, @NotNull Review review, @NotNull Comment parentComment) {
    super(project, review, parentComment);
  }

  @Override
  public void execute(@NotNull DataContext dataContext, @NotNull final Runnable onSuccess) {
    final CommentForm commentForm = new CommentForm(myProject, true, true, null); // TODO general, filepath
    commentForm.setReview(myReview);
    commentForm.setParentComment(myComment);
    final JBPopup balloon =
      CommentBalloonBuilder.getNewCommentBalloon(commentForm, CrucibleBundle.message("crucible.new.reply.$0", myComment.getPermId()));

    balloon.addListener(new JBPopupAdapter() {
      @Override
      public void onClosed(LightweightWindowEvent event) {
        commentForm.postComment();
        onSuccess.run();
      }
    });

    commentForm.setBalloon(balloon);
    balloon.showInBestPositionFor(dataContext);
    commentForm.requestFocus();
  }

}
