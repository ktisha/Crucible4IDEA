package com.jetbrains.crucible.connection;

import com.intellij.openapi.project.Project;
import com.jetbrains.crucible.connection.exceptions.CrucibleApiException;
import org.jetbrains.annotations.Nullable;

/**
 * User : ktisha
 */
public class CrucibleTestConnector {

  public enum ConnectionState {
    NOT_FINISHED,
    SUCCEEDED,
    FAILED,
    INTERRUPTED,
  }

  private ConnectionState myConnectionState = ConnectionState.NOT_FINISHED;
  private Exception myException;
  private final Project myProject;

  public CrucibleTestConnector(Project project) {
    myProject = project;
  }

  public ConnectionState getConnectionState() {
    return myConnectionState;
  }

  public void run() {
    try {
      testConnect();
      if (myConnectionState != ConnectionState.INTERRUPTED) {
        myConnectionState = ConnectionState.SUCCEEDED;
      }
    }
    catch (CrucibleApiException e) {
      if (myConnectionState != ConnectionState.INTERRUPTED) {
        myConnectionState = ConnectionState.FAILED;
        myException = e;
      }
    }
  }

  public void setInterrupted() {
    myConnectionState = ConnectionState.INTERRUPTED;
  }

  @Nullable
  public String getErrorMessage() {
    return myException == null ? null : myException.getMessage();
  }

  public void testConnect() throws CrucibleApiException {
    final CrucibleSession session = new CrucibleSessionImpl(myProject);
    session.login();
    session.getServerVersion();
  }
}


