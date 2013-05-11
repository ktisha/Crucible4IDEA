package com.jetbrains.crucible.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.AnActionButton;
import com.jetbrains.crucible.connection.CrucibleManager;
import com.jetbrains.crucible.model.Review;
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
  }
}
