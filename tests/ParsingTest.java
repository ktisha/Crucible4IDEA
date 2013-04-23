import com.intellij.mock.MockProject;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.UsefulTestCase;
import com.jetbrains.crucible.connection.CrucibleSession;
import com.jetbrains.crucible.model.CrucibleVersionInfo;
import com.jetbrains.crucible.model.Review;
import connection.MockCrucibleSession;
import com.jetbrains.crucible.connection.exceptions.CrucibleApiLoginException;
import com.jetbrains.crucible.model.BasicReview;
import com.jetbrains.crucible.model.CrucibleFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * User: ktisha
 */
public class ParsingTest extends UsefulTestCase {

  private MockProject myProject;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    Extensions.registerAreaClass("IDEA_PROJECT", null);

    myProject = new MockProject(null, new Disposable() {
      @Override
      public void dispose() {
      }
    });
  }

  public void testToReview() {
    CrucibleSession session = new MockCrucibleSession(myProject, getTestName(true));
    final List<BasicReview> reviews;
    try {
      reviews = session.getReviewsForFilter(CrucibleFilter.ToReview);
      assertNotNull(reviews);
      assertEquals(4, reviews.size());

      for (BasicReview review : reviews) {
        assertNotNull(review.getAuthor());
        assertNotNull(review.getCreateDate());
        assertNotNull(review.getDescription());
        assertNotNull(review.getPermaId());
        assertNotNull(review.getState());
      }
    }
    catch (IOException e) {
      fail("Can't find test data");
    }
  }

  public void testLogin() {
    CrucibleSession session = new MockCrucibleSession(myProject, getTestName(true));
    try {
      session.login();
    }
    catch (CrucibleApiLoginException e) {
      fail(e.getMessage());
    }
  }

  public void testLoginFailed() {
    CrucibleSession session = new MockCrucibleSession(myProject, getTestName(true));
    try {
      session.login();
    }
    catch (CrucibleApiLoginException e) {
      assertEquals("authentication failed", e.getMessage());
      return;
    }
    fail("Error while parsing exception message");
  }

  public void testVersionInfo() {
    CrucibleSession session = new MockCrucibleSession(myProject, getTestName(true));
    final CrucibleVersionInfo serverVersion = session.getServerVersion();
    assertNotNull(serverVersion);
    assertEquals("2013-01-15", serverVersion.getBuildDate());
    assertEquals("2.10.0", serverVersion.getReleaseNumber());
  }

  public void testDetails() {
    CrucibleSession session = new MockCrucibleSession(myProject, getTestName(true));
    final Review details;
    try {
      details = session.getDetailsForReview("CR-IC-307");
      assertNotNull(details);
      assertEquals(30, details.getComments().size());
      assertEquals(2, details.getGeneralComments().size());
      assertEquals(55, details.getReviewItems().size());
      assertEquals("irengrig", details.getAuthor().getUserName());
      assertEquals("CR-IC-307", details.getPermaId());
    }
    catch (IOException e) {
      fail(e.getMessage());
    }
  }

  public void testRepoHash() {
    CrucibleSession session = new MockCrucibleSession(myProject, getTestName(true));
    try {
      session.fillRepoHash();
      final Map<String,VirtualFile> hash = session.getRepoHash();
      assertNotNull(hash);
      assertEquals(4, hash.size());
    }
    catch (IOException e) {
      fail(e.getMessage());
    }
  }
}
