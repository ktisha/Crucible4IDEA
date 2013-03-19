package com.jetbrains.crucible.ui.toolWindow;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diff.DiffContent;
import com.intellij.openapi.diff.DiffRequest;
import com.intellij.openapi.diff.impl.external.FrameDiffTool;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.impl.DocumentMarkupModel;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.jetbrains.crucible.CrucibleDataKeys;
import com.jetbrains.crucible.actions.ShowCommentAction;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * User: ktisha
 */
public class CommentsDiffTool extends FrameDiffTool {

  @Override
  public void show(DiffRequest request) {
    super.show(request);
    final DiffContent[] contents = request.getContents();
    if (contents.length != 2) return;

    final AsyncResult<DataContext> dataContextFromFocus = DataManager.getInstance().getDataContextFromFocus();
    final DataContext context = dataContextFromFocus.getResult();
    final Review review = CrucibleDataKeys.REVIEW.getData(context);
    final Change[] changes = VcsDataKeys.CHANGE_LEAD_SELECTION.getData(context);
    if (changes != null && changes.length == 1) {
      final ContentRevision revision = changes[0].getAfterRevision();
      addGutter(contents[1], review, revision, request.getProject());
    }
  }

  private void addGutter(DiffContent content, Review review,
                         @Nullable ContentRevision revision,
                         Project project) {
    final List<Comment> comments = review.getComments();
    final FilePath filePath = revision == null? null : revision.getFile();
    for (Comment comment : comments) {
      final String id = comment.getReviewItemId();
      final String path = review.getPathById(id);
      if (filePath != null && filePath.getPath().endsWith(path) &&
          revision.getRevisionNumber().asString().equals(comment.getRevision())) {

        final MarkupModelEx markup = (MarkupModelEx)DocumentMarkupModel.forDocument(content.getDocument(), project, true);

        final RangeHighlighter highlighter = markup.addPersistentLineHighlighter(Integer.parseInt(comment.getLine()),
                                                                                 HighlighterLayer.ERROR + 1, null);
        if(highlighter == null) return;
        final ReviewGutterIconRenderer gutterIconRenderer = new ReviewGutterIconRenderer(comment);
        highlighter.setGutterIconRenderer(gutterIconRenderer);
      }
    }
  }


  private class ReviewGutterIconRenderer extends GutterIconRenderer {
    private final Icon icon = IconLoader.getIcon("/images/note.png");
    private final Comment myComment;

    ReviewGutterIconRenderer(Comment comment) {
      myComment = comment;
    }
    @NotNull
    @Override
    public Icon getIcon() {
      return icon;
    }

    @Override
    public boolean isNavigateAction() {
      return true;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ReviewGutterIconRenderer that = (ReviewGutterIconRenderer) o;
      return icon.equals(that.getIcon());
    }

    @Override
    public AnAction getClickAction() {
      return new ShowCommentAction(myComment);
    }

    @Override
    public String getTooltipText() {
      return myComment.getAuthor().getUserName();
    }

    @Override
    public int hashCode() {
      return getIcon().hashCode();
    }
  }

}
