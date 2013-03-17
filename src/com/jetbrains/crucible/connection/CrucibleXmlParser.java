package com.jetbrains.crucible.connection;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.crucible.model.BasicReview;
import com.jetbrains.crucible.model.CrucibleVersionInfo;
import com.jetbrains.crucible.model.User;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * User : ktisha
 */
public final class CrucibleXmlParser {
  private static final Logger LOG = Logger.getInstance(CrucibleXmlParser.class.getName());
  private CrucibleXmlParser() {}

  @NotNull
  public static String getChildText(@NotNull final Element node, @NotNull final String childName) {
    final Element child = node.getChild(childName);
    if (child != null) {
      return child.getText();
    }
    return "";
  }

  @NotNull
  public static String getSubChildText(@NotNull final Element node, @NotNull final String childName) {
    final List<String> strings = StringUtil.split(childName, "/");
    Element currentNode = node;
    for (String subChildName : strings) {
      currentNode = currentNode.getChild(subChildName);
      if (currentNode == null)
        return "";
    }
    return currentNode.equals(node) ? "" : currentNode.getText();
  }

  public static CrucibleVersionInfo parseVersionNode(@NotNull final Element element) {
    return new CrucibleVersionInfo(getChildText(element, "releaseNumber"), getChildText(element, "buildDate"));
  }

  public static BasicReview parseBasicReview(@NotNull final String serverUrl, @NotNull final Element reviewNode) {
    final String permaId = getSubChildText(reviewNode, "permaId/id");
    final User author = parseUserNode(reviewNode.getChild("author"));
    final User creator = parseUserNode(reviewNode.getChild("creator"));
    final String description = getChildText(reviewNode, "name");
    final String state = getChildText(reviewNode, "state");

    final User moderator = (reviewNode.getChild("moderator") != null)
                           ? parseUserNode(reviewNode.getChild("moderator")) : null;

    final Date date = parseDate(reviewNode);
    final BasicReview review = new BasicReview(serverUrl, permaId, author, moderator);
    if (date != null)
      review.setCreateDate(date);
    review.setCreator(creator);
    review.setDescription(description);
    review.setState(state);
    return review;
  }

  public static User parseUserNode(@NotNull final Element repoNode) {
    return new User(getChildText(repoNode, "userName"));
  }

  @Nullable
  public static Date parseDate(@NotNull final Element repoNode) {
    final String dateText = getChildText(repoNode, "createDate");
    //2013-02-22T13:08:48.609+0300
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSZZZZ");
    try {
      return df.parse(dateText);
    }
    catch (ParseException e) {
      LOG.warn("Couldn't parse date format");
    }
    return null;
  }
}
