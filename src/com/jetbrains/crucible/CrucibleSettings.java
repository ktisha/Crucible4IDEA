package com.jetbrains.crucible;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;

/**
 * User: catherine
 */
@State(name = "CrucibleSettings",
      storages = {
      @Storage( file = StoragePathMacros.PROJECT_FILE),
      @Storage( file = StoragePathMacros.PROJECT_CONFIG_DIR + "/crucibleConnector.xml", scheme = StorageScheme.DIRECTORY_BASED)
      }
)
public class CrucibleSettings implements PersistentStateComponent<CrucibleSettings> {
  public String SERVER_URL = "";
  public String USERNAME = "";
  public String PASSWORD = "";

  @Override
  public CrucibleSettings getState() {
    return this;
  }

  @Override
  public void loadState(CrucibleSettings state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public static CrucibleSettings getInstance(Project project) {
    return ServiceManager.getService(project, CrucibleSettings.class);
  }

}
