package com.jetbrains.crucible.model;

import org.jetbrains.annotations.NotNull;

/**
 * User abstraction.
 */
public class User {
  @NotNull
  protected String myUserName;

  public User(@NotNull String userName) {
    myUserName = userName;
  }

  @NotNull
  public String getUserName() {
    return myUserName;
  }

  @Override
  public String toString() {
    return "User [[" + myUserName+ "]]";
  }
}