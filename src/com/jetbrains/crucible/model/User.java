package com.jetbrains.crucible.model;

import org.jetbrains.annotations.NotNull;

/**
 * User : ktisha
 * User abstraction.
 */
public class User {
  @NotNull
  protected final String myUserName;

  public User(@NotNull final String userName) {
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

  @Override
  public int hashCode() {
    return myUserName.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof User)
      return myUserName.equals(((User)obj).getUserName());
    return super.equals(obj);
  }
}