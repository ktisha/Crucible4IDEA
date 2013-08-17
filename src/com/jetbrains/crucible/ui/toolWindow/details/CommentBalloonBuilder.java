package com.jetbrains.crucible.ui.toolWindow.details;

import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * User: ktisha
 */
public class CommentBalloonBuilder {

  public static final String DIMENSION_SERVICE_KEY = "Review.Comment.Balloon";

  private CommentBalloonBuilder() {
  }

  public static void showBalloon(@NotNull CommentsTree balloonContent) {
    JBPopup popup = JBPopupFactory.getInstance().createComponentPopupBuilder(balloonContent, balloonContent)
      .setResizable(true)
      .setTitle("Comments")
      .setMovable(true)
      .setDimensionServiceKey(null, DIMENSION_SERVICE_KEY, false).createPopup();

    final PointerInfo pointerInfo = MouseInfo.getPointerInfo();
    final Point targetPoint = pointerInfo.getLocation();
    popup.show(new RelativePoint(targetPoint));
  }


  @NotNull
  public static JBPopup getNewCommentBalloon(@NotNull CommentForm balloonContent, @NotNull final String title) {
    return JBPopupFactory.getInstance().createComponentPopupBuilder(balloonContent, balloonContent)
      .setAdText("Hit Ctrl+Enter to save comment.")
      .setTitle(title)
      .setResizable(true)
      .setMovable(true)
      .setCancelOnWindowDeactivation(false)
      .setDimensionServiceKey(null, DIMENSION_SERVICE_KEY, false)
      .createPopup();
  }

}
