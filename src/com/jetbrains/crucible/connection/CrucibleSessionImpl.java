
package com.jetbrains.crucible.connection;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.jetbrains.crucible.configuration.CrucibleSettings;
import com.jetbrains.crucible.connection.exceptions.CrucibleApiException;
import com.jetbrains.crucible.connection.exceptions.CrucibleApiLoginException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

/**
 * User : ktisha
 */
public class CrucibleSessionImpl implements CrucibleSession {
  private final Project myProject;
  private static final Logger LOG = Logger.getInstance(CrucibleSessionImpl.class.getName());

  CrucibleSessionImpl(Project project) {
    myProject = project;
  }

  @Override
  public void login() throws CrucibleApiLoginException {
    try {
      final String username = getUsername();
      final String password = getPassword();
      if (username == null || password == null) {
        throw new CrucibleApiLoginException("Username or Password is empty");
      }
      final String loginUrlPrefix = UrlUtil.removeUrlTrailingSlashes(getHostUrl()) + AUTH_SERVICE + LOGIN;

      final String loginUrl;
      try {
        loginUrl = loginUrlPrefix + "?userName=" + URLEncoder.encode(username, "UTF-8") + "&password="
                   + URLEncoder.encode(password, "UTF-8");
      }
      catch (UnsupportedEncodingException e) {
        throw new RuntimeException("URLEncoding problem: " + e.getMessage());
      }

      Document doc = buildSaxResponse(loginUrl);
      String exception = getExceptionMessages(doc);
      if (exception != null) {
        throw new CrucibleApiLoginException(exception);
      }
      XPath xpath = XPath.newInstance("/loginResult/token");
      List<?> elements = xpath.selectNodes(doc);
      if (elements == null) {
        throw new CrucibleApiLoginException("Server did not return any authentication token");
      }
      if (elements.size() != 1) {
        throw new CrucibleApiLoginException("Server returned unexpected number of authentication tokens ("
                                          + elements.size() + ")");
      }
    }
    catch (IOException e) {
      throw new CrucibleApiLoginException(getHostUrl() + ":" + e.getMessage(), e);
    }
    catch (JDOMException e) {
      throw new CrucibleApiLoginException("Server:" + getHostUrl() + " returned malformed response", e);
    }
    catch (CrucibleApiException e) {
      throw new CrucibleApiLoginException(e.getMessage(), e);
    }
  }

  @Nullable
  @Override
  public CrucibleVersionInfo getServerVersion() throws CrucibleApiException {
    String requestUrl = UrlUtil.removeUrlTrailingSlashes(getHostUrl()) + REVIEW_SERVICE + VERSION;
    try {
      Document doc = buildSaxResponse(requestUrl);
      XPath xpath = XPath.newInstance("versionInfo");

      @SuppressWarnings("unchecked")
      List<Element> elements = xpath.selectNodes(doc);

      if (elements != null && !elements.isEmpty()) {
        return CrucibleRestXmlHelper.parseVersionNode(elements.get(0));
      }
    }
    catch (JDOMException e) {
      LOG.warn(e);
    }
    catch (IOException e) {
      LOG.warn(e);
    }
    return null;
  }

  protected Document buildSaxResponse(@NotNull final String urlString) throws IOException, JDOMException {
    final SAXBuilder builder = new SAXBuilder();
    final URL repositoryUrl = new URL(urlString);
    return builder.build(repositoryUrl);
  }

  private String getUsername() {
    return CrucibleSettings.getInstance(myProject).USERNAME;
  }

  private String getPassword() {
    return CrucibleSettings.getInstance(myProject).getPassword();
  }

  private String getHostUrl() {
    return CrucibleSettings.getInstance(myProject).SERVER_URL;
  }

  @Nullable
  private static String getExceptionMessages(@NotNull final Document doc) throws JDOMException {
    XPath xpath = XPath.newInstance("/loginResult/error");
    @SuppressWarnings("unchecked")
    List<Element> elements = xpath.selectNodes(doc);

    if (elements != null && elements.size() > 0) {
      StringBuilder exceptionMsg = new StringBuilder();
      for (Element e : elements) {
        exceptionMsg.append(e.getText());
        exceptionMsg.append("\n");
      }
      return exceptionMsg.toString();
    }
    return null;
  }
}