package com.jetbrains.crucible.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * User: ktisha
 */
public class DescriptionCellRenderer extends DefaultTableCellRenderer {
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                 int row, int column) {
    JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    final String text = label.getText();
    label.setToolTipText("<html>" + text + "</html>");
    return label;
  }
}