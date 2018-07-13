package com.jetbrains.crucible.configuration;

import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.ide.passwordSafe.PasswordSafeException;
import com.intellij.openapi.components.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

/**
 * User: ktisha
 */
@State(name = "CrucibleSettings",
       storages = {
         @Storage(file = "$APP_CONFIG$" + "/crucibleConnector.xml")
       }
)
public class CrucibleSettings implements PersistentStateComponent<CrucibleSettings> {
  public String SERVER_URL = "";
  public String USERNAME = "";

  @Override
  public CrucibleSettings getState() {
    return this;
  }

  @Override
  public void loadState(CrucibleSettings state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public static CrucibleSettings getInstance() {
    return ServiceManager.getService(CrucibleSettings.class);
  }

  public static final String CRUCIBLE_SETTINGS_PASSWORD_KEY = "CRUCIBLE_SETTINGS_PASSWORD_KEY";
  private static final Logger LOG = Logger.getInstance(CrucibleConfigurable.class.getName());

  public void savePassword(String pass) {
    try {
      PasswordSafe.getInstance().storePassword(null, this.getClass(), CRUCIBLE_SETTINGS_PASSWORD_KEY, pass);
    }
    catch (PasswordSafeException e) {
      LOG.info("Couldn't get password for key [" + CRUCIBLE_SETTINGS_PASSWORD_KEY + "]", e);
    }
  }

  @Nullable
  public String getPassword() {
    try {
      return PasswordSafe.getInstance().getPassword(null, this.getClass(), CRUCIBLE_SETTINGS_PASSWORD_KEY);
    }
    catch (PasswordSafeException e) {
      LOG.info("Couldn't get the password for key [" + CRUCIBLE_SETTINGS_PASSWORD_KEY + "]", e);
      return null;
    }
  }
}
