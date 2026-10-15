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

package uk.gov.hmrc.rdsdatacacheproxy.pas.controllers

import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.pas.models.{CreateCisAppealRequest, CreateCisAppealResponse}
import uk.gov.hmrc.rdsdatacacheproxy.pas.services.PasCisService

import scala.concurrent.Future

class PasCisControllerSpec extends SpecBase with MockitoSugar {

  "createCisAppeal" - {

    "return 201 with CreateCisAppealResponse when service succeeds" in new Setup {

      val createCisAppealRequest = CreateCisAppealRequest(1L)
      val request: FakeRequest[JsValue] = makeJsonRequest(Json.toJson(createCisAppealRequest))

      when(mockService.createCisAppeal(eqTo(createCisAppealRequest))).thenReturn(Future.successful(createCisAppealResponse))

      val result: Future[Result] = controller.createCisAppeal()(request)

      status(result)        shouldBe CREATED
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(createCisAppealResponse)

      verify(mockService, times(1)).createCisAppeal(createCisAppealRequest)
    }

    "return 500 with generic message on unexpected exception" in new Setup {

      val createCisAppealRequest = CreateCisAppealRequest(1L)
      val request: FakeRequest[JsValue] = makeJsonRequest(Json.toJson(createCisAppealRequest))

      when(mockService.createCisAppeal(eqTo(createCisAppealRequest))).thenReturn(Future.failed(new RuntimeException("Database error")))

      val result: Future[Result] = controller.createCisAppeal()(request)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      (contentAsJson(result) \ "message").as[String] mustBe "Unexpected error"

      verify(mockService, times(1)).createCisAppeal(createCisAppealRequest)
    }
  }

  private trait Setup {
    val mockService: PasCisService = mock[PasCisService]
    val controller: PasCisController = new PasCisController(fakeAuthAction, mockService, cc)
    val createCisAppealResponse: CreateCisAppealResponse = CreateCisAppealResponse(true)

    def makeJsonRequest(body: JsValue): FakeRequest[JsValue] =
      FakeRequest(POST, "/pas/cis/appeal")
        .withHeaders(CONTENT_TYPE -> JSON, ACCEPT -> JSON)
        .withBody(body)
  }
}
