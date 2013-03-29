package com.jetbrains.crucible.ui;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.spellchecker.ui.SpellCheckingEditorCustomization;
import com.intellij.ui.*;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.crucible.configuration.CrucibleSettings;
import com.jetbrains.crucible.connection.CrucibleManager;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
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

  private String myContentName;
  private EditorTextField myReviewTextField;
  private Editor myEditor;

  private static final int ourBalloonWidth = 400;
  private static final int ourBalloonHeight = 400;
  private Balloon myBalloon;
  private VirtualFile myVitualFile;
  private Review myReview;

  public CommentForm(final Project project, final String contentName, final boolean isGeneral) {
    createMainPanel(project, contentName, isGeneral);
  }

  private void createMainPanel(final Project project, final String contentName, final boolean isGeneral) {
    myContentName = contentName;
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
        assert myReview != null;
        if (myEditor != null) {
          final Document document = myEditor.getDocument();
          final int lineNumber = document.getLineNumber(myEditor.getCaretModel().getOffset());
          comment.setLine(String.valueOf(lineNumber));
          final String path = myVitualFile.getPath();
          final String id = myReview.getIdByPath(path, project);
          comment.setReviewItemId(id);
        }

        final boolean success = CrucibleManager.getInstance(project).postComment(comment, isGeneral, myReview.getPermaId());
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

  public String getContentName() {
    return myContentName;
  }

  public void setEditor(Editor editor) {
    myEditor = editor;
  }

  public void setVirtualFile(VirtualFile vitualFile) {
    myVitualFile = vitualFile;
  }

  public void setReview(Review review) {
    myReview = review;
  }
}
