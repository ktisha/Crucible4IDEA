package com.jetbrains.crucible.connection;

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
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
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

  private final Map<String, VirtualFile> myRepoHash = new HashMap<String, VirtualFile>();

  CrucibleSessionImpl(@NotNull final Project project) {
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

      final Document doc = buildSaxResponse(loginUrl);
      final String exception = getExceptionMessages(doc);
      if (exception != null) {
        throw new CrucibleApiLoginException(exception);
      }
      final XPath xpath = XPath.newInstance("/loginResult/token");
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
  public CrucibleVersionInfo getServerVersion() {
    final String requestUrl = getHostUrl() + REVIEW_SERVICE + VERSION;
    try {
      final Document doc = buildSaxResponse(requestUrl);
      final XPath xpath = XPath.newInstance("versionInfo");

      @SuppressWarnings("unchecked")
      final List<Element> elements = xpath.selectNodes(doc);

      if (elements != null && !elements.isEmpty()) {
        return CrucibleXmlParser.parseVersionNode(elements.get(0));
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
    final GetMethod method = new GetMethod(urlString);
    adjustHttpHeader(method);
    final HttpClient client = new HttpClient();
    client.executeMethod(method);

    return builder.build(method.getResponseBodyAsStream());
  }

  protected Document buildSaxResponseForPost(@NotNull final String urlString,
                                             @NotNull final RequestEntity requestEntity) throws IOException, JDOMException {
    final SAXBuilder builder = new SAXBuilder();
    final PostMethod method = new PostMethod(urlString);
    method.setRequestEntity(requestEntity);
    adjustHttpHeader(method);
    final HttpClient client = new HttpClient();
    client.executeMethod(method);

    return builder.build(method.getResponseBodyAsStream());
  }

  @SuppressWarnings("unchecked")
  private static RequestEntity createCommentRequest(Comment comment, boolean isGeneral) throws UnsupportedEncodingException {
    Document doc = new Document();
    Element root = new Element(isGeneral ? "generalCommentData" : "versionedLineCommentData");

    final Element defectApproved = new Element("defectApproved");
    final Element defectRaised = new Element("defectRaised");
    final Element deleted = new Element("deleted");
    final Element draft = new Element("draft");
    final Element message = new Element("message");
    message.addContent(comment.getMessage());

    final Element parentCommentId = new Element("parentCommentId");
    final String parentId = comment.getParentCommentId();
    if (parentId != null) {
      final Element id = new Element("id");
      id.addContent(parentId);
      parentCommentId.addContent(id);
    }

    final Element permId = new Element("permId");
    final Element permaId = new Element("permaId");

    root.addContent(defectApproved);
    root.addContent(defectRaised);
    root.addContent(deleted);
    root.addContent(draft);
    root.addContent(message);
    root.addContent(parentCommentId);
    root.addContent(permId);
    root.addContent(permaId);

    if (!isGeneral) {
      final Element reviewItemId = new Element("reviewItemId");
      final Element id = new Element("id");
      id.addContent(comment.getReviewItemId());

      final Element toLineRange = new Element("toLineRange");
      toLineRange.addContent(comment.getLine());

      root.addContent(reviewItemId);
      root.addContent(toLineRange);
    }

    doc.setRootElement(root);

    XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
    String requestString = serializer.outputString(doc);
    return new StringRequestEntity(requestString, "application/xml", "UTF-8");
  }

  protected void adjustHttpHeader(@NotNull final HttpMethod method) {
    method.addRequestHeader(new Header("Authorization", getAuthHeaderValue()));
  }

  private String getUsername() {
    return CrucibleSettings.getInstance().USERNAME;
  }

  private String getPassword() {
    return CrucibleSettings.getInstance().getPassword();
  }

  private String getHostUrl() {
    return UrlUtil.removeUrlTrailingSlashes(CrucibleSettings.getInstance().SERVER_URL);
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

  @Nullable
  public Comment postComment(@NotNull final Comment comment, boolean isGeneral,
                             @NotNull final String reviewId) {

    String url = getHostUrl() + REVIEW_SERVICE + "/" + reviewId;
    final String parentCommentId = comment.getParentCommentId();
    if (!isGeneral && parentCommentId == null)
      url += REVIEW_ITEMS + "/" + comment.getReviewItemId();

    url += COMMENTS;


    if (parentCommentId != null) {
      url += "/" + parentCommentId + REPLIES;
    }

    try {
      final RequestEntity request = createCommentRequest(comment, isGeneral || parentCommentId != null);
      final Document document = buildSaxResponseForPost(url, request);
      XPath xpath = XPath.newInstance("/error");
      @SuppressWarnings("unchecked")
      Element element = (Element)xpath.selectSingleNode(document);
      if (element != null) {
        final String message = CrucibleXmlParser.getChildText(element, "message");
        UiUtils.showBalloon(myProject, "Sorry, comment wasn't added:\n" + message, MessageType.ERROR);
        return null;
      }

      XPath commentPath = XPath.newInstance(isGeneral || parentCommentId != null ? "/generalCommentData" :
                                          "/versionedLineCommentData");

      @SuppressWarnings("unchecked")
      final Element commentNode = (Element)commentPath.selectSingleNode(document);
      return parseComment(commentNode, !isGeneral);
    }
    catch (IOException e) {
      LOG.warn(e.getMessage());
    }
    catch (JDOMException e) {
      LOG.warn(e.getMessage());
    }
    return null;
  }

  @Override
  public void fillRepoHash() throws IOException, JDOMException {
    String url = getHostUrl() + REPOSITORIES;
    final Document doc = buildSaxResponse(url);
    XPath xpath = XPath.newInstance("/repositories/repoData");
    @SuppressWarnings("unchecked")
    List<Element> elements = xpath.selectNodes(doc);

    if (elements != null && !elements.isEmpty()) {
      for (Element element : elements) {
        final String enabled = CrucibleXmlParser.getChildText(element, "enabled");
        if (Boolean.parseBoolean(enabled)) {
          final String name = CrucibleXmlParser.getChildText(element, "name");
          final String type = CrucibleXmlParser.getChildText(element, "type");
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
  private VirtualFile getLocalPath(@NotNull final String name) throws IOException, JDOMException {
    String url = getHostUrl() + REPOSITORIES + "/" + name;
    final Document doc = buildSaxResponse(url);
    XPath xpath = XPath.newInstance("/gitRepositoryData");
    @SuppressWarnings("unchecked")
    Element element = (Element)xpath.selectSingleNode(doc);
    if (element == null) return null;
    String location = CrucibleXmlParser.getChildText(element, "location");
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


  public List<BasicReview> getReviewsForFilter(@NotNull final CrucibleFilter filter) throws JDOMException, IOException {
    String url = getHostUrl() + REVIEW_SERVICE + FILTERED_REVIEWS;
    final String urlFilter = filter.getFilterUrl();
    if (!StringUtils.isEmpty(urlFilter)) {
      url += "/" + urlFilter;
    }
    final Document doc = buildSaxResponse(url);
    final XPath xpath = XPath.newInstance("/reviews/reviewData");

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

  public Review getDetailsForReview(@NotNull final String permId) throws JDOMException, IOException {
    String url = getHostUrl() + REVIEW_SERVICE + "/" + permId + DETAIL_REVIEW_INFO;
    final Document doc = buildSaxResponse(url);
    XPath xpath = XPath.newInstance("/detailedReviewData/reviewItems");

    @SuppressWarnings("unchecked")
    List<Element> reviewItems = xpath.selectNodes(doc);
    final Element node = (Element)XPath.newInstance("/detailedReviewData").selectSingleNode(doc);
    final User author = CrucibleXmlParser.parseUserNode(node);

    final User moderator = (node.getChild("moderator") != null)
                           ? CrucibleXmlParser.parseUserNode(node.getChild("moderator")) : null;

    final Review review = new Review(permId, author, moderator);

    if (reviewItems != null && !reviewItems.isEmpty()) {
      addReviewItems(reviewItems, review);
    }

    addGeneralComments(doc, review);
    addVersionedComments(doc, review);

    return review;
  }

  private static void addGeneralComments(@NotNull final Document doc,
                                         @NotNull final Review review) throws JDOMException {
    @SuppressWarnings("unchecked")
    final List<Element> generalCommentNodes = XPath.newInstance("/detailedReviewData/generalComments/generalCommentData").selectNodes(doc);
    if (generalCommentNodes != null && !generalCommentNodes.isEmpty()) {
      for (Element generalCommentNode : generalCommentNodes) {
        final Comment comment = parseComment(generalCommentNode, false);
        review.addGeneralComment(comment);
      }
    }
  }

  private static Comment parseComment(@NotNull final Element commentNode, boolean isVersioned) {
    final String message = CrucibleXmlParser.getChildText(commentNode, "message");
    final User commentAuthor = CrucibleXmlParser.parseUserNode(commentNode.getChild("user"));
    final Date createDate = CrucibleXmlParser.parseDate(commentNode);
    final Comment comment = new Comment(commentAuthor, message);

    final Element permId = commentNode.getChild("permaId");
    final String id = CrucibleXmlParser.getChildText(permId, "id");
    comment.setPermId(id);
    if (createDate != null) comment.setCreateDate(createDate);
    getReplies(commentNode, comment);

    if (isVersioned) {
      final String toLineRange = CrucibleXmlParser.getChildText(commentNode, "toLineRange");
      comment.setLine(toLineRange);
      final Element ranges = commentNode.getChild("lineRanges");
      if (ranges != null) {
        final String revision = CrucibleXmlParser.getChildAttribute(ranges, "lineRange", "revision");
        comment.setRevision(revision);
      }

      Element lineRanges = commentNode.getChild("lineRanges");
      if (lineRanges != null) {
        Element lineRange = lineRanges.getChild("lineRange");
        if (lineRange != null) {
          final Attribute revision = lineRange.getAttribute("revision");
          if (revision != null) {
            comment.setRevision(revision.getValue());
          }
        }
      }

      final Element reviewItemNode = commentNode.getChild("reviewItemId");
      if (reviewItemNode != null) {
        final String reviewItemId = CrucibleXmlParser.getChildText(reviewItemNode, "id");
        comment.setReviewItemId(reviewItemId);
      }
    }
    return comment;
  }

  private static void addVersionedComments(@NotNull final Document doc,
                                           @NotNull final Review review) throws JDOMException {
    @SuppressWarnings("unchecked")
    final List<Element> commentNodes = XPath.newInstance("/detailedReviewData/versionedComments/versionedLineCommentData").
      selectNodes(doc);
    if (commentNodes != null && !commentNodes.isEmpty()) {
      for (Element commentNode : commentNodes) {
        Comment comment = parseComment(commentNode, true);
        review.addComment(comment);
      }
    }
  }

  private static void getReplies(@NotNull final Element node, @NotNull final Comment comment) {
    @SuppressWarnings("unchecked")
    final List<Element> replies = node.getChildren("replies");
    for (Element replyNode : replies) {
      @SuppressWarnings("unchecked")
      final List<Element> commentData = replyNode.getChildren("generalCommentData");
      if (!commentData.isEmpty()) {
        for (Element commentNode : commentData) {
          final Comment replyComment = parseComment(commentNode, false);
          comment.addReply(replyComment);
        }
      }
    }
  }

  private static void addReviewItems(@NotNull final List<Element> reviewItems,
                                     @NotNull final Review review) {
    for (Element element : reviewItems) {
      @SuppressWarnings("unchecked")
      final List<Element> items = element.getChildren("reviewItem");
      for (Element item : items) {
        @SuppressWarnings("unchecked")
        final List<Element> expandedRevisions = item.getChildren("expandedRevisions");

        final Element permId = item.getChild("permId");
        final String id = CrucibleXmlParser.getChildText(permId, "id");
        final String toPath = CrucibleXmlParser.getChildText(item, "toPath");
        final String repoName = CrucibleXmlParser.getChildText(item, "repositoryName");
        final String fromRevision = CrucibleXmlParser.getChildText(item, "fromRevision");

        final ReviewItem reviewItem = new ReviewItem(id, toPath, repoName);

        for (Element expandedRevision : expandedRevisions) {
          final String revision = CrucibleXmlParser.getChildText(expandedRevision, "revision");
          final String type = CrucibleXmlParser.getChildText(item, "commitType");
          if (!fromRevision.equals(revision) || "Added".equals(type)) {
            reviewItem.addRevision(revision);
          }
        }
        review.addReviewItem(reviewItem);
      }
    }
  }

  private BasicReview parseBasicReview(@NotNull final Element element) {
    return CrucibleXmlParser.parseBasicReview(element);
  }

  public Map<String, VirtualFile> getRepoHash() {
    return myRepoHash;
  }
}