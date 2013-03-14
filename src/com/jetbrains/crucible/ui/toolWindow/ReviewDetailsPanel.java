package com.jetbrains.crucible.ui.toolWindow;

import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.jetbrains.crucible.model.Review;

import javax.swing.*;

/**
 * User: ktisha
 * <p/>
 * Details panel
 */
public class ReviewDetailsPanel extends SimpleToolWindowPanel {

  private JPanel myMainPanel;
  private JList myPathTree;

  public ReviewDetailsPanel(Review review) {
    super(false);
    final DefaultListModel treeModel = new DefaultListModel();
    for (String path : review.getFiles()) {
      treeModel.addElement(path);
    }
    myPathTree.setModel(treeModel);
    setContent(myMainPanel);
  }

}
