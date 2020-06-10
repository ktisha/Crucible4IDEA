package com.jetbrains.crucible.model;

import org.jetbrains.annotations.NotNull;

/**
 * @author Kirill Likhodedov
 */
public class Repository {

  @NotNull private final String myName;
  @NotNull private final String myUrl;

  public Repository(@NotNull String name, @NotNull String url) {
    myName = name;
    myUrl = url;
  }

  @NotNull
  public String getUrl() {
    return myUrl;
  }

  @NotNull
  public String getName() {
    return myName;
  }

}
