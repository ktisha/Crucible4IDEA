package com.jetbrains.crucible.model;

import org.jetbrains.annotations.NotNull;

/**
 * User : ktisha
 */
public enum CrucibleFilter {
  // Inbox
  ToReview("toReview", "To Review"), //Reviews on which the current user is an uncompleted reviewer.
  ReadyToClose("toSummarize", "Ready To Close"), //Completed reviews which are ready for the current user to summarize.
  InDraft("drafts", "In Draft"), //Draft reviews created by the current user.
  RequireApprovalReview("requireMyApproval", "Review Required My Approval"), //Reviews waiting to be approved by the current user.

  // Outbox
  OutForReview("outForReview", "Out For Review"), //Reviews with uncompleted reviewers, on which the current reviewer is the moderator.
  Completed("completed", "Completed"), //Open reviews where the current user is a completed reviewer.

  // Archive
  Closed("closed", "Closed"), //Closed reviews created by the current user.
  Abandoned("trash", "Abandoned"); //Abandoned reviews created by the current user.

  private final String myFilterUrl;
  private final String myFilterName;

  CrucibleFilter(@NotNull final String filterUrl, @NotNull final String filterName) {
    myFilterUrl = filterUrl;
    myFilterName = filterName;
  }

  @NotNull
  public String getFilterUrl() {
    return myFilterUrl;
  }

  @NotNull
  public String getFilterName() {
    return myFilterName;
  }
}
