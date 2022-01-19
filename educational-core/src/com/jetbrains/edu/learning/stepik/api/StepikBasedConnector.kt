package com.jetbrains.edu.learning.stepik.api

import com.jetbrains.edu.learning.Ok
import com.jetbrains.edu.learning.Result
import com.jetbrains.edu.learning.courseFormat.Course
import com.jetbrains.edu.learning.courseFormat.tasks.Task
import com.jetbrains.edu.learning.stepik.hyperskill.api.HyperskillConnector
import com.jetbrains.edu.learning.stepik.hyperskill.courseFormat.HyperskillCourse
import com.jetbrains.edu.learning.submissions.Submission

interface StepikBasedConnector {
  val platformName: String

  fun getActiveAttempt(task: Task): Result<Attempt?, String>

  fun getActiveAttemptOrPostNew(task: Task, postNew: Boolean = false): Result<Attempt, String> {
    if (!postNew) {
      val activeAttempt = getActiveAttempt(task)
      if (activeAttempt is Ok && activeAttempt.value != null) {
        return Ok(activeAttempt.value)
      }
    }
    return postAttempt(task)
  }

  fun postAttempt(task: Task): Result<Attempt, String>

  fun getSubmission(id: Int): Result<Submission, String>

  fun postSubmission(submission: Submission): Result<Submission, String>

  companion object {
    fun Course.getStepikBasedConnector(): StepikBasedConnector {
      return when {
        isStepikRemote -> StepikConnector.getInstance()
        this is HyperskillCourse -> HyperskillConnector.getInstance()
        else -> error("Wrong course type: ${course.itemType}")
      }
    }

    fun Task.getStepikBasedConnector(): StepikBasedConnector = course.getStepikBasedConnector()
  }
}