package com.jetbrains.crucible.ui;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.*;
import com.intellij.ui.awt.RelativePoint;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.ui.toolWindow.CommentForm;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * User: ktisha
 */
public class ReviewBalloonBuilder {

  private Balloon myBalloon;

  public ReviewBalloonBuilder() {
  }

  public void showBalloon(@NotNull final Comment comment, @NotNull final Editor editor,
                          final ReviewForm balloonContent, final String title) {

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
    if (myBalloon == null) {
      final BalloonBuilder balloonBuilder =
        JBPopupFactory.getInstance().createDialogBalloonBuilder(balloonContent, "New comment");
      balloonBuilder.setHideOnClickOutside(true);
      balloonBuilder.setHideOnKeyOutside(true);

      myBalloon = balloonBuilder.createBalloon();
    }
    return myBalloon;
  }

}
