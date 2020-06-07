package com.jetbrains.crucible.ui.toolWindow.details;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SpellCheckingEditorCustomizationProvider;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.*;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.crucible.configuration.CrucibleSettings;
import com.jetbrains.crucible.connection.CrucibleManager;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import com.jetbrains.crucible.model.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
  private static final int ourBalloonWidth = 350;
  private static final int ourBalloonHeight = 200;

  private final EditorTextField myReviewTextField;
  private JBPopup myBalloon;

  private Editor myEditor;
  @NotNull private final Project myProject;
  private final boolean myGeneral;
  private final boolean myReply;
  @Nullable private FilePath myFilePath;

  private boolean myOK;

  public Review getReview() {
    return myReview;
  }

  private Review myReview;
  private Comment myParentComment;

  public CommentForm(@NotNull Project project, boolean isGeneral, boolean isReply, @Nullable FilePath filePath) {
    super(new BorderLayout());
    myProject = project;
    myGeneral = isGeneral;
    myReply = isReply;
    myFilePath = filePath;

    final EditorTextFieldProvider service = ServiceManager.getService(project, EditorTextFieldProvider.class);
    final Set<EditorCustomization> editorFeatures =
      ContainerUtil.newHashSet(SoftWrapsEditorCustomization.ENABLED,
                               SpellCheckingEditorCustomizationProvider.getInstance().getEnabledCustomization());
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
        myOK = true;
        if (myBalloon != null) {
          myBalloon.dispose();
        }
      }
    });
  }

  @Nullable
  public Comment postComment() {
    final Comment comment = new Comment(new User(CrucibleSettings.getInstance().USERNAME), getText(), !myOK);
    assert myReview != null;

    final Comment parentComment = getParentComment();
    if (parentComment != null) {
      comment.setParentCommentId(parentComment.getPermId());
    }
    if (myEditor != null) {
      final Document document = myEditor.getDocument();
      final int lineNumber = document.getLineNumber(myEditor.getCaretModel().getOffset()) + 1;
      comment.setLine(String.valueOf(lineNumber));
      final String id = myReview.getIdByPath(myFilePath.getPath(), myProject);
      comment.setReviewItemId(id);
    }

    final Comment addedComment = CrucibleManager.getInstance(myProject).postComment(comment, myGeneral, myReview.getPermaId());

    if (addedComment != null) {
      addedComment.setLine(comment.getLine());
      addedComment.setReviewItemId(comment.getReviewItemId());
      if (myReply) {
        myParentComment.addReply(addedComment);
      }
      else {
        if (myGeneral) {
          myReview.addGeneralComment(addedComment);
        }
        else {
          myReview.addComment(addedComment);
        }
      }
    }
    return addedComment;
  }

  public void requestFocus() {
    IdeFocusManager.findInstanceByComponent(myReviewTextField).requestFocus(myReviewTextField, true);
  }

  @NotNull
  public String getText() {
    return myReviewTextField.getText();
  }

  public void setBalloon(@NotNull final JBPopup balloon) {
    myBalloon = balloon;
  }

  public void setEditor(@NotNull final Editor editor) {
    myEditor = editor;
  }

  public void setReview(@NotNull final Review review) {
    myReview = review;
  }

  @Nullable
  public Comment getParentComment() {
    return myParentComment;
  }

  public void setParentComment(@NotNull final Comment parentComment) {
    myParentComment = parentComment;
  }

}
