package com.jetbrains.crucible.connection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diff.impl.patch.PatchReader;
import com.intellij.openapi.diff.impl.patch.PatchSyntaxException;
import com.intellij.openapi.diff.impl.patch.TextFilePatch;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vcs.FilePathImpl;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.changes.patch.FilePatchInProgress;
import com.intellij.openapi.vcs.changes.patch.MatchPatchPaths;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.crucible.model.*;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Kirill Likhodedov
 */
@SuppressWarnings("UnusedDeclaration")
public class CrucibleApi {

  private static final Logger LOG = Logger.getInstance(CrucibleApi.class);
  private static final Gson gson = initGson();

  @NotNull
  private static Gson initGson() {
    GsonBuilder builder = new GsonBuilder();
    builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ");
    return builder.create();
  }

  @NotNull
  public static BasicReview parseReview(@NotNull JsonObject item, @NotNull Project project, @NotNull CrucibleSession crucibleSession)
                                        throws IOException {
    ReviewRaw reviewRaw = gson.fromJson(item, ReviewRaw.class);
    Review review = new Review(reviewRaw.permaId.id, reviewRaw.author.createUser(), reviewRaw.moderator.createUser());
    review.setCreateDate(reviewRaw.createDate);
    review.setDescription(reviewRaw.name);
    review.setState(reviewRaw.state);

    if (reviewRaw.reviewers != null) {
      for (UserRaw reviewer : reviewRaw.reviewers.reviewer) {
        review.addReviewer(reviewer.createUser());
      }
    }

    if (reviewRaw.generalComments != null) {
      for (GeneralComment comment : reviewRaw.generalComments.comments) {
        review.addGeneralComment(createComment(comment));
      }
    }

    if (reviewRaw.versionedComments != null) {
      for (TopLevelComment comment : reviewRaw.versionedComments.comments) {
        review.addComment(createComment(comment));
      }
    }

    if (reviewRaw.reviewItems != null) {
      for (ReviewItemRaw itemRaw : reviewRaw.reviewItems.reviewItem) {
        review.addReviewItem(createReviewItem(itemRaw, project, crucibleSession));
      }
    }

    return review;
  }

  private static ReviewItem createReviewItem(ReviewItemRaw raw, Project project, CrucibleSession crucibleSession) throws IOException {
    if (raw.patchUrl != null) {
      return parsePatchReviewItem(raw, project, crucibleSession);
    }
    else {
      return parseVcsReviewItem(raw);
    }
  }

  @Nullable
  private static ReviewItem parsePatchReviewItem(ReviewItemRaw raw, Project project, CrucibleSession crucibleSession) throws IOException {
    final String id = raw.permId.id;
    final String toPath = raw.toPath;
    String patchUrl = raw.patchUrl;
    String file = crucibleSession.downloadFile(patchUrl);

    List<TextFilePatch> patchTexts;
    try {
      patchTexts = new PatchReader(file).readAllPatches();
    }
    catch (PatchSyntaxException e) {
      throw new IOException(e);
    }

    List<FilePatchInProgress> patches = new MatchPatchPaths(project).execute(patchTexts);

    if (patches.isEmpty()) {
      LOG.error("No patches generated for the following patch texts: " + patchTexts);
      return null;
    }

    FilePatchInProgress patchForItem = findBestMatchingPatchByPath(toPath, patches);

    File base = patchForItem.getIoCurrentBase();
    if (base == null) {
      LOG.error("No base for the patch " + patchForItem.getPatch());
      return null;
    }

    final VirtualFile repo = ProjectLevelVcsManager.getInstance(project).getVcsRootFor(new FilePathImpl(base, base.isDirectory()));
    if (repo == null) {
      LOG.error("Couldn't find repository for base " + base);
      return null;
    }

    Map.Entry<String, VirtualFile> repoEntry = ContainerUtil.find(crucibleSession.getRepoHash().entrySet(),
                                                                  new Condition<Map.Entry<String, VirtualFile>>() {
      @Override
      public boolean value(Map.Entry<String, VirtualFile> entry) {
        return entry.getValue().equals(repo);
      }
    });

    if (repoEntry == null) {
      LOG.error("Couldn't find repository name for root " + repo);
      return null;
    }
    String key = repoEntry.getKey();
    return new PatchReviewItem(id, patchForItem.getNewContentRevision().getFile().getPath(),
                               key, patches, patchUrl.substring(patchUrl.lastIndexOf("/") + 1), "",
                               raw.authorName, new Date(raw.commitDate));
  }

  // temporary workaround until ReviewItem is rethinked
  @NotNull
  private static FilePatchInProgress findBestMatchingPatchByPath(@NotNull String toPath, @NotNull List<FilePatchInProgress> patches) {
    int bestSimilarity = -1;
    FilePatchInProgress bestCandidate = null;
    for (FilePatchInProgress patch : patches) {
      String path = patch.getNewContentRevision().getFile().getPath();
      int similarity = findSimilarity(path, toPath);
      if (similarity > bestSimilarity) {
        bestSimilarity = similarity;
        bestCandidate = patch;
      }
    }
    assert bestCandidate != null : "best candidate should have been initialized. toPath: " + toPath + ", patches: " + patches;
    return bestCandidate;
  }

  private static int findSimilarity(@NotNull String candidate, @NotNull String toPath) {
    String[] candidateSplit = candidate.split("/");
    String[] toSplit = toPath.split("/");
    int i = candidateSplit.length - 1;
    int j = toSplit.length - 1;
    while (i > 0 && j > 0) {
      if (!candidateSplit[i].equals(toSplit[j])) {
        return candidateSplit.length - 1 - i;
      }
      i--;
      j--;
    }
    return i;
  }

  private static ReviewItem parseVcsReviewItem(ReviewItemRaw raw) {
    String id = raw.permId.id;
    String toPath = raw.toPath;
    String repoName = raw.repositoryName;
    String fromRevision = raw.fromRevision;

    ReviewItem reviewItem = new ReviewItem(id, toPath, repoName);
    for (Revision rev : raw.expandedRevisions) {
      String revision = rev.revision;
      String type = rev.commitType;
      if (!fromRevision.equals(revision) || "Added".equals(type)) {
        reviewItem.addRevision(revision);
      }
    }
    return reviewItem;
  }

  @NotNull
  public static Comment parseComment(@NotNull JsonObject item, boolean isVersioned, boolean reply) {
    BaseComment commentRaw;
    if (!isVersioned) {
      commentRaw = gson.fromJson(item, GeneralComment.class);
    }
    else if (!reply) {
      commentRaw = gson.fromJson(item, TopLevelComment.class);
    }
    else {
      commentRaw = gson.fromJson(item, ReplyComment.class);
    }
    return createComment(commentRaw);
  }

  private static Comment createComment(BaseComment raw) {
    Comment comment = new Comment(raw.user.createUser(), raw.messageAsHtml);
    if (raw.reviewItemId != null) {
      comment.setReviewItemId(raw.reviewItemId.id);
    }
    comment.setLine(raw.toLineRange);
    if (raw.lineRanges != null && raw.lineRanges.length > 0) {
      comment.setRevision(raw.lineRanges[raw.lineRanges.length - 1].revision);
    }

    String permId = null;
    Date date = null;
    if (raw instanceof TopLevelComment) {
      permId = ((TopLevelComment)raw).permaId;
      date = new Date(((TopLevelComment)raw).createDate);
    }
    else if (raw instanceof ReplyComment) {
      permId = ((ReplyComment)raw).permaId.id;
      date = ((ReplyComment)raw).createDate;
    }
    else if (raw instanceof GeneralComment) {
      permId = ((GeneralComment)raw).permaId.id;
      date = ((GeneralComment)raw).createDate;
    }
    comment.setPermId(permId);
    comment.setCreateDate(date);

    for (ReplyComment reply : raw.replies) {
      comment.addReply(createComment(reply));
    }
    return comment;
  }

  @NotNull
  public static Collection<Repository> parseGitRepositories(@NotNull JsonObject object) {
    Repositories repositories = gson.fromJson(object, Repositories.class);
    return ContainerUtil.mapNotNull(repositories.repoData, new Function<RepoRaw, Repository>() {
      @Override
      public Repository fun(RepoRaw repository) {
        return "git".equalsIgnoreCase(repository.type) ? new Repository(repository.name, repository.location) : null;
      }
    });
  }

  @NotNull
  public static CrucibleVersionInfo parseVersion(@NotNull JsonObject object) {
    Version raw = gson.fromJson(object, Version.class);
    return new CrucibleVersionInfo(raw.releaseNumber, raw.buildDate);
  }

  public static RequestEntity createCommentRequest(@NotNull Comment comment, boolean isGeneral) throws UnsupportedEncodingException {
    BaseComment raw;
    if (isGeneral) {
      raw = new GeneralComment();
    }
    else if (comment.getParentCommentId() != null) {
      raw = new ReplyComment();
    }
    else {
      raw = new TopLevelComment();
    }

    raw.message = comment.getMessage();
    raw.draft = false;
    raw.deleted = false;
    raw.defectRaised = false;

    if (!isGeneral && comment.getParentCommentId() == null) {
      raw.reviewItemId = new IdHolder(comment.getReviewItemId());
      raw.toLineRange = comment.getLine();
    }

    if (comment.getParentCommentId() != null) {
      raw.parentCommentId = new IdHolder(comment.getParentCommentId());
    }

    String requestString = gson.toJson(raw);
    return new StringRequestEntity(requestString, "application/json", "UTF-8");
  }

  private static class Version {
    String releaseNumber;
    String buildDate;
  }

  private static class Repositories {
    RepoRaw[] repoData;
  }

  private static class RepoRaw {
    String name;
    String type;
    boolean enabled;
    String location;
    String path;
  }

  private static class ReviewRaw {
    String projectKey;
    String name;
    String description;
    UserRaw author;
    UserRaw moderator;
    UserRaw creator;
    IdHolder permaId;
    String[] permaIdHistory;
    String state;
    String type;
    boolean allowReviewersToJoin;
    int metricsVersion;
    Date createDate;
    Date dueDate;
    Reviewers reviewers;
    ReviewItems reviewItems;
    GeneralComments generalComments;
    VersionedComments versionedComments;
    Transitions transitions;
    Actions actions;
    Stat[] stats;
  }

  private static class GeneralComments {
    GeneralComment[] comments;
  }

  private static class VersionedComments {
    TopLevelComment[] comments;
  }

  private static class GeneralComment extends BaseComment {
    IdHolder permaId;
    Date createDate;
  }

  private static class TopLevelComment extends BaseComment {
    String permaId;
    long createDate;
  }

  private static class ReplyComment extends BaseComment {
    IdHolder permaId;
    Date createDate;
  }

  private static class BaseComment {
    Object metrics;
    String message;
    boolean draft;
    boolean deleted;
    boolean defectRaised;
    boolean defectApproved;
    String readStatus;
    UserRaw user;
    ReplyComment[] replies;
    String messageAsHtml;
    IdHolder reviewItemId;
    String toLineRange;
    LineRange[] lineRanges;
    IdHolder parentCommentId;
  }

  private static class LineRange {
    String revision;
    String range;
  }

  private static class Reviewers {
    UserRaw[] reviewer;
  }

  private static class ReviewItems {
    ReviewItemRaw[] reviewItem;
  }

  private static class ReviewItemRaw {
    IdHolder permId;
    Participant[] participants;
    String repositoryName;
    String fromPath;
    String fromRevision;
    String fromContentUrl;
    String toPath;
    String toRevision;
    String toContentUrl;
    String patchUrl;
    String fileType;
    String commitType;
    String authorName;
    boolean showAsDiff;
    long commitDate;
    Revision[] expandedRevisions;
  }

  private static class IdHolder {
    String id;

    public IdHolder() {
    }

    public IdHolder(String id) {
      this.id = id;
    }
  }

  private static class Participant {
    UserRaw user;
    boolean completed;
  }

  private static class UserRaw {
    String userName;
    String displayName;
    String avatarUrl;
    String url;

    User createUser() {
      return new User(userName, avatarUrl);
    }
  }

  private static class Revision {
    long addDate;
    String revision;
    String path;
    String contentUrl;
    String source;
    long changedLines;
    String fileType;
    String commitType;
  }

  private static class Transitions {
    TransitionData[] transitionData;
  }

  private static class TransitionData {
    String name;
    String displayName;
  }

  private static class Actions {
    ActionData[] actionData;
  }

  private static class ActionData {
    String name;
    String displayName;
  }

  private static class Stat {
    String user;
    int published;
    int drafts;
    int defects;
    int unread;
    int leaveUnread;
    int read;
  }

}
