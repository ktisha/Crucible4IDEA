package com.jetbrains.crucible.model;

import org.jetbrains.annotations.NotNull;

/**
 * User : ktisha
 */
public class Reviewer extends User {

  public Reviewer(@NotNull final String userName) {
    super(userName, null);
  }

  @Override
  public String toString() {
    return "Reviewer [[" + myUserName + "]]";
  }
}