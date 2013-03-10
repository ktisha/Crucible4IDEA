package com.jetbrains.crucible.connection;

/**
 * User : ktisha
 */
@SuppressWarnings("serial")
public class CrucibleVersionInfo {
  private final String myBuildDate;
  private final String myReleaseNumber;

  public CrucibleVersionInfo(String releaseNumber, String buildDate) {
    this.myBuildDate = buildDate;
    this.myReleaseNumber = releaseNumber;
  }
}
