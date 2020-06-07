package com.jetbrains.crucible.configuration;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
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
    PasswordSafe.getInstance().set(new CredentialAttributes(CRUCIBLE_SETTINGS_PASSWORD_KEY), new Credentials(USERNAME, pass));
  }

  @Nullable
  public String getPassword() {
    final Credentials credentials = PasswordSafe.getInstance().get(new CredentialAttributes(CRUCIBLE_SETTINGS_PASSWORD_KEY));

    return credentials != null ? credentials.getPasswordAsString() : null;
  }
}
