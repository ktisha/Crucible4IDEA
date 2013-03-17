package com.jetbrains.crucible.model;

/**
 * User : ktisha
 */
public class CrucibleVersionInfo {
  private final String myBuildDate;
  private final String myReleaseNumber;

  public CrucibleVersionInfo(String releaseNumber, String buildDate) {
    this.myBuildDate = buildDate;
    this.myReleaseNumber = releaseNumber;
  }
}
