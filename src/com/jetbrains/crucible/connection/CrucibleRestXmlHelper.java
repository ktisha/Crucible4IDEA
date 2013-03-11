package com.jetbrains.crucible.connection;

import com.jetbrains.crucible.model.BasicReview;
import com.jetbrains.crucible.model.User;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;

/**
 * User : ktisha
 */
public final class CrucibleRestXmlHelper {
  private CrucibleRestXmlHelper() {}

  @NotNull
  public static String getChildText(@NotNull final Element node, @NotNull final String childName) {
    final Element child = node.getChild(childName);
    if (child != null) {
      return child.getText();
    }
    return "";
  }

  public static CrucibleVersionInfo parseVersionNode(@NotNull final Element element) {
    return new CrucibleVersionInfo(getChildText(element, "releaseNumber"), getChildText(element, "buildDate"));
  }

  public static BasicReview parseBasicReview(@NotNull final String serverUrl, @NotNull final Element reviewNode) throws ParseException {
    final String projectKey = getChildText(reviewNode, "projectKey");
    final User author = parseUserNode(reviewNode.getChild("author"));
    final User creator = parseUserNode(reviewNode.getChild("creator"));
    final String description = getChildText(reviewNode, "description");
    final String state = getChildText(reviewNode, "state");

    final User moderator = (reviewNode.getChild("moderator") != null)
                           ? parseUserNode(reviewNode.getChild("moderator")) : null;

    final BasicReview review = new BasicReview(serverUrl, projectKey, author, moderator);
    review.setCreator(creator);
    review.setDescription(description);
    review.setState(state);
    return review;
  }

  public static User parseUserNode(@NotNull final Element repoNode) {
    return new User(getChildText(repoNode, "userName"));
  }
}
