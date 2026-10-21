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
import org.mockito.Mockito.{times, verify, verifyNoInteractions, when}
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.pas.models.{CreateCisAppealRequest, CreateCisAppealResponse, GetCisAppealablePenaltiesRequest, GetCisAppealablePenaltiesResponse, GetCisAppealablePenaltyResponse}
import uk.gov.hmrc.rdsdatacacheproxy.pas.services.PasCisService

import java.time.LocalDate
import scala.concurrent.Future

class PasCisControllerSpec extends SpecBase with MockitoSugar {

  "createCisAppeal" - {

    "return 201 with CreateCisAppealResponse when service succeeds" in new Setup {

      val createCisAppealRequest = CreateCisAppealRequest(1L)
      val request: FakeRequest[JsValue] = makeJsonRequest(Json.toJson(createCisAppealRequest), "/pas/cis/appeal")
      val createCisAppealResponse: CreateCisAppealResponse = CreateCisAppealResponse(true)

      when(mockService.createCisAppeal(eqTo(createCisAppealRequest))).thenReturn(Future.successful(createCisAppealResponse))

      val result: Future[Result] = controller.createCisAppeal()(request)

      status(result)        shouldBe CREATED
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(createCisAppealResponse)

      verify(mockService, times(1)).createCisAppeal(createCisAppealRequest)
    }

    "return 500 with generic message on unexpected exception" in new Setup {

      val createCisAppealRequest = CreateCisAppealRequest(1L)
      val request: FakeRequest[JsValue] = makeJsonRequest(Json.toJson(createCisAppealRequest), "/pas/cis/appeal")

      when(mockService.createCisAppeal(eqTo(createCisAppealRequest))).thenReturn(Future.failed(new RuntimeException("Database error")))

      val result: Future[Result] = controller.createCisAppeal()(request)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      (contentAsJson(result) \ "message").as[String] mustBe "Unexpected error"

      verify(mockService, times(1)).createCisAppeal(createCisAppealRequest)
    }
  }

  "getCisAppealablePenalties" - {

    val testRequest: GetCisAppealablePenaltiesRequest = GetCisAppealablePenaltiesRequest(
      hmrcOfficeNumber      = "123",
      schemeReferenceNumber = "AB456",
      startRow              = 1L,
      endRow                = 10L
    )

    "return 200 with GetCisAppealablePenaltiesResponse when service succeeds with 2 results" in new Setup {

      val expectedResponse = GetCisAppealablePenaltiesResponse(
        total = 2,
        appealablePenalties = List(
          GetCisAppealablePenaltyResponse(
            uniqueId         = Some(10001),
            issueDate        = Some(LocalDate.of(2023, 1, 1)),
            taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
            penaltyType      = Some("CIS003"),
            penaltyAmount    = Some(BigDecimal(999.23))
          ),
          GetCisAppealablePenaltyResponse(
            uniqueId         = Some(10002),
            issueDate        = Some(LocalDate.of(2026, 2, 20)),
            taxPeriodEndDate = Some(LocalDate.of(2026, 1, 5)),
            penaltyType      = Some("CIS005"),
            penaltyAmount    = Some(BigDecimal(928.23))
          )
        )
      )
      val request: FakeRequest[JsValue] = makeJsonRequest(Json.toJson(testRequest), "/pas/cis/penalties")

      when(mockService.getCisAppealablePenalties(eqTo(testRequest))).thenReturn(Future.successful(expectedResponse))

      val result: Future[Result] = controller.getCisAppealablePenalties()(request)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(expectedResponse)

      verify(mockService, times(1)).getCisAppealablePenalties(testRequest)
    }

    "return 200 with GetCisAppealablePenaltiesResponse when service succeeds with an empty list" in new Setup {

      val expectedResponse = GetCisAppealablePenaltiesResponse(
        total               = 0,
        appealablePenalties = List.empty
      )
      val request: FakeRequest[JsValue] = makeJsonRequest(Json.toJson(testRequest), "/pas/cis/penalties")

      when(mockService.getCisAppealablePenalties(eqTo(testRequest))).thenReturn(Future.successful(expectedResponse))

      val result: Future[Result] = controller.getCisAppealablePenalties()(request)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(expectedResponse)

      verify(mockService, times(1)).getCisAppealablePenalties(testRequest)
    }

    "return 400 when payload is invalid" in new Setup {
      val badRequest: JsObject = Json.obj(
        "foo" -> "bar"
      )
      val request: FakeRequest[JsValue] = makeJsonRequest(badRequest, "/pas/cis/penalties")

      val result: Future[Result] = controller.getCisAppealablePenalties()(request)

      status(result) mustBe BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] mustBe "Invalid payload"
      verifyNoInteractions(mockService)
    }

    "return 500 with generic message on unexpected exception" in new Setup {
      val request: FakeRequest[JsValue] = makeJsonRequest(Json.toJson(testRequest), "/pas/cis/penalties")

      when(mockService.getCisAppealablePenalties(eqTo(testRequest))).thenReturn(Future.failed(new RuntimeException("Database error")))

      val result: Future[Result] = controller.getCisAppealablePenalties()(request)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      (contentAsJson(result) \ "message").as[String] mustBe "Unexpected error"

      verify(mockService, times(1)).getCisAppealablePenalties(testRequest)
    }
  }

  private trait Setup {
    val mockService: PasCisService = mock[PasCisService]
    val controller: PasCisController = new PasCisController(fakeAuthAction, mockService, cc)

    def makeJsonRequest(body: JsValue, path: String): FakeRequest[JsValue] =
      FakeRequest(POST, path)
        .withHeaders(CONTENT_TYPE -> JSON, ACCEPT -> JSON)
        .withBody(body)
  }
}
