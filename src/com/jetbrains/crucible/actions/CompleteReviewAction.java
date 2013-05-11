package com.jetbrains.crucible.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.jetbrains.crucible.connection.CrucibleManager;
import com.jetbrains.crucible.model.CrucibleFilter;
import com.jetbrains.crucible.model.Review;
import com.jetbrains.crucible.ui.toolWindow.CruciblePanel;
import com.jetbrains.crucible.utils.CrucibleBundle;
import org.jetbrains.annotations.NotNull;

/**
 * User: ktisha
 * <p/>
 * Complete current review
 */
@SuppressWarnings("ComponentNotRegistered")
public class CompleteReviewAction extends AnActionButton implements DumbAware {

  private final Review myReview;

  public CompleteReviewAction(@NotNull final Review review, @NotNull final String description) {
    super(description, description, IconLoader.getIcon("/images/complete.png"));
    myReview = review;
  }

  public void actionPerformed(AnActionEvent e) {
    final Project project = e.getData(PlatformDataKeys.PROJECT);
    if (project == null) return;
    CrucibleManager.getInstance(project).completeReview(myReview.getPermaId());
    final ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(CrucibleBundle.message("crucible.toolwindow.id"));
    final ContentManager contentManager = toolWindow.getContentManager();
    final Content foundContent = contentManager.findContent("Details for " + myReview.getPermaId());
    contentManager.removeContent(foundContent, true);

    final Content dash = contentManager.findContent("Dashboard");
    if (dash.getComponent() instanceof CruciblePanel) {
      ((CruciblePanel)dash.getComponent()).getReviewModel().updateModel(CrucibleFilter.ToReview);
    }
  }
}
