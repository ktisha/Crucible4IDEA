package com.jetbrains.crucible.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User : ktisha
 * User abstraction.
 */
public class User {

  @NotNull protected final String myUserName;
  @Nullable private final String myAvatar;

  public User(@NotNull final String userName, @Nullable String avatar) {
    myUserName = userName;
    myAvatar = avatar;
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

  @Nullable
  public String getAvatar() {
    return myAvatar;
  }
}