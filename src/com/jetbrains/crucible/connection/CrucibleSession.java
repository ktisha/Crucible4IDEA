package com.jetbrains.crucible.connection;

import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.crucible.connection.exceptions.CrucibleApiLoginException;
import com.jetbrains.crucible.model.*;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * User : ktisha
 */
public interface CrucibleSession {
  String REVIEW_SERVICE = "/rest-service/reviews-v1";
  String REVIEW_ITEMS = "/reviewitems";
  String DETAIL_REVIEW_INFO = "/details";
  String VERSION = "/versionInfo";
  String AUTH_SERVICE = "/rest-service/auth-v1";
  String LOGIN = "/login";
  String FILTERED_REVIEWS = "/filter";
  String COMMENTS = "/comments";
  String REPLIES = "/replies";
  String REPOSITORIES = "/rest-service/repositories-v1";
  void login() throws CrucibleApiLoginException;

  @Nullable
  CrucibleVersionInfo getServerVersion();

  List<BasicReview> getReviewsForFilter(@NotNull final CrucibleFilter filter) throws JDOMException, IOException;
  Review getDetailsForReview(@NotNull final String permId) throws JDOMException, IOException;

  boolean postComment(@NotNull final Comment comment, boolean isGeneral, String reviewId);

  void fillRepoHash() throws IOException, JDOMException;
  Map<String,VirtualFile> getRepoHash();
}
