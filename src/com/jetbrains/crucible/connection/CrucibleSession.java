package com.jetbrains.crucible.connection;

import com.jetbrains.crucible.connection.exceptions.CrucibleApiException;
import com.jetbrains.crucible.connection.exceptions.CrucibleApiLoginException;
import com.jetbrains.crucible.model.*;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

/**
 * User : ktisha
 */
public interface CrucibleSession {
  String REVIEW_SERVICE = "/rest-service/reviews-v1";
  String DETAIL_REVIEW_INFO = "/details";
  String VERSION = "/versionInfo";
  String AUTH_SERVICE = "/rest-service/auth-v1";
  String LOGIN = "/login";
  String FILTERED_REVIEWS = "/filter";
  String COMMENTS = "/comments";
  void login() throws CrucibleApiLoginException;

  @Nullable
  CrucibleVersionInfo getServerVersion();

  List<BasicReview> getReviewsForFilter(@NotNull final CrucibleFilter filter) throws CrucibleApiException, JDOMException, IOException;
  Review getDetailsForReview(@NotNull final String permId) throws JDOMException, IOException;

  boolean postComment(@NotNull final Comment comment, String reviewId);
}
