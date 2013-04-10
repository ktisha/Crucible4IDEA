package com.jetbrains.crucible.configuration;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.crucible.connection.CrucibleTestConnectionTask;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: ktisha
 */
public class CrucibleConfigurable implements SearchableConfigurable {
  private JPanel myMainPanel;
  private JTextField myServerField;
  private JTextField myUsernameField;
  private JPasswordField myPasswordField;
  private JButton myTestButton;
  private final CrucibleSettings myCrucibleSettings;
  private static final String DEFAULT_PASSWORD_TEXT = "************";
  private boolean myPasswordModified;


  public CrucibleConfigurable() {
    myCrucibleSettings = CrucibleSettings.getInstance();

    myTestButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        saveSettings();
        final Task.Modal testConnectionTask = new CrucibleTestConnectionTask(ProjectManager.getInstance().getDefaultProject());
        ProgressManager.getInstance().run(testConnectionTask);
      }
    }
    );

    myPasswordField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        myPasswordModified = true;
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        myPasswordModified = true;
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        myPasswordModified = true;
      }
    });
  }


  private void saveSettings() {
    myCrucibleSettings.USERNAME = myUsernameField.getText();
    myCrucibleSettings.SERVER_URL = myServerField.getText();
    if (isPasswordModified())
      myCrucibleSettings.savePassword(String.valueOf(myPasswordField.getPassword()));
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
    if (isPasswordModified()) {
      final String password = myCrucibleSettings.getPassword();
      if (!StringUtil.equals(password, new String(myPasswordField.getPassword()))) {
        return true;
      }
    }
    return !StringUtil.equals(myCrucibleSettings.SERVER_URL, myServerField.getText()) ||
           !StringUtil.equals(myCrucibleSettings.USERNAME, myUsernameField.getText());
  }

  @Override
  public void apply() throws ConfigurationException {
    saveSettings();
  }

  @Override
  public void reset() {
    myUsernameField.setText(myCrucibleSettings.USERNAME);
    myPasswordField.setText(StringUtil.isEmptyOrSpaces(myUsernameField.getText()) ? "" : DEFAULT_PASSWORD_TEXT);
    resetPasswordModification();
    myServerField.setText(myCrucibleSettings.SERVER_URL);
  }


  public boolean isPasswordModified() {
    return myPasswordModified;
  }

  public void resetPasswordModification() {
    myPasswordModified = false;
  }

  @Override
  public void disposeUIResources() {
  }
}
