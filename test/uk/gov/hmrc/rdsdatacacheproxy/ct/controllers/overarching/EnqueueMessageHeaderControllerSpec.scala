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

import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.test.Helpers.*
import play.api.libs.json.*
import play.api.test.FakeRequest
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.overarching.EnqueueHelper
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.overarching.EnqueueMessageHeaderRequest
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.overarching.EnqueueMessageHeaderService

import scala.concurrent.Future

class EnqueueMessageHeaderControllerSpec extends SpecBase with MockitoSugar with EnqueueHelper {

  private class Setup {
    val mockService: EnqueueMessageHeaderService = mock[EnqueueMessageHeaderService]
    val controller: EnqueueMessageHeaderController = new EnqueueMessageHeaderController(fakeAuthAction, mockService, cc)

    def makeJsonRequest(body: JsValue): FakeRequest[JsValue] =
      FakeRequest(POST, "/enqueue-message-header")
        .withHeaders(CONTENT_TYPE -> JSON, ACCEPT -> JSON)
        .withBody(body)
  }

  val validRequest = EnqueueMessageHeaderRequest(
      sender = "Portal",
      queue = "AGTAUTH",
      replyQueue = "",
      correlationId = "",
      filter = "RemoveClient"
  )

  "EnqueueMessageHeaderController" - {
    "return a 200 and a successful response" in new Setup {
      when(mockService.enqueueMessageHeader(validRequest))
        .thenReturn(Future.successful(enqueue))

      val req: FakeRequest[JsValue] = makeJsonRequest(Json.toJson(validRequest))

      val result: Future[Result] =
        controller.enqueueMessageHeader()(req)

      status(result)        shouldBe OK
      contentType(result) mustBe Some(JSON)
      contentAsJson(result) shouldBe Json.toJson(enqueue)
      verify(mockService).enqueueMessageHeader(eqTo(validRequest))
    }

    "return 500 when repository call fails" in new Setup {
      when(mockService.enqueueMessageHeader(validRequest))
        .thenReturn(Future.failed(new RuntimeException("Error")))

      val req: FakeRequest[JsValue] = makeJsonRequest(Json.toJson(validRequest))

      val result: Future[Result] =
        controller.enqueueMessageHeader()(req)


      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some(JSON)
      verify(mockService).enqueueMessageHeader(eqTo(validRequest))
    }

  }
}