package com.jetbrains.crucible.connection;

import com.google.gson.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diff.impl.patch.PatchReader;
import com.intellij.openapi.diff.impl.patch.PatchSyntaxException;
import com.intellij.openapi.diff.impl.patch.TextFilePatch;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.FilePathImpl;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.changes.patch.FilePatchInProgress;
import com.intellij.openapi.vcs.changes.patch.MatchPatchPaths;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.SLRUCache;
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
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jetbrains.crucible.connection.CrucibleJsonUtils.getChildText;

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

  private SLRUCache<String, String> myDownloadedFilesCache = new SLRUCache<String, String>(50, 50) {
    @NotNull
    @Override
    public String createValue(String relativeUrl) {
      return doDownloadFile(relativeUrl);
    }

    private String doDownloadFile(String relativeUrl) {
      String url = getHostUrl() + relativeUrl;
      final GetMethod method = new GetMethod(url);
      try {
        executeHttpMethod(method);
        return method.getResponseBodyAsString();
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  };

  public CrucibleSessionImpl(@NotNull final Project project) {
    myProject = project;
    initSSLCertPolicy();
  }

  private void initSSLCertPolicy() {
    EasySSLProtocolSocketFactory secureProtocolSocketFactory = new EasySSLProtocolSocketFactory();
    Protocol.registerProtocol("https", new Protocol("https", (ProtocolSocketFactory)secureProtocolSocketFactory, 443));
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
    return parser.parse(new InputStreamReader(method.getResponseBodyAsStream(), Charset.forName("UTF-8"))).getAsJsonObject();
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
    return parser.parse(new InputStreamReader(method.getResponseBodyAsStream(), Charset.forName("UTF-8"))).getAsJsonObject();
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
    final JsonElement code = jsonObject.get("code");
    if (error != null) {
      return error.getAsString();
    }
    else if (statusCode != null && "500".equals(statusCode.getAsString())) {
      final JsonPrimitive message = jsonObject.getAsJsonPrimitive("message");
      return message.getAsString();
    }
    else if (code != null && code.getAsString().equalsIgnoreCase("IllegalState")) {
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
    String location = getChildText(jsonObject, "location");
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

  public String downloadFile(@NotNull String relativeUrl) throws IOException {
    return myDownloadedFilesCache.get(relativeUrl);
  }

  public Review getDetailsForReview(@NotNull final String permId) throws IOException {
    String url = getHostUrl() + REVIEW_SERVICE + "/" + permId + DETAIL_REVIEW_INFO;

    final JsonObject jsonObject = buildJsonResponse(url);

    final Review review = (Review)CrucibleJsonUtils.parseBasicReview(jsonObject);
    final JsonObject reviewItems = jsonObject.getAsJsonObject("reviewItems");
    if (reviewItems != null) {
      addReviewItems(reviewItems.getAsJsonArray("reviewItem"), review);
    }

    final JsonObject reviewers = jsonObject.getAsJsonObject("reviewers");
    if (reviewers != null) {
      final JsonArray reviewerArray = reviewers.getAsJsonArray("reviewer");
      for (int i = 0; i != reviewerArray.size(); ++i) {
        final JsonObject reviewerObject = reviewerArray.get(i).getAsJsonObject();
        final User reviewer = CrucibleJsonUtils.parseUserNode(reviewerObject);
        review.addReviewer(reviewer);
      }
    }

    CrucibleJsonUtils.addGeneralComments(jsonObject, review);
    CrucibleJsonUtils.addVersionedComments(jsonObject, review);

    return review;
  }

  void addReviewItems(@NotNull final JsonArray reviewItems, @NotNull final Review review) throws IOException {
    for (int i = 0; i != reviewItems.size(); i++) {
      final JsonObject item = reviewItems.get(i).getAsJsonObject();
      final ReviewItem reviewItem = parseReviewItem(item);
      if (reviewItem != null) {
        review.addReviewItem(reviewItem);
      }
      else {
        LOG.warn("Review item was null for " + item);
      }
    }
  }

  @Nullable
  private ReviewItem parseReviewItem(JsonObject item) throws IOException {
    boolean isPatch = item.has("patchUrl");
    return isPatch ? parsePatchReviewItem(item) : parseVcsReviewItem(item);
  }

  @Nullable
  private ReviewItem parsePatchReviewItem(JsonObject item) throws IOException {
    final String id = getChildText(item.getAsJsonObject("permId"), "id");
    final String toPath = getChildText(item, "toPath");
    String patchUrl = item.get("patchUrl").getAsString();
    String file = downloadFile(patchUrl);

    List<TextFilePatch> patchTexts;
    try {
      patchTexts = new PatchReader(file).readAllPatches();
    }
    catch (PatchSyntaxException e) {
      throw new IOException(e);
    }

    List<FilePatchInProgress> patches = new MatchPatchPaths(myProject).execute(patchTexts);

    if (patches.isEmpty()) {
      LOG.error("No patches generated for the following patch texts: " + patchTexts);
      return null;
    }

    FilePatchInProgress patchForItem = findBestMatchingPatchByPath(toPath, patches);

    File base = patchForItem.getIoCurrentBase();
    if (base == null) {
      LOG.error("No base for the patch " + patchForItem.getPatch());
      return null;
    }

    final VirtualFile repo = ProjectLevelVcsManager.getInstance(myProject).getVcsRootFor(new FilePathImpl(base, base.isDirectory()));
    if (repo == null) {
      LOG.error("Couldn't find repository for base " + base);
      return null;
    }

    Map.Entry<String, VirtualFile> repoEntry = ContainerUtil.find(myRepoHash.entrySet(), new Condition<Map.Entry<String, VirtualFile>>() {
      @Override
      public boolean value(Map.Entry<String, VirtualFile> entry) {
        return entry.getValue().equals(repo);
      }
    });

    if (repoEntry == null) {
      LOG.error("Couldn't find repository name for root " + repo);
      return null;
    }
    String key = repoEntry.getKey();
    return new PatchReviewItem(id, patchForItem.getNewContentRevision().getFile().getPath(),
                               key, patches, patchUrl.substring(patchUrl.lastIndexOf("/") + 1), "",
                               item.get("authorName").getAsString(), new Date(item.get("commitDate").getAsLong()));
  }

  // temporary workaround until ReviewItem is rethinked
  @NotNull
  private FilePatchInProgress findBestMatchingPatchByPath(@NotNull String toPath, @NotNull List<FilePatchInProgress> patches) {
    int bestSimilarity = -1;
    FilePatchInProgress bestCandidate = null;
    for (FilePatchInProgress patch : patches) {
      String path = patch.getNewContentRevision().getFile().getPath();
      int similarity = findSimilarity(path, toPath);
      if (similarity > bestSimilarity) {
        bestSimilarity = similarity;
        bestCandidate = patch;
      }
    }
    assert bestCandidate != null : "best candidate should have been initialized. toPath: " + toPath + ", patches: " + patches;
    return bestCandidate;
  }

  private int findSimilarity(@NotNull String candidate, @NotNull String toPath) {
    String[] candidateSplit = candidate.split("/");
    String[] toSplit = toPath.split("/");
    int i = candidateSplit.length - 1;
    int j = toSplit.length - 1;
    while (i > 0 && j > 0) {
      if (!candidateSplit[i].equals(toSplit[j])) {
        return candidateSplit.length - 1 - i;
      }
      i--;
      j--;
    }
    return i;
  }

  private ReviewItem parseVcsReviewItem(JsonObject item) {
    final String id = getChildText(item.getAsJsonObject("permId"), "id");
    final String toPath = getChildText(item, "toPath");
    final String repoName = getChildText(item, "repositoryName");
    final String fromRevision = getChildText(item, "fromRevision");

    final ReviewItem reviewItem = new ReviewItem(id, toPath, repoName);

    final JsonArray expandedRevisions = item.getAsJsonArray("expandedRevisions");
    for (int j = 0; j != expandedRevisions.size(); ++j) {
      final JsonObject expandedRevision = expandedRevisions.get(j).getAsJsonObject();
      final String revision = getChildText(expandedRevision, "revision");
      final String type = getChildText(item, "commitType");
      if (!fromRevision.equals(revision) || "Added".equals(type)) {
        reviewItem.addRevision(revision);
      }
    }
    return reviewItem;
  }

  public Map<String, VirtualFile> getRepoHash() {
    return myRepoHash;
  }

  @Override
  public void completeReview(@NotNull String reviewId) {
    final String url = getHostUrl() + REVIEW_SERVICE + "/" + reviewId + COMPLETE;
    try {
      final PostMethod method = new PostMethod(url);
      executeHttpMethod(method);
    }
    catch (IOException e) {
      LOG.warn(e.getMessage());
    }
  }
}