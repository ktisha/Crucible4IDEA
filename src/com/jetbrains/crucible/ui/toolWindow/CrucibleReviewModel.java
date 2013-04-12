package com.jetbrains.crucible.ui.toolWindow;

import com.intellij.openapi.project.Project;
import com.jetbrains.crucible.connection.CrucibleManager;
import com.jetbrains.crucible.model.BasicReview;
import com.jetbrains.crucible.model.CrucibleFilter;
import com.jetbrains.crucible.utils.CrucibleBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.table.DefaultTableModel;
import java.util.Date;
import java.util.List;

/**
 * User: ktisha
 */
public class CrucibleReviewModel extends DefaultTableModel {
  private final Project myProject;

  public CrucibleReviewModel(Project project) {
    myProject = project;
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    if (columnIndex == 4) return Date.class;
    return String.class;
  }

  @Override
  public int getColumnCount() {
    return 5;
  }

  @Override
  public String getColumnName(int column) {
    switch (column) {
      case 0:
        return CrucibleBundle.message("crucible.id");
      case 1:
        return CrucibleBundle.message("crucible.description");
      case 2:
        return CrucibleBundle.message("crucible.state");
      case 3:
        return CrucibleBundle.message("crucible.author");
      case 4:
        return CrucibleBundle.message("crucible.author");
    }
    return super.getColumnName(column);
  }

  @Override
  public boolean isCellEditable(int row, int column) {
    return false;
  }

  public void updateModel(@NotNull final CrucibleFilter filter) {
    setRowCount(0);
    final CrucibleManager manager = CrucibleManager.getInstance(myProject);
    final List<BasicReview> reviews;
    reviews = manager.getReviewsForFilter(filter);
    if (reviews != null) {
      for (BasicReview review : reviews) {
        addRow(new Object[]{review.getPermaId(), review.getDescription(), review.getState(), review.getAuthor().getUserName(),
        review.getCreateDate()});
      }
    }
  }
}
