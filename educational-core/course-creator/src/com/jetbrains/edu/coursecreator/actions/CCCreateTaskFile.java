package com.jetbrains.edu.coursecreator.actions;

import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeView;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.util.DirectoryChooserUtil;
import com.intellij.ide.util.EditorHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.jetbrains.edu.learning.StudyTaskManager;
import com.jetbrains.edu.learning.core.EduNames;
import com.jetbrains.edu.learning.courseFormat.Course;
import com.jetbrains.edu.learning.courseFormat.Lesson;
import com.jetbrains.edu.learning.courseFormat.Task;
import com.jetbrains.edu.coursecreator.CCLanguageManager;
import com.jetbrains.edu.coursecreator.CCUtils;
import com.jetbrains.edu.coursecreator.ui.CreateTaskFileDialog;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.ui.DialogWrapper.OK_EXIT_CODE;

public class CCCreateTaskFile extends DumbAwareAction {

  public CCCreateTaskFile() {
    super("Task File", "Create new Task File", AllIcons.FileTypes.Text);
  }

  @Override
  public void actionPerformed(final AnActionEvent e) {
    final IdeView view = e.getData(LangDataKeys.IDE_VIEW);
    final Project project = e.getData(CommonDataKeys.PROJECT);

    if (view == null || project == null) {
      return;
    }
    final PsiDirectory taskDir = DirectoryChooserUtil.getOrChooseDirectory(view);
    if (taskDir == null) return;
    PsiDirectory lessonDir = taskDir.getParent();
    if (lessonDir == null) {
      return;
    }
    final Course course = StudyTaskManager.getInstance(project).getCourse();
    final Lesson lesson = course.getLesson(lessonDir.getName());
    final Task task = lesson.getTask(taskDir.getName());

    final int index = task.getTaskFiles().size() + 1;
    String generatedName = "file" + index;
    CreateTaskFileDialog dialog = new CreateTaskFileDialog(project, generatedName, course);
    dialog.show();
    if (dialog.getExitCode() != OK_EXIT_CODE) {
      return;
    }
    final String name = dialog.getFileName();
    if (name == null) return;
    FileType type = dialog.getFileType();
    if (type == null) {
      return;
    }
    final CCLanguageManager CCLanguageManager = CCUtils.getStudyLanguageManager(course);
    if (CCLanguageManager == null) {
      return;
    }
    final String extension = type.getDefaultExtension();
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        final FileTemplate taskTemplate = CCLanguageManager.getTaskFileTemplateForExtension(project, extension);
        final String taskFileName = name + "." + extension;
        try {
          if (taskTemplate == null) {
            VirtualFile file = taskDir.getVirtualFile().createChildData(this, taskFileName);
            ProjectView.getInstance(project).select(file, file, false);
            FileEditorManager.getInstance(project).openFile(file, true);
          }
          else {
            final PsiElement taskFile = FileTemplateUtil.createFromTemplate(taskTemplate, taskFileName, null, taskDir);
            ApplicationManager.getApplication().invokeLater(new Runnable() {
              @Override
              public void run() {
                EditorHelper.openInEditor(taskFile, false);
                view.selectElement(taskFile);
              }
            });
          }
          task.addTaskFile(taskFileName, index);
        }
        catch (Exception ignored) {
        }
      }
    });
  }

  @Override
  public void update(@NotNull AnActionEvent event) {
    final Presentation presentation = event.getPresentation();
    presentation.setEnabledAndVisible(false);
    final Project project = event.getData(CommonDataKeys.PROJECT);
    if (project == null) {
      return;
    }
    final IdeView view = event.getData(LangDataKeys.IDE_VIEW);
    if (view == null) {
      return;
    }

    final PsiDirectory[] directories = view.getDirectories();
    if (directories.length == 0) {
      return;
    }
    final PsiDirectory directory = DirectoryChooserUtil.getOrChooseDirectory(view);
    final Course course = StudyTaskManager.getInstance(project).getCourse();
    if (course != null && directory != null && !directory.getName().contains(EduNames.TASK)) {
      return;
    }
    presentation.setEnabledAndVisible(true);
  }
}
