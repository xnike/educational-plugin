package com.jetbrains.edu.coursecreator.ui;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.edu.coursecreator.actions.CCCreateCourseArchive;
import com.jetbrains.edu.learning.EduNames;
import com.jetbrains.edu.learning.EduSettings;
import com.jetbrains.edu.learning.stepik.StepikUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class CCCreateCourseArchivePanel extends JPanel {
  private JPanel myPanel;
  private JTextField myNameField;
  private TextFieldWithBrowseButton myLocationField;
  private JLabel myErrorIcon;
  private JLabel myErrorLabel;
  private JTextField myAuthorField;
  private CCCreateCourseArchiveDialog myDlg;

  public CCCreateCourseArchivePanel(@NotNull final Project project, CCCreateCourseArchiveDialog dlg, String name) {
    setLayout(new BorderLayout());
    add(myPanel, BorderLayout.CENTER);
    myErrorIcon.setIcon(AllIcons.Actions.Lightning);
    setState(false);
    myDlg = dlg;
    String sanitizedName = FileUtil.sanitizeFileName(name);
    myNameField.setText(sanitizedName.startsWith("_") ? EduNames.COURSE : sanitizedName);
    myAuthorField.setText(getAuthorInitialValue(project));
    myLocationField.setText(getArchiveLocation(project));
    FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
    myLocationField.addBrowseFolderListener("Choose Location Folder", null, project, descriptor);
    myLocationField.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String location = myLocationField.getText();
        File file = new File(location);
        if (!file.exists() || !file.isDirectory()) {
          myDlg.enableOKAction(false);
          setError("Invalid location");
        }
        myDlg.enableOKAction(true);
      }
    });
  }

  @NotNull
  private static String getAuthorInitialValue(@NotNull Project project) {
    String savedAuthorName = PropertiesComponent.getInstance(project).getValue(CCCreateCourseArchive.AUTHOR_NAME);
    if (savedAuthorName != null) {
      return savedAuthorName;
    }
    StepikUser stepikUser = EduSettings.getInstance().getUser();
    if (stepikUser != null) {
      return stepikUser.getName();
    }

    String userName = System.getProperty("user.name");
    if (userName != null) {
      return StringUtil.capitalize(userName);
    }
    return "User";
  }

  private void setState(boolean isVisible) {
    myErrorIcon.setVisible(isVisible);
    myErrorLabel.setVisible(isVisible);
  }

  private void setError(String message) {
    myErrorLabel.setText(message);
    setState(true);
  }

  public String getZipName() {
    return myNameField.getText();
  }

  public String getLocationPath() {
    return myLocationField.getText();
  }

  public String getAuthorName() {
    return myAuthorField.getText();
  }

  @Nullable
  private static String getArchiveLocation(@NotNull Project project) {
    String location = PropertiesComponent.getInstance(project).getValue(CCCreateCourseArchive.LAST_ARCHIVE_LOCATION);
    return location == null ? project.getBasePath() : location;
  }
}
