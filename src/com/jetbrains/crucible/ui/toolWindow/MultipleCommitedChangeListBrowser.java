
package com.jetbrains.crucible.ui.toolWindow;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ui.ChangesBrowser;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MultipleCommitedChangeListBrowser extends ChangesBrowser {
  final List<CommittedChangeList> mychangeLists;
  final List<Change> mychanges;
  public MultipleCommitedChangeListBrowser(final Project project, final List<? extends ChangeList> changeLists, final List<Change> changes,
                                   final ChangeList initialListSelection,
                                   final boolean capableOfExcludingChanges,
                                   final boolean highlightProblems, @Nullable final Runnable inclusionListener) {
    super(project, changeLists, changes, initialListSelection, capableOfExcludingChanges, highlightProblems, inclusionListener, MyUseCase.COMMITTED_CHANGES, null);
    mychangeLists = (List<CommittedChangeList>)changeLists;
    mychanges = changes;
    rebuildList();
  }

  public List<Change> getCurrentDisplayedChanges() {
    if (mychanges == null)
      return super.getCurrentDisplayedChanges();
    return mychanges;
  }

  @Override
  protected List<AnAction> createDiffActions(final Change change) {
    List<AnAction> actions = super.createDiffActions(change);
    return actions;
  }
}
