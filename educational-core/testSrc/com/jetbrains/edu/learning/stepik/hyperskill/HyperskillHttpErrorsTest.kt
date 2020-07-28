package com.jetbrains.edu.learning.stepik.hyperskill

import com.jetbrains.edu.learning.EduTestCase
import com.jetbrains.edu.learning.Err
import com.jetbrains.edu.learning.messages.EduCoreErrorBundle
import com.jetbrains.edu.learning.stepik.api.Submission
import com.jetbrains.edu.learning.stepik.hyperskill.api.HyperskillConnector
import com.jetbrains.edu.learning.stepik.hyperskill.api.MockHyperskillConnector
import okhttp3.mockwebserver.MockResponse
import java.net.HttpURLConnection.*

class HyperskillHttpErrorsTest : EduTestCase() {
  private val mockConnector: MockHyperskillConnector get() = HyperskillConnector.getInstance() as MockHyperskillConnector

  fun `test service is under maintenance`() = doTest(HTTP_BAD_GATEWAY, EduCoreErrorBundle.message("service.maintenance"))
  fun `test service is down`() = doTest(HTTP_GATEWAY_TIMEOUT, EduCoreErrorBundle.message("service.down"))
  fun `test unexpected error occurred`() = doTest(HTTP_BAD_REQUEST, EduCoreErrorBundle.message("unexpected.error", ""))
  fun `test forbidden`() = doTest(HTTP_FORBIDDEN, EduCoreErrorBundle.message("access.denied"))

  private fun doTest(code: Int, expectedError: String) {
    mockConnector.withResponseHandler(testRootDisposable) { MockResponse().setResponseCode(code) }
    val response = mockConnector.postSubmission(Submission())
    val actualError = (response as Err).error
    assertTrue("Unexpected error message: `$actualError`", actualError.startsWith(expectedError))
  }
}
