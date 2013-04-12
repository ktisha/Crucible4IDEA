package com.jetbrains.crucible.ui.toolWindow.diff;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diff.DiffContent;
import com.intellij.openapi.diff.DiffRequest;
import com.intellij.openapi.diff.impl.DiffPanelImpl;
import com.intellij.openapi.diff.impl.external.FrameDiffTool;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.PopupHandler;
import com.jetbrains.crucible.actions.AddCommentAction;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import com.jetbrains.crucible.utils.CrucibleBundle;
import com.jetbrains.crucible.utils.CrucibleDataKeys;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * User: ktisha
 */
public class CommentsDiffTool extends FrameDiffTool {
  @Override
  public boolean canShow(DiffRequest request) {
    final boolean superCanShow = super.canShow(request);
    final AsyncResult<DataContext> dataContextFromFocus = DataManager.getInstance().getDataContextFromFocus();
    final DataContext context = dataContextFromFocus.getResult();
    final Review review = CrucibleDataKeys.REVIEW.getData(context);
    return superCanShow && review != null;
  }

  @Override
  public void show(DiffRequest request) {
    final DiffContent[] contents = request.getContents();
    if (contents.length != 2) return;

    final DialogBuilder builder = new DialogBuilder(request.getProject());
    final DiffPanelImpl diffPanel = (DiffPanelImpl)createComponent("", request, builder.getWindow(), builder);
    if (diffPanel == null) {
      Disposer.dispose(builder);
      return;
    }

    final AsyncResult<DataContext> dataContextFromFocus = DataManager.getInstance().getDataContextFromFocus();
    final DataContext context = dataContextFromFocus.getResult();
    final Review review = CrucibleDataKeys.REVIEW.getData(context);
    final Change[] changes = VcsDataKeys.CHANGE_LEAD_SELECTION.getData(context);
    final VirtualFile vFile = PlatformDataKeys.VIRTUAL_FILE.getData(context);
    final Editor editor2 = diffPanel.getEditor2();

    final String name = vFile == null ? "file" : vFile.getName();
    addCommentAction(editor2, vFile, review, name);

    builder.removeAllActions();
    builder.setCenterPanel(diffPanel.getComponent());
    builder.setPreferredFocusComponent(diffPanel.getPreferredFocusedComponent());
    builder.setTitle(request.getWindowTitle());
    builder.setDimensionServiceKey(request.getGroupKey());

    new AnAction() {
      public void actionPerformed(final AnActionEvent e) {
        builder.getDialogWrapper().close(0);
      }
    }.registerCustomShortcutSet(new CustomShortcutSet(KeymapManager.getInstance().getActiveKeymap().getShortcuts("CloseContent")),
                                diffPanel.getComponent());

    if (changes != null && changes.length == 1 && review != null && vFile != null) {
      final ContentRevision revision = changes[0].getAfterRevision();
      addGutter(review, revision, vFile, editor2);
    }
    builder.showModal(true);
  }

  private static void addCommentAction(@Nullable final Editor editor2,
                                       @Nullable final VirtualFile vFile,
                                       @Nullable final Review review, @NotNull final String name) {
    if (editor2 != null && review != null) {
      DefaultActionGroup group = new DefaultActionGroup();
      final AddCommentAction addCommentAction = new AddCommentAction(review, editor2, vFile, CrucibleBundle.message("crucible.add.comment"),
                                                                             name, false);
      addCommentAction.setContextComponent(editor2.getComponent());
      group.add(addCommentAction);
      PopupHandler.installUnknownPopupHandler(editor2.getContentComponent(), group, ActionManager.getInstance());
    }
  }

  private void addGutter(@NotNull final Review review,
                         @Nullable final ContentRevision revision,
                         @NotNull final VirtualFile vFile, Editor editor2) {
    final List<Comment> comments = review.getComments();
    final FilePath filePath = revision == null? null : revision.getFile();

    for (Comment comment : comments) {
      final String id = comment.getReviewItemId();
      final String path = review.getPathById(id);
      if (filePath != null && path != null && filePath.getPath().endsWith(path) &&
          revision.getRevisionNumber().asString().equals(comment.getRevision())) {

        final MarkupModel markup = editor2.getMarkupModel();

        final RangeHighlighter highlighter = markup.addLineHighlighter(Integer.parseInt(comment.getLine()), HighlighterLayer.ERROR + 1, null);
        final ReviewGutterIconRenderer gutterIconRenderer =
          new ReviewGutterIconRenderer(review, vFile, comment);
        highlighter.setGutterIconRenderer(gutterIconRenderer);
      }
    }
  }
}
