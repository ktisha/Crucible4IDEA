package com.jetbrains.crucible.connection;

import org.jetbrains.annotations.NotNull;


/**
 * User: ktisha
 */
public abstract class UrlUtil {
  private UrlUtil() {
  }

  public static String removeUrlTrailingSlashes(@NotNull String address) {
    while (address.endsWith("/")) {
      address = address.substring(0, address.length() - 1);
    }
    return address;
  }
}
