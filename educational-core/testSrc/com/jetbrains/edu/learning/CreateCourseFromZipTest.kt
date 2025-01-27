package com.jetbrains.edu.learning

import com.intellij.testFramework.UsefulTestCase
import com.jetbrains.edu.learning.courseFormat.Course
import com.jetbrains.edu.learning.courseFormat.EduCourse
import com.jetbrains.edu.learning.courseGeneration.GeneratorUtils


class CreateCourseFromZipTest : EduTestCase() {

  fun `test create marketplace course from old format zip archive`() {
    doTestCreateMarketplaceCourseFromZip("old format marketplace course.zip")
  }

  fun `test create marketplace course from new format zip archive`() {
    doTestCreateMarketplaceCourseFromZip("new format marketplace course.zip")
  }

  fun `test create edu course from not encrypted old format zip archive`() {
    doTestCreateEduCourseFromZip("old format edu course.zip")
  }

  fun `test create pycharm typed course from encrypted new format zip archive`() {
    doTestCreateEduCourseFromZip("new format edu course.zip")
  }

  private fun doTestCreateMarketplaceCourseFromZip(fileName: String) {
    val marketplaceCourse = doTestCreateEduCourseFromZip(fileName)

    assertTrue(marketplaceCourse.isMarketplace)
    assertEquals(1, marketplaceCourse.marketplaceCourseVersion)
  }

  private fun doTestCreateEduCourseFromZip(fileName: String,
                                   courseName: String = "Introduction to Python",
                                   lessonsSize: Int = 10): Course {
    val zipPath = "$testDataPath/$fileName"
    val course = EduUtils.getLocalCourse(zipPath) ?: error("Failed to load course from $zipPath")

    UsefulTestCase.assertInstanceOf(course, EduCourse::class.java)
    GeneratorUtils.initializeCourse(project, course)

    UsefulTestCase.assertInstanceOf(course, EduCourse::class.java)
    assertEquals(courseName, course.name)
    assertTrue(course.lessons.size == lessonsSize)

    val task = findTask(0, 0)
    assertEquals("print(\"Hello, world! My name is type your name\")\n", task.taskFiles["hello_world.py"]?.text)
    assertTrue(course.additionalFiles.size == 1)
    assertEquals("additional file text", course.additionalFiles.first().text)

    return course
  }

  override fun getTestDataPath(): String {
    return super.getTestDataPath() + "/unpack"
  }
}