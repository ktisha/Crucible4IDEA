package com.jetbrains.crucible.model;

import org.jetbrains.annotations.NotNull;

/**
 * User : ktisha
 */
public enum CrucibleFilter {
  ToReview("toReview", "To Review"),
  RequireApprovalReview("requireMyApproval", "Review Required My Approval"),
  OutForReview("outForReview", "Out For Review"),
  Closed("closed", "Closed");

  private String myFilterUrl;
  private String myFilterName;

  CrucibleFilter(@NotNull final String filterUrl, @NotNull final String filterName) {
    this.myFilterUrl = filterUrl;
    this.myFilterName = filterName;
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
