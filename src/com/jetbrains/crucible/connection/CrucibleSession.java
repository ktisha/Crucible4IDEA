package com.jetbrains.crucible.connection;

import com.jetbrains.crucible.connection.exceptions.RemoteApiException;
import com.jetbrains.crucible.connection.exceptions.RemoteApiLoginException;
import org.jetbrains.annotations.Nullable;

/**
 * User : ktisha
 */
public interface CrucibleSession {
  String REVIEW_SERVICE = "/rest-service/reviews-v1";
  String VERSION = "/versionInfo";
  String AUTH_SERVICE = "/rest-service/auth-v1";
  String LOGIN = "/login";

  void login() throws RemoteApiLoginException;

  @Nullable
  CrucibleVersionInfo getServerVersion() throws RemoteApiException;
}
