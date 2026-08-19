/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.rdsdatacacheproxy.cis.controllers

import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.cis.models.EnqueueMessageHeaderRequest
import uk.gov.hmrc.rdsdatacacheproxy.cis.services.UdasQueueService

import scala.concurrent.Future

class UdasQueueControllerSpec extends SpecBase with MockitoSugar {
  "UdasQueueController#enqueueMessageHeader" - {

    val requestModel: EnqueueMessageHeaderRequest = EnqueueMessageHeaderRequest(
      sender        = "Portal",
      queueName     = "AGTAUTH",
      replyQueue    = "",
      correlationId = "",
      filter        = "RemoveClient"
    )

    val mockMessageId = 10L

    "returns 200 when service succeeds" in new Setup {

      when(mockService.enqueueMessageHeader(eqTo(requestModel)))
        .thenReturn(Future.successful(mockMessageId))

      val req: FakeRequest[JsValue] = makeJsonRequest(Json.toJson(requestModel))
      val res: Future[Result] = controller.enqueueMessageHeader()(req)

      status(res) mustBe OK
      contentType(res) mustBe Some(JSON)
      contentAsJson(res) mustBe Json.obj("messageId" -> mockMessageId)

      verify(mockService).enqueueMessageHeader(eqTo(requestModel))
      verifyNoMoreInteractions(mockService)
    }

    "returns 400 BadRequest with error payload when JSON is invalid" in new Setup {
      val badJson: JsObject = Json.obj("instanceId" -> "abc-123")

      val req: FakeRequest[JsValue] = makeJsonRequest(Json.toJson(badJson))
      val res: Future[Result] = controller.enqueueMessageHeader()(req)

      status(res) mustBe BAD_REQUEST
      contentType(res) mustBe Some(JSON)

      val body: JsValue = contentAsJson(res)
      (body \ "message").as[String] mustBe "Invalid payload"
      (body \ "errors").isDefined mustBe true

      verifyNoInteractions(mockService)
    }

    "returns 500 InternalServerError with error body when service fails" in new Setup {

      when(mockService.enqueueMessageHeader(eqTo(requestModel)))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val req: FakeRequest[JsValue] = makeJsonRequest(Json.toJson(requestModel))
      val res: Future[Result] = controller.enqueueMessageHeader()(req)

      status(res) mustBe INTERNAL_SERVER_ERROR
      contentType(res) mustBe Some(JSON)
      contentAsJson(res) mustBe Json.obj("message" -> "Unexpected error")

      verify(mockService).enqueueMessageHeader(eqTo(requestModel))
      verifyNoMoreInteractions(mockService)
    }
  }

  private trait Setup {
    val mockService: UdasQueueService = mock[UdasQueueService]
    val controller = new UdasQueueController(fakeAuthAction, mockService, cc)

    def makeJsonRequest(body: JsValue): FakeRequest[JsValue] =
      FakeRequest(POST, "/enqueue-message-header")
        .withHeaders(CONTENT_TYPE -> JSON, ACCEPT -> JSON)
        .withBody(body)
  }
}
