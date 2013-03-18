package com.jetbrains.crucible.ui;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
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
                          final ReviewForm balloonContent, final String title) {

    final Document document = editor.getDocument();
    final int endOffset = document.getLineEndOffset(Integer.parseInt(comment.getLine()));

    final BalloonBuilder balloonBuilder =
      JBPopupFactory.getInstance().createDialogBalloonBuilder(balloonContent, title);
    balloonBuilder.setHideOnClickOutside(true);
    balloonBuilder.setHideOnKeyOutside(true);
    Balloon balloon = balloonBuilder.createBalloon();

    final Point targetPoint = editor.visualPositionToXY(editor.offsetToVisualPosition(endOffset));
    balloon.show(new RelativePoint(editor.getContentComponent(), targetPoint), Balloon.Position.below);
  }
}
