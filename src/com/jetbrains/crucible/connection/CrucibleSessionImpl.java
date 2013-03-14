
package com.jetbrains.crucible.connection;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.jetbrains.crucible.configuration.CrucibleSettings;
import com.jetbrains.crucible.connection.exceptions.CrucibleApiException;
import com.jetbrains.crucible.connection.exceptions.CrucibleApiLoginException;
import com.jetbrains.crucible.model.BasicReview;
import com.jetbrains.crucible.model.Review;
import com.jetbrains.crucible.model.User;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * User : ktisha
 *
 * http://docs.atlassian.com/fisheye-crucible/latest/wadl/crucible.html
 */
public class CrucibleSessionImpl implements CrucibleSession {
  private final Project myProject;
  private String myAuthentification;
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
      final String loginUrlPrefix = getHostUrl() + AUTH_SERVICE + LOGIN;

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
      myAuthentification = ((Element)elements.get(0)).getText();
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
    String requestUrl = getHostUrl() + REVIEW_SERVICE + VERSION;
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

  private String getAuthHeaderValue() {
    return "Basic " + encode(getUsername() + ":" + getPassword());
  }

  public static String encode(String str2encode) {
    try {
      Base64 base64 = new Base64();
      byte[] bytes = base64.encode(str2encode.getBytes("UTF-8"));
      return new String(bytes);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("UTF-8 is not supported", e);
    }
  }

  protected Document buildSaxResponse(@NotNull final String urlString) throws IOException, JDOMException {
    final SAXBuilder builder = new SAXBuilder();
    GetMethod method = new GetMethod(urlString);
    adjustHttpHeader(method);
    HttpClient client = new HttpClient();
    client.executeMethod(method);

    return builder.build(method.getResponseBodyAsStream());
  }

  protected void adjustHttpHeader(HttpMethod method) {
    method.addRequestHeader(new Header("Authorization", getAuthHeaderValue()));
  }

  private String getUsername() {
    return CrucibleSettings.getInstance(myProject).USERNAME;
  }

  private String getPassword() {
    return CrucibleSettings.getInstance(myProject).getPassword();
  }

  private String getHostUrl() {
    return UrlUtil.removeUrlTrailingSlashes(CrucibleSettings.getInstance(myProject).SERVER_URL);
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

  public List<BasicReview> getReviewsForFilter(CrucibleFilter filter) throws CrucibleApiException, JDOMException, IOException {
    String url = getHostUrl() + REVIEW_SERVICE + FILTERED_REVIEWS;
    String urlFilter = filter.getFilterUrl();
    if (!StringUtils.isEmpty(urlFilter)) {
      url += "/" + urlFilter;
    }
    final Document doc = buildSaxResponse(url);
    XPath xpath = XPath.newInstance("/reviews/reviewData");

    @SuppressWarnings("unchecked")
    List<Element> elements = xpath.selectNodes(doc);
    List<BasicReview> reviews = new ArrayList<BasicReview>();

    if (elements != null && !elements.isEmpty()) {
      for (Element element : elements) {
        reviews.add(parseBasicReview(element));
      }
    }
    return reviews;
  }

  public Review getDetailsForReview(String permId) throws CrucibleApiException, JDOMException, IOException {
    String url = getHostUrl() + REVIEW_SERVICE + "/" + permId + DETAIL_REVIEW_INFO;
    final Document doc = buildSaxResponse(url);
    //XPath xpath = XPath.newInstance("/detailedReviewData/reviewItems");

    @SuppressWarnings("unchecked")
    //List<Element> reviewItems = xpath.selectNodes(doc);
    final Element node = (Element)XPath.newInstance("/detailedReviewData").selectSingleNode(doc);
    final User author = CrucibleRestXmlHelper.parseUserNode(node);

    final User moderator = (node.getChild("moderator") != null)
                           ? CrucibleRestXmlHelper.parseUserNode(node.getChild("moderator")) : null;

    Review review = new Review(getHostUrl(), permId, author, moderator);
    final String description = CrucibleRestXmlHelper.getChildText(node, "description");
    final int i = description.indexOf("id=") + 3;
    String revision = description.substring(i, description.indexOf("|"));
    review.setRevisionNumber(revision);
    //if (reviewItems != null && !reviewItems.isEmpty()) {
    //  for (Element element : reviewItems) {
    //    @SuppressWarnings("unchecked")
    //    final List<Element> items = element.getChildren("reviewItem");
    //    for (Element item : items) {
    //      final String path = CrucibleRestXmlHelper.getChildText(item, "toPath");
    //      review.addFile(path);
    //    }
    //  }
    //}
    return review;
  }

  private BasicReview parseBasicReview(Element element) throws CrucibleApiException {
    try {
      return CrucibleRestXmlHelper.parseBasicReview(getHostUrl(), element);
    } catch (ParseException e) {
      throw new CrucibleApiException(e.getMessage());
    }
  }
}