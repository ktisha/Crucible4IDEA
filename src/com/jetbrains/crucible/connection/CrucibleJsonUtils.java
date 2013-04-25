package com.jetbrains.crucible.connection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.jetbrains.crucible.model.*;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User : ktisha
 */
public final class CrucibleJsonUtils {
  private static final Logger LOG = Logger.getInstance(CrucibleJsonUtils.class.getName());
  private CrucibleJsonUtils() {}

  @NotNull
  public static BasicReview parseBasicReview(@NotNull final JsonElement element) {
    final JsonObject data = element.getAsJsonObject();

    final String permaId = getChildText(data.getAsJsonObject("permaId"), "id");
    final User author = parseUserNode(data.getAsJsonObject("author"));
    final String description = data.get("name").getAsString();
    final String state = data.get("state").getAsString();

    final User moderator = data.get("moderator") != null ? parseUserNode(data.getAsJsonObject("moderator")) : null;

    final Date date = parseDate(data.get("createDate"));
    final BasicReview review = new BasicReview(permaId, author, moderator);
    if (date != null)
      review.setCreateDate(date);
    review.setDescription(description);
    review.setState(state);

    return review;
  }

  @NotNull
  public static String getChildText(@NotNull final JsonObject jsonObject, @NotNull final String childName) {
    final JsonElement child = jsonObject.get(childName);
    if (child != null)
      return child.getAsString();
    return "";
  }


  @Nullable
  public static Date parseDate(@NotNull final JsonElement element) {
    final String dateText = element.getAsString();
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


  public static User parseUserNode(@NotNull final JsonObject element) {
    return new User(getChildText(element, "userName"));
  }

  public static CrucibleVersionInfo parseVersionNode(@NotNull final JsonObject element) {
    return new CrucibleVersionInfo(getChildText(element, "releaseNumber"), getChildText(element, "buildDate"));
  }

  public static Comment parseComment(@NotNull final JsonObject jsonObject, boolean isVersioned) {
    final String message = getChildText(jsonObject, "message");
    final User commentAuthor = parseUserNode(jsonObject.getAsJsonObject("user"));
    final Comment comment = new Comment(commentAuthor, message);

    final String id = isVersioned ? jsonObject.get("permaId").getAsString() :
                      getChildText(jsonObject.getAsJsonObject("permaId"), "id");
    comment.setPermId(id);

    if (!isVersioned) {
      final Date createDate = parseDate(jsonObject.get("createDate"));
      if (createDate != null) comment.setCreateDate(createDate);
    }
    parseReplies(jsonObject, comment);

    if (isVersioned) {
      final String toLineRange = getChildText(jsonObject, "toLineRange");
      comment.setLine(toLineRange);
      final JsonArray ranges = jsonObject.getAsJsonArray("lineRanges");
      if (ranges != null && ranges.size() > 0) {
        for (int i = 0; i != ranges.size(); ++i) {
          final String revision = getChildText(ranges.get(0).getAsJsonObject(), "revision");
          comment.setRevision(revision);
        }
      }

      final JsonObject reviewItemNode = jsonObject.getAsJsonObject("reviewItemId");
      if (reviewItemNode != null) {
        final String reviewItemId = getChildText(reviewItemNode, "id");
        comment.setReviewItemId(reviewItemId);
      }
    }
    return comment;
  }


  private static void parseReplies(@NotNull final JsonObject jsonObject, @NotNull final Comment comment) {
    final JsonArray replies = jsonObject.getAsJsonArray("replies");
    for (int i = 0; i != replies.size(); ++i) {
      final JsonObject reply = replies.get(i).getAsJsonObject();
      final Comment replyComment = CrucibleJsonUtils.parseComment(reply, false);
      comment.addReply(replyComment);
    }
  }

  @SuppressWarnings("unchecked")
  static RequestEntity createCommentRequest(Comment comment, boolean isGeneral) throws UnsupportedEncodingException {
    JsonObject root = new JsonObject();
    root.addProperty("message", comment.getMessage());
    root.addProperty("draft", false);
    root.addProperty("deleted", false);
    root.addProperty("defectRaised", false);

    final String parentId = comment.getParentCommentId();
    if (parentId != null) {
      JsonObject parentCommentId = new JsonObject();

      parentCommentId.addProperty("id", parentId);
      root.add("parentCommentId", parentCommentId);
    }

    if (!isGeneral) {
      final JsonObject reviewId = new JsonObject();
      reviewId.addProperty("id", comment.getReviewItemId());
      root.add("reviewItemId", reviewId);

      root.addProperty("toLineRange", comment.getLine());
    }

    final String requestString = root.toString();

    return new StringRequestEntity(requestString, "application/json", "UTF-8");
  }

  static void addGeneralComments(@NotNull final JsonObject jsonObject,
                                 @NotNull final Review review) {
    final JsonObject generalComments = jsonObject.getAsJsonObject("generalComments");
    if (generalComments != null) {
      final JsonArray generalCommentData = generalComments.getAsJsonArray("comments");
      if (generalCommentData != null && generalCommentData.size() > 0) {
        for (int i = 0; i != generalCommentData.size(); ++i) {
          final Comment comment = parseComment(generalCommentData.get(i).getAsJsonObject(), false);
          review.addGeneralComment(comment);
        }
      }
    }
  }

  static void addVersionedComments(@NotNull final JsonObject jsonObject, @NotNull final Review review) {
    final JsonObject versionedComments = jsonObject.getAsJsonObject("versionedComments");
    if (versionedComments != null) {
      final JsonArray comments = versionedComments.getAsJsonArray("comments");
      if (comments != null && comments.size() > 0) {
        for (int i = 0; i != comments.size(); ++i) {
          Comment comment = parseComment(comments.get(i).getAsJsonObject(), true);
          review.addComment(comment);
        }
      }
    }
  }

  static void addReviewItems(@NotNull final JsonArray reviewItems,
                             @NotNull final Review review) {
    for (int i = 0; i != reviewItems.size(); ++i) {
      final JsonObject item = reviewItems.get(i).getAsJsonObject();
      final JsonArray expandedRevisions = item.getAsJsonArray("expandedRevisions");
      final String id = getChildText(item.getAsJsonObject("permId"), "id");
      final String toPath = getChildText(item, "toPath");
      final String repoName = getChildText(item, "repositoryName");
      final String fromRevision = getChildText(item, "fromRevision");

      final ReviewItem reviewItem = new ReviewItem(id, toPath, repoName);
      for (int j = 0; j != expandedRevisions.size(); ++j) {
        final JsonObject expandedRevision = expandedRevisions.get(j).getAsJsonObject();
        final String revision = getChildText(expandedRevision, "revision");
        final String type = getChildText(item, "commitType");
        if (!fromRevision.equals(revision) || "Added".equals(type)) {
          reviewItem.addRevision(revision);
        }
      }
      review.addReviewItem(reviewItem);
    }
  }
}
