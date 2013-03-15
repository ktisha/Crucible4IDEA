package com.jetbrains.crucible.ui.toolWindow;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.ui.ScrollPaneFactory;

import javax.swing.*;
import java.util.List;

/**
 * User: ktisha
 * <p/>
 * Main code review panel
 */
public class DetailsPanel extends SimpleToolWindowPanel {
  private static final Logger LOG = Logger.getInstance(DetailsPanel.class.getName());

  private final Project myProject;
  private final JPanel myPanel;

  public DetailsPanel(Project project, List<CommittedChangeList> list) {
    super(false);
    myProject = project;
    myPanel = new JPanel();

    final CrucibleChangesListView listView = new CrucibleChangesListView(myProject);
    listView.updateModel(list);

    JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(listView);
    myPanel.add(scrollPane);

    setContent(scrollPane);
  }
}
