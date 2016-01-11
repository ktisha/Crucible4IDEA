package com.jetbrains.crucible.ui.toolWindow.diff;

import com.intellij.diff.DiffContext;
import com.intellij.diff.DiffTool;
import com.intellij.diff.FrameDiffTool;
import com.intellij.diff.SuppressiveDiffTool;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.diff.tools.simple.SimpleDiffTool;
import com.intellij.diff.tools.simple.SimpleDiffViewer;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangesUtil;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.changes.actions.diff.ChangeDiffRequestProducer;
import com.intellij.ui.PopupHandler;
import com.jetbrains.crucible.actions.AddCommentAction;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import com.jetbrains.crucible.utils.CrucibleBundle;
import com.jetbrains.crucible.utils.CrucibleUserDataKeys;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * User: ktisha
 */
public class CommentsDiffTool implements FrameDiffTool, SuppressiveDiffTool {
  @NotNull
  @Override
  public String getName() {
    return SimpleDiffTool.INSTANCE.getName();
  }

  @Override
  public List<Class<? extends DiffTool>> getSuppressedTools() {
    return Collections.<Class<? extends DiffTool>>singletonList(SimpleDiffTool.class);
  }

  @Override
  public boolean canShow(@NotNull DiffContext context, @NotNull DiffRequest request) {
    if (context.getUserData(CrucibleUserDataKeys.REVIEW) == null) return false;
    if (request.getUserData(ChangeDiffRequestProducer.CHANGE_KEY) == null) return false;
    return SimpleDiffViewer.canShowRequest(context, request);
  }

  @NotNull
  @Override
  public DiffViewer createComponent(@NotNull DiffContext context, @NotNull DiffRequest request) {
    return new MySimpleDiffViewer(context, request);
  }

  private static class MySimpleDiffViewer extends SimpleDiffViewer {
    public MySimpleDiffViewer(@NotNull DiffContext context, @NotNull DiffRequest request) {
      super(context, request);
    }

    @Override
    protected void onInit() {
      super.onInit();

      final Review review = myContext.getUserData(CrucibleUserDataKeys.REVIEW);
      final Change change = myRequest.getUserData(ChangeDiffRequestProducer.CHANGE_KEY);
      assert review != null && change != null;

      final FilePath path = ChangesUtil.getFilePath(change);
      ContentRevision revision = change.getAfterRevision();
      if (revision == null) {
        revision = change.getBeforeRevision();
      }

      addCommentAction(getEditor2(), review, path);
      addGutter(getEditor2(), review, path, revision);
    }

    private static void addCommentAction(@NotNull final Editor editor2,
                                         @NotNull final Review review,
                                         @NotNull final FilePath filePath) {
      final AddCommentAction addCommentAction =
        new AddCommentAction(review, editor2, filePath, CrucibleBundle.message("crucible.add.comment"), false);
      addCommentAction.setContextComponent(editor2.getComponent());

      DefaultActionGroup group = new DefaultActionGroup(addCommentAction);
      PopupHandler.installUnknownPopupHandler(editor2.getContentComponent(), group, ActionManager.getInstance());
    }

    private static void addGutter(@NotNull Editor editor2,
                                  @NotNull final Review review,
                                  @NotNull FilePath filePath,
                                  @Nullable final ContentRevision revision) {
      if (revision == null) return;

      for (Comment comment : review.getComments()) {
        final String id = comment.getReviewItemId();
        final String path = review.getPathById(id);
        if (path != null && filePath.getPath().endsWith(path) &&
            (review.isInPatch(comment) || revision.getRevisionNumber().asString().equals(comment.getRevision()))) {

          int line = Integer.parseInt(comment.getLine()) - 1;
          final RangeHighlighter highlighter = editor2.getMarkupModel().addLineHighlighter(line, HighlighterLayer.ERROR + 1, null);
          final ReviewGutterIconRenderer gutterIconRenderer = new ReviewGutterIconRenderer(review, filePath, comment);
          highlighter.setGutterIconRenderer(gutterIconRenderer);
        }
      }
    }
  }
}
