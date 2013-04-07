package com.jetbrains.crucible.ui;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.*;
import com.intellij.ui.awt.RelativePoint;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.utils.CrucibleBundle;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * User: ktisha
 */
public class CommentBalloonBuilder {

  public CommentBalloonBuilder() {
  }

  public void showBalloon(@NotNull final Comment comment, @NotNull final Editor editor,
                          @NotNull final CommentsTree balloonContent, @NotNull final String title) {

    final Document document = editor.getDocument();
    final int endOffset = document.getLineEndOffset(Integer.parseInt(comment.getLine()));

    final ComponentPopupBuilder builder = JBPopupFactory.getInstance().
      createComponentPopupBuilder(balloonContent, balloonContent);
    builder.setResizable(true);
    builder.setTitle(title);
    builder.setMovable(true);
    final JBPopup popup = builder.createPopup();
    final Point targetPoint = editor.visualPositionToXY(editor.offsetToVisualPosition(endOffset));
    popup.show(new RelativePoint(editor.getContentComponent(), targetPoint));
  }


  public Balloon getNewCommentBalloon(final CommentForm balloonContent) {
    final BalloonBuilder balloonBuilder =
      JBPopupFactory.getInstance().
        createDialogBalloonBuilder(balloonContent,
                                   CrucibleBundle.message("crucible.new.comment.$0", balloonContent.getContentName()));
    balloonBuilder.setHideOnClickOutside(true);
    balloonBuilder.setHideOnKeyOutside(true);

    return balloonBuilder.createBalloon();
  }

}
