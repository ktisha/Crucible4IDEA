package com.jetbrains.crucible.connection;

import com.intellij.openapi.project.Project;
import com.jetbrains.crucible.configuration.CrucibleSettings;
import com.jetbrains.crucible.connection.exceptions.CrucibleApiException;
import com.jetbrains.crucible.model.BasicReview;
import com.jetbrains.crucible.model.Review;
import org.jdom.JDOMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: ktisha
 */
public class CrucibleManager {
  private final Project myProject;
  private static CrucibleManager ourInstance;
  private CrucibleManager(Project project) {
    myProject = project;
  }

  public static CrucibleManager getInstance(Project project) {
    if (ourInstance == null) {
      ourInstance = new CrucibleManager(project);
    }
    return ourInstance;
  }

  public List<BasicReview> getReviewsForFilter(CrucibleFilter filter) throws CrucibleApiException, JDOMException {
    final CrucibleSession session = getSession();
    try {
      return session.getReviewsForFilter(filter);
    }
    catch (IOException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
    return null;
  }

  public Review getDetailsForReview(String permId) throws CrucibleApiException, JDOMException {
    final CrucibleSession session = getSession();
    try {
      return session.getDetailsForReview(permId);
    }
    catch (IOException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
    return null;
  }

  private final Map<String, CrucibleSession> sessions = new HashMap<String, CrucibleSession>();

  public synchronized CrucibleSession getSession() throws CrucibleApiException {
    final CrucibleSettings crucibleSettings = CrucibleSettings.getInstance(myProject);
    String key = crucibleSettings.SERVER_URL + crucibleSettings.USERNAME + crucibleSettings.getPassword();
    CrucibleSession session = sessions.get(key);
    if (session == null) {
      session = new CrucibleSessionImpl(myProject);
      session.login();
      sessions.put(key, session);
    }
    return session;
  }
}
