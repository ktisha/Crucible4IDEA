package com.jetbrains.crucible.ui.toolWindow.details;

import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * User: ktisha
 */
public class CommentBalloonBuilder {

  public CommentBalloonBuilder() {
  }

  public void showBalloon(@NotNull final CommentsTree balloonContent) {
    final ComponentPopupBuilder builder = JBPopupFactory.getInstance().
      createComponentPopupBuilder(balloonContent, balloonContent);
    builder.setResizable(true);
    builder.setTitle("Comments");
    builder.setMovable(true);
    final JBPopup popup = builder.createPopup();

    final PointerInfo pointerInfo = MouseInfo.getPointerInfo();
    final Point targetPoint = pointerInfo.getLocation();
    popup.show(new RelativePoint(targetPoint));
  }


  public JBPopup getNewCommentBalloon(final CommentForm balloonContent, @NotNull final String title) {
    final ComponentPopupBuilder builder = JBPopupFactory.getInstance().
      createComponentPopupBuilder(balloonContent, balloonContent);
    builder.setAdText("Hit Ctrl+Enter to save comment.");
    builder.setTitle(title);
    builder.setResizable(true);
    builder.setMovable(true);
    return builder.createPopup();
  }

}
