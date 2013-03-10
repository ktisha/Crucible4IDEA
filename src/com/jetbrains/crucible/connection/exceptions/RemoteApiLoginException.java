package com.jetbrains.crucible.connection.exceptions;

/**
 * User : ktisha
 */
public class RemoteApiLoginException extends RemoteApiException {

  public RemoteApiLoginException(String message) {
    super(message);
  }

  public RemoteApiLoginException(String message, Throwable throwable) {
    super(message, throwable);
  }
}