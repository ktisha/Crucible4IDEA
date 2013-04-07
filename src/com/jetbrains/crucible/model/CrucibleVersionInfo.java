package com.jetbrains.crucible.model;

/**
 * User : ktisha
 */
public class CrucibleVersionInfo {
  private final String myBuildDate;
  private final String myReleaseNumber;

  public CrucibleVersionInfo(String releaseNumber, String buildDate) {
    myBuildDate = buildDate;
    myReleaseNumber = releaseNumber;
  }
}
