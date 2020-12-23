package com.jetbrains.edu.learning.marketplace

import com.jetbrains.edu.learning.EduTestCase
import com.jetbrains.edu.learning.courseFormat.EduCourse
import com.jetbrains.edu.learning.marketplace.api.MarketplaceConnector
import com.jetbrains.edu.learning.marketplace.api.MockMarketplaceConnector
import okhttp3.mockwebserver.RecordedRequest

class MarketplaceSearchCoursesTest : EduTestCase() {

  private val mockConnector: MockMarketplaceConnector get() = MarketplaceConnector.getInstance() as MockMarketplaceConnector

  private fun configureCoursesResponse() {
    mockConnector.withResponseHandler(testRootDisposable) { request ->
      COURSES_REQUEST_RE.matchEntire(request.path) ?: return@withResponseHandler null
      mockResponse("courses.json")
    }
  }

  fun `test courses loaded`() {
    configureCoursesResponse()
    doTestCoursesLoaded()
  }

  fun `test python en course created`() {
    configureCoursesResponse()
    val courses = doTestCoursesLoaded()

    val pythonCourse = courses.first()
    assertEquals("Introduction to Python", pythonCourse.name)
    assertEquals(1, pythonCourse.id)
    assertEquals("Python", pythonCourse.language)
    assertEquals("English", pythonCourse.humanLanguage)
    assertEquals("Introduction course to Python", pythonCourse.description)
    assertEquals("JetBrains s.r.o.", pythonCourse.vendor.name)
    assertEquals(2, pythonCourse.learnersCount)
    assertEquals(5.0, pythonCourse.reviewScore)
    assertEquals(true, pythonCourse.isMarketplace)
  }

  fun `test java ru course created`() {
    configureCoursesResponse()
    val courses = doTestCoursesLoaded()

    val javaCourse = courses[1]
    assertEquals("Introduction to Java", javaCourse.name)
    assertEquals(2, javaCourse.id)
    assertEquals("Java", javaCourse.language)
    assertEquals("Russian", javaCourse.humanLanguage)
    assertEquals("Introduction course to Java", javaCourse.description)
    assertEquals("JetBrains s.r.o.", javaCourse.vendor.name)
    assertEquals(5, javaCourse.learnersCount)
    assertEquals(5.0, javaCourse.reviewScore)
    assertEquals(true, javaCourse.isMarketplace)
  }

  fun `test all courses loaded`() {
    mockConnector.withResponseHandler(testRootDisposable) { request ->
      COURSES_REQUEST_RE.matchEntire(request.path) ?: return@withResponseHandler null
      when (request.getOffset()) {
        0 -> mockResponse("courses_10.json")
        else -> mockResponse("courses.json")
      }
    }

    doTestCoursesLoaded(12)
  }

  private fun RecordedRequest.getOffset(): Int {
    return body.readUtf8().substringAfter("offset: ").substringBefore("\\n").toInt()
  }

  private fun doTestCoursesLoaded(coursesNumber: Int = 2): List<EduCourse> {
    val courses = MarketplaceConnector.getInstance().searchCourses()
    assertEquals(coursesNumber, courses.size)
    return courses
  }

  override fun getTestDataPath(): String = super.getTestDataPath() + "/marketplace/searchCourses/"

  companion object {
    private val COURSES_REQUEST_RE = """/api/search/graphql?.*""".toRegex()
  }
}