package com.jetbrains.crucible.ui;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.*;
import com.intellij.ui.awt.RelativePoint;
import com.jetbrains.crucible.model.Comment;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * User: ktisha
 */
public class ReviewBalloonBuilder {

  public ReviewBalloonBuilder() {
  }

  public void showBalloon(@NotNull final Comment comment, @NotNull final Editor editor,
                          final CommentsTree balloonContent, final String title) {

    final Document document = editor.getDocument();
    final int endOffset = document.getLineEndOffset(Integer.parseInt(comment.getLine()));

    ComponentPopupBuilder builder = JBPopupFactory.getInstance().createComponentPopupBuilder(balloonContent, balloonContent);
    builder.setResizable(true);
    builder.setTitle(title);
    builder.setMovable(true);
    final JBPopup popup = builder.createPopup();
    final Point targetPoint = editor.visualPositionToXY(editor.offsetToVisualPosition(endOffset));
    popup.show(new RelativePoint(editor.getContentComponent(), targetPoint));
  }


  public Balloon getCommentBalloon(final CommentForm balloonContent) {
    final BalloonBuilder balloonBuilder =
      JBPopupFactory.getInstance().createDialogBalloonBuilder(balloonContent, "New comment for " + balloonContent.getContentName());
    balloonBuilder.setHideOnClickOutside(true);
    balloonBuilder.setHideOnKeyOutside(true);

    return balloonBuilder.createBalloon();
  }

}
