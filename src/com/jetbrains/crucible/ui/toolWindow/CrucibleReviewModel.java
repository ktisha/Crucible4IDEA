package com.jetbrains.crucible.ui.toolWindow;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.jetbrains.crucible.connection.CrucibleFilter;
import com.jetbrains.crucible.connection.CrucibleManager;
import com.jetbrains.crucible.connection.exceptions.CrucibleApiException;
import com.jetbrains.crucible.model.BasicReview;
import org.jdom.JDOMException;

import javax.swing.table.DefaultTableModel;
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
  public int getColumnCount() {
    return 4;
  }

  public void updateModel(CrucibleFilter filter) {
    setRowCount(0);
    final CrucibleManager manager = CrucibleManager.getInstance(myProject);
    final List<BasicReview> reviews;
    try {
      reviews = manager.getReviewsForFilter(filter);
      for (BasicReview review : reviews) {
        addRow(new Object[]{review.getPermaId(), review.getDescription(), review.getState(), review.getAuthor().getUserName()});
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
