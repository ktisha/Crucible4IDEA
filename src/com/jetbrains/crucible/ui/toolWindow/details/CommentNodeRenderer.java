package com.jetbrains.crucible.ui.toolWindow.details;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.JBDefaultTreeCellRenderer;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.GridBag;
import com.intellij.util.ui.UIUtil;
import com.intellij.xml.util.XmlStringUtil;
import com.jetbrains.crucible.model.Comment;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
* @author Kirill Likhodedov
*/
class CommentNodeRenderer extends JBDefaultTreeCellRenderer {

  private static final Logger LOG = Logger.getInstance(CommentNodeRenderer.class);
  private DefaultTreeCellRenderer myDefaultRenderer = new DefaultTreeCellRenderer();
  private CommentRendererPanel myPanel = new CommentRendererPanel();

  public CommentNodeRenderer(@NotNull JTree tree) {
    super(tree);
  }

  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    if (value instanceof DefaultMutableTreeNode) {
      Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
      if (userObject instanceof Comment) {
        myPanel.setComment((Comment)userObject);
        return myPanel;
      }
    }
    return myDefaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
  }

  private static class CommentRendererPanel extends JPanel {

    private final JBLabel myIconLabel;
    private final JBLabel myMessageLabel;

    CommentRendererPanel() {
      super(new BorderLayout());
      setOpaque(false);

      myIconLabel = new JBLabel();
      myMessageLabel = new JBLabel();

      JPanel content = new JPanel(new GridBagLayout());
      content.setOpaque(false);
      myMessageLabel.setOpaque(false);
      GridBag bag = new GridBag().setDefaultInsets(UIUtil.DEFAULT_VGAP, UIUtil.DEFAULT_HGAP, UIUtil.DEFAULT_VGAP, UIUtil.DEFAULT_HGAP);
      content.add(myIconLabel, bag.next().anchor(GridBagConstraints.NORTHWEST).weightx(0.1));
      content.add(myMessageLabel, bag.next().fillCell().weightx(1.0));

      add(content);
    }

    void setComment(Comment comment) {
      String avatar = comment.getAuthor().getAvatar();
      Icon icon = AllIcons.Ide.Warning_notifications;
      if (avatar != null) {
        try {
          icon = IconLoader.findIcon(new URL(avatar));
        }
        catch (MalformedURLException e) {
          LOG.warn(e);
        }
      }

      myIconLabel.setIcon(icon);
      myMessageLabel.setText(XmlStringUtil.wrapInHtml(comment.getMessage()));
    }

  }
}
