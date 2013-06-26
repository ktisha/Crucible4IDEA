package com.jetbrains.crucible.ui.toolWindow.details;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
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

  private VirtualFile myVirtualFile;
  private Editor myEditor;
  private Comment myComment;

  public Review getReview() {
    return myReview;
  }

  private Review myReview;
  private Comment myParentComment;

  public CommentForm(@NotNull final Project project, final boolean isGeneral, final boolean isReply) {
    super(new BorderLayout());
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
        final Comment comment = new Comment(new User(CrucibleSettings.getInstance().USERNAME), getText());
        assert myReview != null;

        final Comment parentComment = getParentComment();
        if (parentComment != null) {
          comment.setParentCommentId(parentComment.getPermId());
        }
        if (myEditor != null) {
          final Document document = myEditor.getDocument();
          final int lineNumber = document.getLineNumber(myEditor.getCaretModel().getOffset());
          comment.setLine(String.valueOf(lineNumber));
          final String path = myVirtualFile.getPath();
          final String id = myReview.getIdByPath(path, project);
          comment.setReviewItemId(id);
        }

        final Comment addedComment = CrucibleManager.getInstance(project).postComment(comment, isGeneral, myReview.getPermaId());

        if (addedComment != null && myBalloon != null) {
          addedComment.setLine(comment.getLine());
          addedComment.setReviewItemId(comment.getReviewItemId());
          myComment = addedComment;
          if (isReply) {
            myParentComment.addReply(addedComment);
          }
          else {
            if (isGeneral) {
              myReview.addGeneralComment(addedComment);
            }
            else {
              myReview.addComment(addedComment);
            }
          }
          myBalloon.dispose();
        }
      }
    });
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

  public void setVirtualFile(@NotNull final VirtualFile virtualFile) {
    myVirtualFile = virtualFile;
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

  public Comment getComment() {
    return myComment;
  }
}
