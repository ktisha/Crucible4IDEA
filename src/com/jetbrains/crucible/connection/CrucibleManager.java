package com.jetbrains.crucible.connection;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.crucible.configuration.CrucibleSettings;
import com.jetbrains.crucible.connection.exceptions.CrucibleApiException;
import com.jetbrains.crucible.model.BasicReview;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.CrucibleFilter;
import com.jetbrains.crucible.model.Review;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: ktisha
 */
public class CrucibleManager {
  private final Project myProject;
  private static CrucibleManager ourInstance;
  private final Map<String, CrucibleSession> mySessions = new HashMap<String, CrucibleSession>();

  private static final Logger LOG = Logger.getInstance(CrucibleManager.class.getName());

  private CrucibleManager(@NotNull final Project project) {
    myProject = project;
  }

  public static CrucibleManager getInstance(@NotNull final Project project) {
    if (ourInstance == null) {
      ourInstance = new CrucibleManager(project);
    }
    return ourInstance;
  }

  @Nullable
  public List<BasicReview> getReviewsForFilter(@NotNull final CrucibleFilter filter) throws CrucibleApiException, JDOMException {
    final CrucibleSession session = getSession();
    try {
      return session.getReviewsForFilter(filter);
    }
    catch (IOException e) {
      LOG.warn(e.getMessage());
    }
    return null;
  }

  @Nullable
  public Review getDetailsForReview(@NotNull final String permId) throws CrucibleApiException, JDOMException {
    final CrucibleSession session = getSession();
    try {
      return session.getDetailsForReview(permId);
    }
    catch (IOException e) {
      LOG.warn(e.getMessage());
    }
    return null;
  }

  public boolean postComment(@NotNull final Comment comment, boolean isGeneral, String reviewId) {
    try {
      final CrucibleSession session = getSession();
      return session.postComment(comment, isGeneral, reviewId);
    }
    catch (CrucibleApiException e) {
      LOG.warn(e.getMessage());
    }
    return false;
  }

  public CrucibleSession getSession() throws CrucibleApiException {
    final CrucibleSettings crucibleSettings = CrucibleSettings.getInstance(myProject);
    String key = crucibleSettings.SERVER_URL + crucibleSettings.USERNAME + crucibleSettings.getPassword();
    CrucibleSession session = mySessions.get(key);
    if (session == null) {
      session = new CrucibleSessionImpl(myProject);
      session.login();
      mySessions.put(key, session);
      try {
        session.fillRepoHash();
      }
      catch (IOException e) {
        LOG.warn(e.getMessage());
      }
      catch (JDOMException e) {
        LOG.warn(e.getMessage());
      }
    }
    return session;
  }

  public Map<String,VirtualFile> getRepoHash() {
    try {
      final CrucibleSession session = getSession();
      return session.getRepoHash();
    }
    catch (CrucibleApiException e) {
      LOG.warn(e.getMessage());
    }
    return Collections.emptyMap();
  }

}
