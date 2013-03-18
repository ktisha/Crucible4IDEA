package com.jetbrains.crucible.ui;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.EditorCustomization;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.EditorTextFieldProvider;
import com.intellij.ui.ScrollPaneFactory;
import com.jetbrains.crucible.model.Comment;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * User: ktisha
 */
public class ReviewForm extends JPanel {

  private EditorTextField myReviewTextField;

  private static final int ourBalloonWidth = 400;
  private static final int ourBalloonHeight = 400;

  public ReviewForm(final Comment comment, final Project project, boolean editable) {
    final Set<EditorCustomization.Feature> enabledFeatures = EnumSet.of(EditorCustomization.Feature.SOFT_WRAP);
    final Set<EditorCustomization.Feature> disabledFeatures = Collections.emptySet();

    final EditorTextFieldProvider service = ServiceManager.getService(project, EditorTextFieldProvider.class);
    myReviewTextField = service.getEditorField(PlainTextLanguage.INSTANCE, project, enabledFeatures, disabledFeatures);
    myReviewTextField.setText(comment.getMessage());

    final JScrollPane pane = ScrollPaneFactory.createScrollPane(myReviewTextField);
    pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    add(pane);
    myReviewTextField.setEnabled(editable);
    myReviewTextField.setPreferredSize(new Dimension(ourBalloonWidth, ourBalloonHeight));

  }

  public void requestFocus() {
    IdeFocusManager.findInstanceByComponent(myReviewTextField).requestFocus(myReviewTextField, true);
  }

  public String getText() {
    return myReviewTextField.getText();
  }
}
