package com.jetbrains.crucible.ui.toolWindow.diff;

import com.intellij.ide.DataManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diff.DiffRequest;
import com.intellij.openapi.diff.impl.DiffPanelImpl;
import com.intellij.openapi.diff.impl.external.DiffManagerImpl;
import com.intellij.openapi.diff.impl.external.FrameDiffTool;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FilePathImpl;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeRequestChain;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.changes.actions.DiffRequestPresentable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.PopupHandler;
import com.jetbrains.crucible.actions.AddCommentAction;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import com.jetbrains.crucible.utils.CrucibleBundle;
import com.jetbrains.crucible.utils.CrucibleDataKeys;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

/**
 * User: ktisha
 */
public class CommentsDiffTool extends FrameDiffTool {

  @Nullable private Review myReview;
  @Nullable private Change[] myChanges;

  @Override
  public boolean canShow(DiffRequest request) {
    final boolean superCanShow = super.canShow(request);
    final AsyncResult<DataContext> dataContextFromFocus = DataManager.getInstance().getDataContextFromFocus();
    final DataContext context = dataContextFromFocus.getResult();
    if (context == null) return false;
    final Review review = CrucibleDataKeys.REVIEW.getData(context);
    return superCanShow && review != null;
  }

  @Nullable
  @Override
  protected DiffPanelImpl createDiffPanelImpl(@NotNull DiffRequest request, @Nullable Window window, @NotNull Disposable parentDisposable) {
    final AsyncResult<DataContext> dataContextFromFocus = DataManager.getInstance().getDataContextFromFocus();
    final DataContext context = dataContextFromFocus.getResult();
    if (context == null) return null;
    myReview = CrucibleDataKeys.REVIEW.getData(context);
    myChanges = VcsDataKeys.SELECTED_CHANGES.getData(context);

    DiffPanelImpl diffPanel = new CommentableDiffPanel(window, request);
    diffPanel.setDiffRequest(request);
    Disposer.register(parentDisposable, diffPanel);
    return diffPanel;
  }

  private static void addCommentAction(@Nullable final Editor editor2,
                                       @Nullable final VirtualFile vFile,
                                       @Nullable final Review review) {
    if (editor2 != null && review != null) {
      DefaultActionGroup group = new DefaultActionGroup();
      final AddCommentAction addCommentAction = new AddCommentAction(review, editor2, vFile, CrucibleBundle.message("crucible.add.comment"), false);
      addCommentAction.setContextComponent(editor2.getComponent());
      group.add(addCommentAction);
      PopupHandler.installUnknownPopupHandler(editor2.getContentComponent(), group, ActionManager.getInstance());
    }
  }

  private void addGutter(@NotNull final Review review,
                         @Nullable final ContentRevision revision,
                         @NotNull final VirtualFile vFile, Editor editor2) {
    final List<Comment> comments = review.getComments();
    final FilePath filePath = new FilePathImpl(vFile);

    for (Comment comment : comments) {
      final String id = comment.getReviewItemId();
      final String path = review.getPathById(id);
      if (revision != null && path != null && filePath.getPath().endsWith(path) &&
          revision.getRevisionNumber().asString().equals(comment.getRevision())) {

        final MarkupModel markup = editor2.getMarkupModel();

        final RangeHighlighter highlighter = markup.addLineHighlighter(Integer.parseInt(comment.getLine()), HighlighterLayer.ERROR + 1, null);
        final ReviewGutterIconRenderer gutterIconRenderer =
          new ReviewGutterIconRenderer(review, vFile, comment);
        highlighter.setGutterIconRenderer(gutterIconRenderer);
      }
    }
  }

  private class CommentableDiffPanel extends DiffPanelImpl {
    public CommentableDiffPanel(Window window, DiffRequest request) {
      super(window, request.getProject(), true, true, DiffManagerImpl.FULL_DIFF_DIVIDER_POLYGONS_OFFSET, CommentsDiffTool.this);
    }

    @Override
    public void setDiffRequest(DiffRequest request) {
      super.setDiffRequest(request);

      Object chain = request.getGenericData().get(VcsDataKeys.DIFF_REQUEST_CHAIN.getName());
      if (chain instanceof ChangeRequestChain) {
        DiffRequestPresentable currentRequest = ((ChangeRequestChain)chain).getCurrentRequest();
        if (currentRequest != null) {
          String path = currentRequest.getPathPresentation();
          VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);

          Editor editor2 = getEditor2();
          addCommentAction(editor2, file, myReview);

          if (myChanges != null && myChanges.length == 1 && myReview != null && file != null) {
            final ContentRevision revision = myChanges[0].getAfterRevision();
            addGutter(myReview, revision, file, editor2);
          }
        }
      }
    }
  }
}
