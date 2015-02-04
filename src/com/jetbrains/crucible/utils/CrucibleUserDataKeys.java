package com.jetbrains.crucible.utils;

import com.intellij.openapi.util.Key;
import com.jetbrains.crucible.model.Review;

/**
 * User: ktisha
 */
public interface CrucibleUserDataKeys {
  Key<Review> REVIEW = Key.create("crucible.Review");
}
