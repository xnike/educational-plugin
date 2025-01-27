package com.jetbrains.edu.coursecreator.handlers.move;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.move.MoveCallback;
import com.jetbrains.edu.coursecreator.CCUtils;
import com.jetbrains.edu.learning.StudyTaskManager;
import com.jetbrains.edu.learning.VirtualFileExt;
import com.jetbrains.edu.learning.courseFormat.Course;
import com.jetbrains.edu.learning.courseFormat.Lesson;
import com.jetbrains.edu.learning.courseFormat.tasks.Task;
import com.jetbrains.edu.learning.messages.EduCoreBundle;
import com.jetbrains.edu.learning.yaml.YamlFormatSynchronizer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

import static com.jetbrains.edu.coursecreator.StudyItemType.TASK_TYPE;

public class CCTaskMoveHandlerDelegate extends CCStudyItemMoveHandlerDelegate {

  private static final Logger LOG = Logger.getInstance(CCTaskMoveHandlerDelegate.class);

  public CCTaskMoveHandlerDelegate() {
    super(TASK_TYPE);
  }

  @Override
  protected boolean isAvailable(@NotNull PsiDirectory directory) {
    return VirtualFileExt.isTaskDirectory(directory.getVirtualFile(), directory.getProject());
  }

  @Override
  public void doMove(final Project project,
                     PsiElement[] elements,
                     @Nullable PsiElement targetContainer,
                     @Nullable MoveCallback callback) {
    if (!(targetContainer instanceof PsiDirectory)) {
      return;
    }

    final VirtualFile targetVFile = ((PsiDirectory)targetContainer).getVirtualFile();

    if (!VirtualFileExt.isTaskDirectory(targetVFile, project) && !VirtualFileExt.isLessonDirectory(targetVFile, project)) {
      Messages.showInfoMessage(EduCoreBundle.message("dialog.message.incorrect.movement.task"),
                               EduCoreBundle.message("dialog.title.incorrect.target.for.move"));
      return;
    }
    final Course course = StudyTaskManager.getInstance(project).getCourse();
    final PsiDirectory sourceDirectory = (PsiDirectory)elements[0];

    if (course == null) {
      return;
    }
    final Task taskToMove = VirtualFileExt.getTask(sourceDirectory.getVirtualFile(), project);
    if (taskToMove == null) {
      return;
    }

    Lesson sourceLesson = taskToMove.getLesson();
    if (VirtualFileExt.isLessonDirectory(targetVFile, project)) {
      //if user moves task to any lesson, this task is inserted as the last task in this lesson
      Lesson targetLesson = VirtualFileExt.getLesson(targetVFile, project);
      if (targetLesson == null) {
        return;
      }
      if (targetVFile.findChild(taskToMove.getName()) != null) {
        Messages.showInfoMessage(EduCoreBundle.message("dialog.message.task.name.conflict.in.lesson"),
                                 EduCoreBundle.message("dialog.title.incorrect.target.for.move"));
        return;
      }
      List<Task> taskList = targetLesson.getTaskList();
      moveTask(sourceDirectory, taskToMove, taskList.isEmpty() ? null : taskList.get(taskList.size() - 1),
               1, targetVFile, targetLesson);
      YamlFormatSynchronizer.saveItem(sourceLesson);
      YamlFormatSynchronizer.saveItem(targetLesson);
    }
    else {
      VirtualFile lessonDir = targetVFile.getParent();
      if (lessonDir == null) {
        return;
      }
      Task targetTask = VirtualFileExt.getTask(targetVFile, project);
      if (targetTask == null) {
        return;
      }
      final Integer delta = getDelta(project, targetTask);
      if (delta == null) {
        return;
      }
      moveTask(sourceDirectory, taskToMove, targetTask, delta, lessonDir, targetTask.getLesson());
      YamlFormatSynchronizer.saveItem(sourceLesson);
      YamlFormatSynchronizer.saveItem(targetTask.getLesson());
    }
    ProjectView.getInstance(project).refresh();
  }

  private void moveTask(@NotNull final PsiDirectory sourceDirectory,
                        @NotNull final Task taskToMove,
                        @Nullable Task targetTask,
                        int indexDelta,
                        @NotNull final VirtualFile targetDirectory,
                        @NotNull Lesson targetLesson) {
    final VirtualFile sourceLessonDir = sourceDirectory.getVirtualFile().getParent();
    if (sourceLessonDir == null) {
      return;
    }
    CCUtils.updateHigherElements(sourceLessonDir.getChildren(), file -> taskToMove.getLesson().getTask(file.getName()),
                                 taskToMove.getIndex(),-1);

    final int newItemIndex = targetTask != null ? targetTask.getIndex() + indexDelta : 1;
    taskToMove.setIndex(-1);
    taskToMove.getLesson().removeTask(taskToMove);
    final Lesson finalTargetLesson = targetLesson;
    CCUtils.updateHigherElements(targetDirectory.getChildren(), file -> finalTargetLesson.getTask(file.getName()), newItemIndex - 1, 1);

    taskToMove.setIndex(newItemIndex);
    taskToMove.setParent(targetLesson);
    targetLesson.addTask(taskToMove);
    targetLesson.sortItems();
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        try {
          //moving file to the same directory leads to exception
          if (!targetDirectory.equals(sourceLessonDir)) {
            sourceDirectory.getVirtualFile().move(this, targetDirectory);
          }
        }
        catch (IOException e) {
          LOG.error(e);
        }
      }
    });
  }
}
