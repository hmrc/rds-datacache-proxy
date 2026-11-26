/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers.overarching

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.test.Helpers.*
import play.api.libs.json.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.overarching.EnqueueHelper
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.overarching.EnqueueLongService

import scala.concurrent.Future

class EnqueueLongControllerSpec extends SpecBase with MockitoSugar with EnqueueHelper {

  private class Setup {
    val mockService: EnqueueLongService = mock[EnqueueLongService]
    val controller: EnqueueLongController = new EnqueueLongController(fakeAuthAction, mockService, cc)
  }

  "EnqueueLongControllerSpec" - {
    "return a 200 and a successful response for enqueue long" in new Setup {
      when(mockService.getEnqueueLong(any[Long], any[String], any[String], any[String], any[String], any[String], any[String], any[Int], any[Int]))
        .thenReturn(Future.successful(enqueue))

      val result: Future[Result] =
        controller.getEnqueueLong(12345, "sender1", "queue1", "replyQueue1", "correlationId1", "filter1", "key1", 1, 1010)(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(enqueue)
      verify(mockService).getEnqueueLong(12345, "sender1", "queue1", "replyQueue1", "correlationId1", "filter1", "key1", 1, 1010)
    }

    "return 500 and when repository call fails" in new Setup {
      when(mockService.getEnqueueLong(any[Long], any[String], any[String], any[String], any[String], any[String], any[String], any[Int], any[Int]))
        .thenReturn(Future.failed(new RuntimeException("Error")))

      val result: Future[Result] =
        controller.getEnqueueLong(65789, "sender2", "queue2", "replyQueue2", "correlationId2", "filter2", "key2", 2, 2020)(fakeRequest)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getEnqueueLong(65789, "sender2", "queue2", "replyQueue2", "correlationId2", "filter2", "key2", 2, 2020)
    }

  }
}
