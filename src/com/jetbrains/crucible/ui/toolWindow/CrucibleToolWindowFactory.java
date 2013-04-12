package com.jetbrains.crucible.ui.toolWindow;

import com.intellij.openapi.diff.DiffManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.jetbrains.crucible.configuration.CrucibleSettings;
import com.jetbrains.crucible.ui.toolWindow.diff.CommentsDiffTool;
import com.jetbrains.crucible.utils.CrucibleBundle;

/**
 * User: ktisha
 */
public class CrucibleToolWindowFactory implements ToolWindowFactory, DumbAware {

  @Override
  public void createToolWindowContent(Project project, ToolWindow toolWindow) {
    DiffManager.getInstance().registerDiffTool(new CommentsDiffTool());
    final ContentManager contentManager = toolWindow.getContentManager();
    if (StringUtil.isEmptyOrSpaces(CrucibleSettings.getInstance().SERVER_URL)) return;
    final CruciblePanel cruciblePanel = new CruciblePanel(project);
    final Content content = ContentFactory.SERVICE.getInstance().
      createContent(cruciblePanel, CrucibleBundle.message("crucible.main.name"), false);

    contentManager.addContent(content);
  }
}
