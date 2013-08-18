package com.jetbrains.crucible.connection;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.crucible.configuration.CrucibleSettings;
import com.jetbrains.crucible.connection.exceptions.CrucibleApiException;
import com.jetbrains.crucible.model.BasicReview;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.CrucibleFilter;
import com.jetbrains.crucible.model.Review;
import com.jetbrains.crucible.ui.UiUtils;
import com.jetbrains.crucible.utils.CrucibleBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: ktisha
 */
public class CrucibleManager {
  private final Project myProject;
  private final Map<String, CrucibleSession> mySessions = new HashMap<String, CrucibleSession>();

  private static final Logger LOG = Logger.getInstance(CrucibleManager.class.getName());

  // implicitly constructed by pico container
  @SuppressWarnings("UnusedDeclaration")
  private CrucibleManager(@NotNull final Project project) {
    myProject = project;
  }

  public static CrucibleManager getInstance(@NotNull final Project project) {
    return ServiceManager.getService(project, CrucibleManager.class);
  }

  @Nullable
  public List<BasicReview> getReviewsForFilter(@NotNull final CrucibleFilter filter) {
    try {
      final CrucibleSession session = getSession();
      if (session != null) {
        return session.getReviewsForFilter(filter);
      }
    }
    catch (IOException e) {
      LOG.warn(e.getMessage());
      UiUtils.showBalloon(myProject, CrucibleBundle.message("crucible.connection.error.message.$0", e.getMessage()), MessageType.ERROR);
    }
    catch (CrucibleApiException e) {
      LOG.warn(e.getMessage());
      UiUtils.showBalloon(myProject, CrucibleBundle.message("crucible.connection.error.message.$0", e.getMessage()), MessageType.ERROR);
    }
    return null;
  }

  @Nullable
  public Review getDetailsForReview(@NotNull final String permId) {
    try {
      final CrucibleSession session = getSession();
      if (session != null) {
        return session.getDetailsForReview(permId);
      }
    }
    catch (CrucibleApiException e) {
      LOG.warn(e.getMessage());
      UiUtils.showBalloon(myProject, CrucibleBundle.message("crucible.connection.error.message.$0", e.getMessage()), MessageType.ERROR);

    }
    catch (IOException e) {
      LOG.warn(e.getMessage());
      UiUtils.showBalloon(myProject, CrucibleBundle.message("crucible.connection.error.message.$0", e.getMessage()), MessageType.ERROR);
    }
    return null;
  }

  @Nullable
  public Comment postComment(@NotNull final Comment comment, boolean isGeneral, String reviewId) {
    try {
      final CrucibleSession session = getSession();
      if (session != null) {
        return session.postComment(comment, isGeneral, reviewId);
      }
    }
    catch (CrucibleApiException e) {
      LOG.warn(e.getMessage());
      UiUtils.showBalloon(myProject, CrucibleBundle.message("crucible.connection.error.message.$0", e.getMessage()), MessageType.ERROR);
    }
    return null;
  }

  public void completeReview(String reviewId) {
    try {
      final CrucibleSession session = getSession();
      if (session != null) {
        session.completeReview(reviewId);
      }
    }
    catch (CrucibleApiException e) {
      LOG.warn(e.getMessage());
      UiUtils.showBalloon(myProject, CrucibleBundle.message("crucible.connection.error.message.$0", e.getMessage()), MessageType.ERROR);
    }
  }

  @Nullable
  public CrucibleSession getSession() throws CrucibleApiException {
    final CrucibleSettings crucibleSettings = CrucibleSettings.getInstance();
    final String serverUrl = crucibleSettings.SERVER_URL;
    final String username = crucibleSettings.USERNAME;
    if (StringUtil.isEmptyOrSpaces(serverUrl) || StringUtil.isEmptyOrSpaces(username)) {
      UiUtils.showBalloon(myProject, CrucibleBundle.message("crucible.define.host.username"), MessageType.ERROR);
      return null;
    }
    try {
      new URL(serverUrl);
    }
    catch (MalformedURLException e) {
      UiUtils.showBalloon(myProject, CrucibleBundle.message("crucible.wrong.host"), MessageType.ERROR);
      return null;
    }
    String key = serverUrl + username + crucibleSettings.getPassword();
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
    }
    return session;
  }

  public Map<String,VirtualFile> getRepoHash() {
    try {
      final CrucibleSession session = getSession();
      if (session != null) {
        return session.getRepoHash();
      }
    }
    catch (CrucibleApiException e) {
      LOG.warn(e.getMessage());
    }
    return Collections.emptyMap();
  }

  public void publishComment(@NotNull Review review, @NotNull Comment comment) throws IOException, CrucibleApiException {
    CrucibleSession session = getSession();
    if (session != null) {
      session.publishComment(review, comment);
    }
  }
}
