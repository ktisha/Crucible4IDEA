package com.jetbrains.crucible.connection;

import com.google.gson.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.crucible.configuration.CrucibleSettings;
import com.jetbrains.crucible.connection.exceptions.CrucibleApiException;
import com.jetbrains.crucible.connection.exceptions.CrucibleApiLoginException;
import com.jetbrains.crucible.model.*;
import com.jetbrains.crucible.ui.UiUtils;
import git4idea.GitUtil;
import git4idea.commands.GitRemoteProtocol;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User : ktisha
 *
 * Crucible API
 * http://docs.atlassian.com/fisheye-crucible/latest/wadl/crucible.html
 */
public class CrucibleSessionImpl implements CrucibleSession {
  private final Project myProject;
  private static final Logger LOG = Logger.getInstance(CrucibleSessionImpl.class.getName());
  private static final int CONNECTION_TIMEOUT = 5000;

  private final Map<String, VirtualFile> myRepoHash = new HashMap<String, VirtualFile>();


  public CrucibleSessionImpl(@NotNull final Project project) {
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
      final String loginUrl = getLoginUrl(username, password);

      final JsonObject jsonObject = buildJsonResponse(loginUrl);
      final JsonElement authToken = jsonObject.get("token");
      final String errorMessage = getExceptionMessages(jsonObject);
      if (authToken == null || errorMessage != null) {
        throw new CrucibleApiLoginException(errorMessage != null ? errorMessage : "Unknown error");
      }
    }
    catch (IOException e) {
      throw new CrucibleApiLoginException(getHostUrl() + ":" + e.getMessage(), e);
    }
    catch (CrucibleApiException e) {
      throw new CrucibleApiLoginException(e.getMessage(), e);
    }
  }

  private String getLoginUrl(String username, String password) {
    final String loginUrlPrefix = getHostUrl() + AUTH_SERVICE + LOGIN;

    final String loginUrl;
    try {
      loginUrl = loginUrlPrefix + "?userName=" + URLEncoder.encode(username, "UTF-8") + "&password="
                 + URLEncoder.encode(password, "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException("URLEncoding problem: " + e.getMessage());
    }
    return loginUrl;
  }

  @Nullable
  @Override
  public CrucibleVersionInfo getServerVersion() {
    final String requestUrl = getHostUrl() + REVIEW_SERVICE + VERSION;
    try {
      final JsonObject jsonObject = buildJsonResponse(requestUrl);
      return CrucibleJsonUtils.parseVersionNode(jsonObject);
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

  protected JsonObject buildJsonResponse(@NotNull final String urlString) throws IOException {
    final GetMethod method = new GetMethod(urlString);
    executeHttpMethod(method);

    JsonParser parser = new JsonParser();
    return parser.parse(new InputStreamReader(method.getResponseBodyAsStream())).getAsJsonObject();
  }

  private void executeHttpMethod(@NotNull HttpMethodBase method) throws IOException {
    adjustHttpHeader(method);

    final HttpClient client = new HttpClient();
    HttpConnectionManagerParams params = client.getHttpConnectionManager().getParams();
    params.setConnectionTimeout(CONNECTION_TIMEOUT); //set connection timeout (how long it takes to connect to remote host)
    params.setSoTimeout(CONNECTION_TIMEOUT); //set socket timeout (how long it takes to retrieve data from remote host)
    client.executeMethod(method);
  }

  protected JsonObject buildJsonResponseForPost(@NotNull final String urlString,
                                             @NotNull final RequestEntity requestEntity) throws IOException {
    final PostMethod method = new PostMethod(urlString);
    method.setRequestEntity(requestEntity);
    executeHttpMethod(method);
    JsonParser parser = new JsonParser();
    return parser.parse(new InputStreamReader(method.getResponseBodyAsStream())).getAsJsonObject();
  }

  protected void adjustHttpHeader(@NotNull final HttpMethod method) {
    method.addRequestHeader(new Header("Authorization", getAuthHeaderValue()));
    method.addRequestHeader(new Header("accept", "application/json"));
  }

  protected String getUsername() {
    return CrucibleSettings.getInstance().USERNAME;
  }

  protected String getPassword() {
    return CrucibleSettings.getInstance().getPassword();
  }

  protected String getHostUrl() {
    return UrlUtil.removeUrlTrailingSlashes(CrucibleSettings.getInstance().SERVER_URL);
  }

  @Nullable
  public static String getExceptionMessages(@NotNull final JsonObject jsonObject) {
    final JsonElement error = jsonObject.get("error");
    final JsonElement statusCode = jsonObject.get("status-code");
    if (error != null) {
      return error.getAsString();
    }
    else if (statusCode != null && "500".equals(statusCode.getAsString())) {
      final JsonPrimitive message = jsonObject.getAsJsonPrimitive("message");
      return message.getAsString();
    }
    return null;
  }

  @Nullable
  public Comment postComment(@NotNull final Comment comment, boolean isGeneral,
                             @NotNull final String reviewId) {

    String url = getHostUrl() + REVIEW_SERVICE + "/" + reviewId;
    final String parentCommentId = comment.getParentCommentId();
    final boolean isVersioned = !isGeneral && parentCommentId == null;
    if (isVersioned)
      url += REVIEW_ITEMS + "/" + comment.getReviewItemId();

    url += COMMENTS;


    if (parentCommentId != null) {
      url += "/" + parentCommentId + REPLIES;
    }

    try {
      final RequestEntity request = CrucibleJsonUtils.createCommentRequest(comment, !isVersioned);

      final JsonObject jsonObject = buildJsonResponseForPost(url, request);
      final String errorMessage = getExceptionMessages(jsonObject);
      if (errorMessage != null) {
        UiUtils.showBalloon(myProject, "Sorry, comment wasn't added:\n" + errorMessage, MessageType.ERROR);
        return null;
      }

      return CrucibleJsonUtils.parseComment(jsonObject, isVersioned);
    }
    catch (IOException e) {
      LOG.warn(e.getMessage());
    }
    return null;
  }

  @Override
  public void fillRepoHash() throws IOException {
    String url = getHostUrl() + REPOSITORIES;

    final JsonObject jsonObject = buildJsonResponse(url);
    final JsonArray elements = jsonObject.getAsJsonArray("repoData");

    if (elements != null && elements.size() > 0) {
      for (int i = 0; i != elements.size(); ++i) {
        final JsonObject element = elements.get(i).getAsJsonObject();

        final String enabled = CrucibleJsonUtils.getChildText(element, "enabled");
        if (Boolean.parseBoolean(enabled)) {
          final String name = CrucibleJsonUtils.getChildText(element, "name");
          final String type = CrucibleJsonUtils.getChildText(element, "type");
          if ("git".equals(type)) {
            final VirtualFile localPath = getLocalPath(name);
            if (localPath != null)
              myRepoHash.put(name, localPath);
          }
        }
      }
    }
  }

  @Nullable
  protected VirtualFile getLocalPath(@NotNull final String name) throws IOException {
    String url = getHostUrl() + REPOSITORIES + "/" + name;
    final JsonObject jsonObject = buildJsonResponse(url);
    String location = CrucibleJsonUtils.getChildText(jsonObject, "location");
    final GitRepositoryManager manager = GitUtil.getRepositoryManager(myProject);
    final List<GitRepository> repositories = manager.getRepositories();
    location = unifyLocation(location);
    for (GitRepository repo : repositories) {
      final GitRemote origin = GitUtil.findRemoteByName(repo, GitRemote.ORIGIN_NAME);
      if (origin != null && location != null) {
        final String originFirstUrl = origin.getFirstUrl();
        if (originFirstUrl == null) continue;
        final String originLocation  = unifyLocation(originFirstUrl);
        if (location.equals(originLocation)) {
          return repo.getRoot();
        }
      }
    }
    return null;
  }

  @Nullable
  private static String unifyLocation(@NotNull final String location) {
    final GitRemoteProtocol protocol = GitRemoteProtocol.fromUrl(location);
    if (protocol == null) return null;
    switch (protocol) {
      case GIT:
        return StringUtil.trimEnd(StringUtil.trimStart(location, "git://"), ".git");
      case HTTP:
        Pattern pattern = Pattern.compile("https?://(.*)\\.git");
        Matcher matcher = pattern.matcher(location);
        boolean found = matcher.find();
        return found ? matcher.group(1) : null;
      case SSH:
        pattern = Pattern.compile("git@(.*)?:(.*)(\\.git)?");
        matcher = pattern.matcher(location);
        found = matcher.find();
        return found ? matcher.group(1) + "/" + matcher.group(2) : null;
      default:
    }
    return null;
  }


  public List<BasicReview> getReviewsForFilter(@NotNull final CrucibleFilter filter) throws IOException {
    String url = getHostUrl() + REVIEW_SERVICE + FILTERED_REVIEWS;
    final String urlFilter = filter.getFilterUrl();
    if (!StringUtils.isEmpty(urlFilter)) {
      url += "/" + urlFilter;
    }
    List<BasicReview> reviews = new ArrayList<BasicReview>();

    final JsonObject jsonElement = buildJsonResponse(url);
    final JsonArray reviewData = jsonElement.getAsJsonArray("reviewData");
    if (reviewData != null) {
      for (int i = 0; i != reviewData.size(); ++i) {
        reviews.add(CrucibleJsonUtils.parseBasicReview(reviewData.get(i)));
      }
    }
    return reviews;
  }

  public Review getDetailsForReview(@NotNull final String permId) throws IOException {
    String url = getHostUrl() + REVIEW_SERVICE + "/" + permId + DETAIL_REVIEW_INFO;

    final JsonObject jsonObject = buildJsonResponse(url);
    final User author = CrucibleJsonUtils.parseUserNode(jsonObject.getAsJsonObject("author"));

    final User moderator = (jsonObject.get("moderator") != null) ?
                           CrucibleJsonUtils.parseUserNode(jsonObject.getAsJsonObject("moderator")) : null;


    final Review review = new Review(permId, author, moderator);
    final JsonObject reviewItems = jsonObject.getAsJsonObject("reviewItems");
    if (reviewItems != null) {
      CrucibleJsonUtils.addReviewItems(reviewItems.getAsJsonArray("reviewItem"), review);
    }

    CrucibleJsonUtils.addGeneralComments(jsonObject, review);
    CrucibleJsonUtils.addVersionedComments(jsonObject, review);

    return review;
  }


  public Map<String, VirtualFile> getRepoHash() {
    return myRepoHash;
  }
}