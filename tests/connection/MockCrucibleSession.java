package connection;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.jetbrains.crucible.connection.CrucibleSessionImpl;
import com.jetbrains.crucible.model.Repository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;

/**
 * User : ktisha
 *
 */
public class MockCrucibleSession extends CrucibleSessionImpl {
  private String myName;

  public MockCrucibleSession(@NotNull Project project, String name) {
    super(project);
    myName = name;
  }

  protected JsonObject buildJsonResponse(@NotNull final String urlString) throws IOException {
    String name = getTestDataPath() + "/" + myName + ".json";
    return JsonParser.parseReader(new BufferedReader(new FileReader(name))).getAsJsonObject();
  }

  protected String getHostUrl() {
    return "";
  }

  @Override
  protected String getUsername() {
    return "";
  }

  @Override
  protected String getPassword() {
    return "";
  }

  @Nullable
  @Override
  protected VirtualFile getLocalPath(@NotNull final Repository repo) {
    return new VirtualFile() {
      @NotNull
      @Override
      public String getName() {
        return repo.getName();
      }

      @NotNull
      @Override
      public VirtualFileSystem getFileSystem() {
        return null;
      }

      @Override
      public String getPath() {
        return null;
      }

      @Override
      public boolean isWritable() {
        return false;
      }

      @Override
      public boolean isDirectory() {
        return false;
      }

      @Override
      public boolean isValid() {
        return false;
      }

      @Override
      public VirtualFile getParent() {
        return null;
      }

      @Override
      public VirtualFile[] getChildren() {
        return new VirtualFile[0];
      }

      @NotNull
      @Override
      public OutputStream getOutputStream(Object requestor, long newModificationStamp, long newTimeStamp) throws IOException {
        return null;
      }

      @NotNull
      @Override
      public byte[] contentsToByteArray() throws IOException {
        return new byte[0];
      }

      @Override
      public long getTimeStamp() {
        return 0;
      }

      @Override
      public long getLength() {
        return 0;
      }

      @Override
      public void refresh(boolean asynchronous, boolean recursive, @Nullable Runnable postRunnable) {
      }

      @Override
      public InputStream getInputStream() throws IOException {
        return null;
      }
    };
  }

  private String getTestDataPath() {
    return "testData";
  }
}
