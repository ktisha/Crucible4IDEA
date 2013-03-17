package com.jetbrains.crucible.ui.toolWindow;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.jetbrains.crucible.model.CrucibleFilter;
import com.jetbrains.crucible.connection.CrucibleManager;
import com.jetbrains.crucible.connection.exceptions.CrucibleApiException;
import com.jetbrains.crucible.model.BasicReview;
import org.jdom.JDOMException;

import javax.swing.table.DefaultTableModel;
import java.util.Date;
import java.util.List;

/**
 * User: ktisha
 */
public class CrucibleReviewModel extends DefaultTableModel {
  private static final Logger LOG = Logger.getInstance(CrucibleReviewModel.class.getName());
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
        return "ID";
      case 1:
        return "Description";
      case 2:
        return "State";
      case 3:
        return "Author";
      case 4:
        return "Date";
    }
    return super.getColumnName(column);
  }

  @Override
  public boolean isCellEditable(int row, int column) {
    return false;
  }

  public void updateModel(CrucibleFilter filter) {
    setRowCount(0);
    final CrucibleManager manager = CrucibleManager.getInstance(myProject);
    final List<BasicReview> reviews;
    try {
      reviews = manager.getReviewsForFilter(filter);
      for (BasicReview review : reviews) {
        addRow(new Object[]{review.getPermaId(), review.getDescription(), review.getState(), review.getAuthor().getUserName(),
        review.getCreateDate()});
      }
    }
    catch (CrucibleApiException e) {
      LOG.warn(e.getMessage());
    }
    catch (JDOMException e) {
      LOG.warn(e.getMessage());
    }
  }
}
