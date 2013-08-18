package com.jetbrains.crucible.ui.toolWindow.details;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBDefaultTreeCellRenderer;
import com.intellij.ui.RoundedLineBorder;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.util.ui.GridBag;
import com.intellij.util.ui.UIUtil;
import com.intellij.xml.util.XmlStringUtil;
import com.jetbrains.crucible.model.Comment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
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

  @NotNull private final DefaultTreeCellRenderer myDefaultRenderer = new DefaultTreeCellRenderer();
  @NotNull private final CommentRendererPanel myPanel;

  public CommentNodeRenderer(@NotNull JTree tree) {
    super(tree);
    myPanel = new CommentRendererPanel();
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

  /**
   * Finds the link which was clicked by user, or null if he didn't click on an action link.
   * dx and dy are relative coordinates on the renderer panel.
   */
  @Nullable
  public CommentAction.Type getActionLink(int dx, int dy) {
    return myPanel.getActionLink(dx, dy);
  }

  public static class CommentRendererPanel extends JPanel {

    public static final Color COMMENT_BG_COLOR = new JBColor(new Color(253, 255, 224), JBColor.YELLOW);
    public static final Color COMMENT_BORDER_COLOR = new JBColor(new Color(236, 217, 164), JBColor.ORANGE);
    public static final Color DRAFT_BG_COLOR = new JBColor(Gray._247, JBColor.LIGHT_GRAY);
    public static final Color DRAFT_BORDER_COLOR = JBColor.GRAY;

    @NotNull private final JBLabel myIconLabel;
    @NotNull private final JBLabel myMessageLabel;
    @NotNull private final JPanel myMainPanel;
    @NotNull private final JLabel myPostLink;
    @NotNull private final JLabel myReplyLink;
    private final JPanel myActionsPanel;

    CommentRendererPanel() {
      super(new BorderLayout());
      setOpaque(false);

      myIconLabel = new JBLabel();
      myMessageLabel = new JBLabel();
      myMessageLabel.setOpaque(false);

      myActionsPanel = new JPanel();
      myActionsPanel.setOpaque(false);
      myReplyLink = new LinkLabel("Reply", null);
      myPostLink = new LinkLabel("Publish", null);
      myActionsPanel.add(myReplyLink);
      myActionsPanel.add(myPostLink);

      myMainPanel = new JPanel(new GridBagLayout());
      GridBag bag = new GridBag()
        .setDefaultInsets(UIUtil.DEFAULT_VGAP, UIUtil.DEFAULT_HGAP, UIUtil.DEFAULT_VGAP, UIUtil.DEFAULT_HGAP)
        .setDefaultPaddingY(UIUtil.DEFAULT_VGAP);
      myMainPanel.add(myIconLabel, bag.next().coverColumn().anchor(GridBagConstraints.NORTHWEST).weightx(0.1));
      myMainPanel.add(myMessageLabel, bag.next().fillCell().anchor(GridBagConstraints.NORTH).weightx(1.0));
      myMainPanel.add(myActionsPanel, bag.nextLine().anchor(GridBagConstraints.SOUTHEAST));

      add(myMainPanel);
    }

    void setComment(final Comment comment) {
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

      RoundedLineBorder roundedLineBorder = new RoundedLineBorder(comment.isDraft() ? DRAFT_BORDER_COLOR : COMMENT_BORDER_COLOR, 20, 2);
      Border marginBorder = BorderFactory.createEmptyBorder(0, 0, UIUtil.DEFAULT_VGAP, 0);
      myMainPanel.setBorder(BorderFactory.createCompoundBorder(marginBorder, roundedLineBorder));

      myMainPanel.setBackground(comment.isDraft() ? DRAFT_BG_COLOR : COMMENT_BG_COLOR);

      myPostLink.setVisible(comment.isDraft());
    }

    @Nullable
    public CommentAction.Type getActionLink(int x, int y) {
      int dx = x - myActionsPanel.getX();
      int dy = y - myActionsPanel.getY();

      if (checkLink(dx, dy, myReplyLink)) {
        return CommentAction.Type.REPLY;
      }
      if (checkLink(dx, dy, myPostLink)) {
        return CommentAction.Type.PUBLISH;
      }
      return null;
    }

    private boolean checkLink(int dx, int dy, JLabel link) {
      int postX = link.getX();
      int postY = link.getY();
      return dx >= postX && dx <= postX + link.getWidth() && dy >= postY && dy <= postY + link.getHeight();
    }
  }
}
