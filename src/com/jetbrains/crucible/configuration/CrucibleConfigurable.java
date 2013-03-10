package com.jetbrains.crucible.configuration;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.crucible.connection.TestConnectionTask;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: ktisha
 */
public class CrucibleConfigurable implements SearchableConfigurable {
  private final Project myProject;
  private JPanel myMainPanel;
  private JTextField myServerField;
  private JTextField myUsernameField;
  private JPasswordField myPasswordField;
  private JButton myTestButton;
  private JCheckBox myCheckRememberPass;
  private CrucibleSettings myCrucibleSettings;

  public CrucibleConfigurable(Project project) {
    myProject = project;
    myCrucibleSettings = CrucibleSettings.getInstance(myProject);

    myTestButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        saveSettings();
        Task.Modal testConnectionTask = new TestConnectionTask(myProject, "Testing Connection", true);
        testConnectionTask.setCancelText("Stop");
        ProgressManager.getInstance().run(testConnectionTask);
      }
    }
    );
  }

  private void saveSettings() {
    myCrucibleSettings.USERNAME = myUsernameField.getText();
    myCrucibleSettings.SERVER_URL = myServerField.getText();
    myCrucibleSettings.PASSWORD = new String(myPasswordField.getPassword());
  }

  @NotNull
  @Override
  public String getId() {
    return "CrucibleConfigurable";
  }

  @Nullable
  @Override
  public Runnable enableSearch(String option) {
    return null;
  }

  @Nls
  @Override
  public String getDisplayName() {
    return "Crucible Connector";
  }

  @Nullable
  @Override
  public String getHelpTopic() {
    return null;
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    return myMainPanel;
  }

  @Override
  public boolean isModified() {
    if (!StringUtil.equals(myCrucibleSettings.PASSWORD, new String(myPasswordField.getPassword()))) {
      return true;
    }
    if (!StringUtil.equals(myCrucibleSettings.SERVER_URL, new String(myServerField.getText()))) {
      return true;
    }
    if (!StringUtil.equals(myCrucibleSettings.USERNAME, new String(myUsernameField.getText()))) {
      return true;
    }
    return false;
  }

  @Override
  public void apply() throws ConfigurationException {
    saveSettings();
  }

  @Override
  public void reset() {
    myUsernameField.setText(myCrucibleSettings.USERNAME);
    myPasswordField.setText(myCrucibleSettings.PASSWORD);
    myServerField.setText(myCrucibleSettings.SERVER_URL);
  }

  @Override
  public void disposeUIResources() {
  }
}
