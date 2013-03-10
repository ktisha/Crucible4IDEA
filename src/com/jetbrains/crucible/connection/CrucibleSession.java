package com.jetbrains.crucible.connection;

import com.jetbrains.crucible.connection.exceptions.CrucibleApiException;
import com.jetbrains.crucible.connection.exceptions.CrucibleApiLoginException;
import org.jetbrains.annotations.Nullable;

/**
 * User : ktisha
 */
public interface CrucibleSession {
  String REVIEW_SERVICE = "/rest-service/reviews-v1";
  String VERSION = "/versionInfo";
  String AUTH_SERVICE = "/rest-service/auth-v1";
  String LOGIN = "/login";

  void login() throws CrucibleApiLoginException;

  @Nullable
  CrucibleVersionInfo getServerVersion() throws CrucibleApiException;
}
