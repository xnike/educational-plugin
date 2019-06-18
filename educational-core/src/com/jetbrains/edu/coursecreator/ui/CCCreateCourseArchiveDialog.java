package com.jetbrains.edu.coursecreator.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.jetbrains.edu.coursecreator.actions.CCCreateCourseArchive;
import com.jetbrains.edu.learning.StudyTaskManager;
import com.jetbrains.edu.learning.courseFormat.Course;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CCCreateCourseArchiveDialog extends DialogWrapper {

  private CCCreateCourseArchivePanel myPanel;
  private CCCreateCourseArchive myAction;

  public CCCreateCourseArchiveDialog(@NotNull final  Project project, CCCreateCourseArchive action) {
    super(project);
    Course course = StudyTaskManager.getInstance(project).getCourse();
    assert course != null;
    setTitle("Create Course Archive");
    myPanel = new CCCreateCourseArchivePanel(project, this, course.getName());
    myAction = action;
    init();
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return myPanel;
  }

  public void enableOKAction(boolean isEnabled) {
    myOKAction.setEnabled(isEnabled);
  }

  @Override
  protected void doOKAction() {
    myAction.setZipName(myPanel.getZipName());
    myAction.setLocationDir(myPanel.getLocationPath());
    myAction.setAuthorName(myPanel.getAuthorName());
    super.doOKAction();
  }
}
