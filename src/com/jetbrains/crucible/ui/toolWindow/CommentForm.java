package com.jetbrains.crucible.ui.toolWindow;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.spellchecker.ui.SpellCheckingEditorCustomization;
import com.intellij.ui.*;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.crucible.configuration.CrucibleSettings;
import com.jetbrains.crucible.connection.CrucibleManager;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Set;

/**
 * User: ktisha
 */
public class CommentForm extends JPanel {

  private final String myReviewId;
  private EditorTextField myReviewTextField;

  private static final int ourBalloonWidth = 400;
  private static final int ourBalloonHeight = 400;
  private Balloon myBalloon;

  public CommentForm(final Project project, final String reviewId) {
    myReviewId = reviewId;
    final EditorTextFieldProvider service = ServiceManager.getService(project, EditorTextFieldProvider.class);
    final Set<EditorCustomization> editorFeatures = ContainerUtil.newHashSet();
    editorFeatures.add(SoftWrapsEditorCustomization.ENABLED);
    editorFeatures.add(SpellCheckingEditorCustomization.ENABLED);
    myReviewTextField = service.getEditorField(PlainTextLanguage.INSTANCE, project, editorFeatures);

    final JScrollPane pane = ScrollPaneFactory.createScrollPane(myReviewTextField);
    pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    add(pane);

    myReviewTextField.setPreferredSize(new Dimension(ourBalloonWidth, ourBalloonHeight));

    myReviewTextField.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
      put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), "postComment");
    myReviewTextField.getActionMap().put("postComment", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final Comment comment = new Comment(new User(CrucibleSettings.getInstance(project).USERNAME), getText());
        final boolean success = CrucibleManager.getInstance(project).postComment(comment, reviewId);
        if (success && myBalloon != null)
          myBalloon.dispose();
      }
    });
  }

  public void requestFocus() {
    IdeFocusManager.findInstanceByComponent(myReviewTextField).requestFocus(myReviewTextField, true);
  }

  public String getText() {
    return myReviewTextField.getText();
  }

  public void setBalloon(Balloon balloon) {
    myBalloon = balloon;
  }

  public String getReviewId() {
    return myReviewId;
  }
}
