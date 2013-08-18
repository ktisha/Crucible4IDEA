package com.jetbrains.crucible.utils;

import com.intellij.openapi.actionSystem.DataKey;
import com.jetbrains.crucible.model.Review;

/**
 * User: ktisha
 */
public interface CrucibleDataKeys {

  DataKey<Review> REVIEW = DataKey.create("crucible.Review");
}
