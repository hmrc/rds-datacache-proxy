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
import uk.gov.hmrc.rdsdatacacheproxy.pas.models.*
import uk.gov.hmrc.rdsdatacacheproxy.pas.services.PasPayeService

import java.time.LocalDate
import scala.concurrent.Future

class PasPayeControllerSpec extends SpecBase with MockitoSugar {

  "createPayeAppeal" - {

    "return 201 with CreateAppealResponse when service succeeds" in new Setup {

      val createPayeAppealRequest = CreateAppealRequest(1L)
      val request: FakeRequest[JsValue] = makeJsonRequest(Json.toJson(createPayeAppealRequest), "/pas/paye/appeal")
      val createPayeAppealResponse: CreateAppealResponse = CreateAppealResponse(true)

      when(mockService.createPayeAppeal(eqTo(createPayeAppealRequest))).thenReturn(Future.successful(createPayeAppealResponse))

      val result: Future[Result] = controller.createPayeAppeal()(request)

      status(result)        shouldBe CREATED
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(createPayeAppealResponse)

      verify(mockService, times(1)).createPayeAppeal(createPayeAppealRequest)
    }

    "return 500 with generic message on unexpected exception" in new Setup {

      val createPayeAppealRequest = CreateAppealRequest(1L)
      val request: FakeRequest[JsValue] = makeJsonRequest(Json.toJson(createPayeAppealRequest), "/pas/paye/appeal")

      when(mockService.createPayeAppeal(eqTo(createPayeAppealRequest))).thenReturn(Future.failed(new RuntimeException("Database error")))

      val result: Future[Result] = controller.createPayeAppeal()(request)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      (contentAsJson(result) \ "message").as[String] mustBe "Unexpected error"

      verify(mockService, times(1)).createPayeAppeal(createPayeAppealRequest)
    }
  }

  "getPayeAppealablePenalties" - {

    val testRequest: GetAppealablePenaltiesRequest = GetAppealablePenaltiesRequest(
      hmrcOfficeNumber      = "123",
      schemeReferenceNumber = "AB456",
      startRow              = 1L,
      endRow                = 10L
    )

    "return 200 with GetAppealablePenaltiesResponse when service succeeds with 2 results" in new Setup {

      val expectedResponse = GetAppealablePenaltiesResponse(
        total = 2,
        appealablePenalties = List(
          GetAppealablePenaltyResponse(
            uniqueId         = Some(10001),
            issueDate        = Some(LocalDate.of(2023, 1, 1)),
            taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
            penaltyType      = Some("F"),
            penaltyAmount    = Some(BigDecimal(999.23))
          ),
          GetAppealablePenaltyResponse(
            uniqueId         = Some(10002),
            issueDate        = Some(LocalDate.of(2026, 2, 20)),
            taxPeriodEndDate = Some(LocalDate.of(2026, 1, 5)),
            penaltyType      = Some("P"),
            penaltyAmount    = Some(BigDecimal(928.23))
          )
        )
      )
      val request: FakeRequest[JsValue] = makeJsonRequest(Json.toJson(testRequest), "/pas/paye/penalties")

      when(mockService.getPayeAppealablePenalties(eqTo(testRequest))).thenReturn(Future.successful(expectedResponse))

      val result: Future[Result] = controller.getPayeAppealablePenalties()(request)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(expectedResponse)

      verify(mockService, times(1)).getPayeAppealablePenalties(testRequest)
    }

    "return 200 with GetAppealablePenaltiesResponse when service succeeds with an empty list" in new Setup {

      val expectedResponse = GetAppealablePenaltiesResponse(
        total               = 0,
        appealablePenalties = List.empty
      )
      val request: FakeRequest[JsValue] = makeJsonRequest(Json.toJson(testRequest), "/pas/paye/penalties")

      when(mockService.getPayeAppealablePenalties(eqTo(testRequest))).thenReturn(Future.successful(expectedResponse))

      val result: Future[Result] = controller.getPayeAppealablePenalties()(request)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(expectedResponse)

      verify(mockService, times(1)).getPayeAppealablePenalties(testRequest)
    }

    "return 400 when payload is invalid" in new Setup {
      val badRequest: JsObject = Json.obj(
        "foo" -> "bar"
      )
      val request: FakeRequest[JsValue] = makeJsonRequest(badRequest, "/pas/paye/penalties")

      val result: Future[Result] = controller.getPayeAppealablePenalties()(request)

      status(result) mustBe BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] mustBe "Invalid payload"
      verifyNoInteractions(mockService)
    }

    "return 500 with generic message on unexpected exception" in new Setup {
      val request: FakeRequest[JsValue] = makeJsonRequest(Json.toJson(testRequest), "/pas/paye/penalties")

      when(mockService.getPayeAppealablePenalties(eqTo(testRequest))).thenReturn(Future.failed(new RuntimeException("Database error")))

      val result: Future[Result] = controller.getPayeAppealablePenalties()(request)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      (contentAsJson(result) \ "message").as[String] mustBe "Unexpected error"

      verify(mockService, times(1)).getPayeAppealablePenalties(testRequest)
    }
  }

  "getPayeAppealablePenalty" - {

    val testRequest: GetAppealablePenaltyRequest = GetAppealablePenaltyRequest(
      penaltyId             = 1L,
      hmrcOfficeNumber      = "840123",
      schemeReferenceNumber = "098345"
    )

    "return 200 with GetAppealablePenaltyResponse when service succeeds" in new Setup {

      val expectedResponse = GetAppealablePenaltyResponse(
        uniqueId         = None,
        issueDate        = Some(LocalDate.of(2023, 1, 1)),
        taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
        penaltyType      = Some("CIS003"),
        penaltyAmount    = Some(BigDecimal(999.23))
      )

      val request: FakeRequest[JsValue] = makeJsonRequest(Json.toJson(testRequest), "/pas/cis/penalty")

      when(mockService.getPayeAppealablePenalty(eqTo(testRequest))).thenReturn(Future.successful(expectedResponse))

      val result: Future[Result] = controller.getPayeAppealablePenalty()(request)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(expectedResponse)

      verify(mockService, times(1)).getPayeAppealablePenalty(testRequest)
    }

    "return 400 when payload is invalid" in new Setup {
      val badRequest: JsObject = Json.obj(
        "foo" -> "bar"
      )
      val request: FakeRequest[JsValue] = makeJsonRequest(badRequest, "/pas/cis/penalty")

      val result: Future[Result] = controller.getPayeAppealablePenalty()(request)

      status(result) mustBe BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] mustBe "Invalid payload"
      verifyNoInteractions(mockService)
    }

    "return 500 with generic message on unexpected exception" in new Setup {
      val request: FakeRequest[JsValue] = makeJsonRequest(Json.toJson(testRequest), "/pas/cis/penalty")

      when(mockService.getPayeAppealablePenalty(eqTo(testRequest))).thenReturn(Future.failed(new RuntimeException("Database error")))

      val result: Future[Result] = controller.getPayeAppealablePenalty()(request)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      (contentAsJson(result) \ "message").as[String] mustBe "Unexpected error"

      verify(mockService, times(1)).getPayeAppealablePenalty(testRequest)
    }
  }

  "getPayeTaxpayer" - {

    val testRequest: GetTaxpayerRequest = GetTaxpayerRequest(
      hmrcOfficeNumber      = "123",
      schemeReferenceNumber = "AB456"
    )

    "return 200 with GetPayeTaxpayerResponse when service returns a taxpayer" in new Setup {

      val expectedResponse = GetPayeTaxpayerResponse(
        uniqueId       = 1L,
        instanceId     = 2L,
        districtNumber = "123",
        employerRef    = "AB456",
        employerName1  = Some("TEST EMPLOYER"),
        employerName2  = Some("LTD"),
        agentOwnRef    = Some("AGENTREF")
      )
      val request: FakeRequest[JsValue] = makeJsonRequest(Json.toJson(testRequest), "/pas/paye/taxpayer")

      when(mockService.getPayeTaxpayer(eqTo(testRequest))).thenReturn(Future.successful(Some(expectedResponse)))

      val result: Future[Result] = controller.getPayeTaxpayer()(request)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(expectedResponse)

      verify(mockService, times(1)).getPayeTaxpayer(testRequest)
    }

    "return 404 when service returns None" in new Setup {
      val request: FakeRequest[JsValue] = makeJsonRequest(Json.toJson(testRequest), "/pas/paye/taxpayer")

      when(mockService.getPayeTaxpayer(eqTo(testRequest))).thenReturn(Future.successful(None))

      val result: Future[Result] = controller.getPayeTaxpayer()(request)

      status(result)      shouldBe NOT_FOUND
      contentType(result) shouldBe Some("application/json")
      (contentAsJson(result) \ "message").as[String] mustBe "PAYE taxpayer not found"

      verify(mockService, times(1)).getPayeTaxpayer(testRequest)
    }

    "return 400 when payload is invalid" in new Setup {
      val badRequest: JsObject = Json.obj(
        "foo" -> "bar"
      )
      val request: FakeRequest[JsValue] = makeJsonRequest(badRequest, "/pas/paye/taxpayer")

      val result: Future[Result] = controller.getPayeTaxpayer()(request)

      status(result) mustBe BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] mustBe "Invalid payload"
      verifyNoInteractions(mockService)
    }

    "return 400 when required fields are missing" in new Setup {
      val badRequest: JsObject = Json.obj(
        "hmrcOfficeNumber" -> "123"
      )
      val request: FakeRequest[JsValue] = makeJsonRequest(badRequest, "/pas/paye/taxpayer")

      val result: Future[Result] = controller.getPayeTaxpayer()(request)

      status(result) mustBe BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] mustBe "Invalid payload"
      (contentAsJson(result) \ "errors").toOption must not be empty
      verifyNoInteractions(mockService)
    }

    "return 500 with generic message on unexpected exception" in new Setup {
      val request: FakeRequest[JsValue] = makeJsonRequest(Json.toJson(testRequest), "/pas/paye/taxpayer")

      when(mockService.getPayeTaxpayer(eqTo(testRequest))).thenReturn(Future.failed(new RuntimeException("Database error")))

      val result: Future[Result] = controller.getPayeTaxpayer()(request)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      (contentAsJson(result) \ "message").as[String] mustBe "Unexpected error"

      verify(mockService, times(1)).getPayeTaxpayer(testRequest)
    }
  }

  "getPayePenaltyAllowed" - {

    val testRequest: GetPenaltyAllowedRequest = GetPenaltyAllowedRequest(
      districtNumber    = 1L,
      employerReference = "357",
      uniqueId          = 1L
    )

    "return 200 with GetPenaltyAllowedResponse when service succeeds" in new Setup {

      val expectedResponse = GetPenaltyAllowedResponse(
        allowed = true
      )

      val request: FakeRequest[JsValue] = makeJsonRequest(Json.toJson(testRequest), "/paye/penalties/access-allowed")

      when(mockService.getPayePenaltyAllowed(eqTo(testRequest))).thenReturn(Future.successful(expectedResponse))

      val result: Future[Result] = controller.getPayePenaltyAllowed()(request)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(expectedResponse)

      verify(mockService, times(1)).getPayePenaltyAllowed(testRequest)
    }

    "return 400 when payload is invalid" in new Setup {
      val badRequest: JsObject = Json.obj(
        "foo" -> "bar"
      )
      val request: FakeRequest[JsValue] = makeJsonRequest(badRequest, "/paye/penalties/access-allowed")

      val result: Future[Result] = controller.getPayePenaltyAllowed()(request)

      status(result) mustBe BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] mustBe "Invalid payload"
      verifyNoInteractions(mockService)
    }

    "return 500 with generic message on unexpected exception" in new Setup {
      val request: FakeRequest[JsValue] = makeJsonRequest(Json.toJson(testRequest), "/paye/penalties/access-allowed")

      when(mockService.getPayePenaltyAllowed(eqTo(testRequest))).thenReturn(Future.failed(new RuntimeException("Database error")))

      val result: Future[Result] = controller.getPayePenaltyAllowed()(request)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      (contentAsJson(result) \ "message").as[String] mustBe "Unexpected error"

      verify(mockService, times(1)).getPayePenaltyAllowed(testRequest)
    }
  }

  private trait Setup {
    val mockService: PasPayeService = mock[PasPayeService]
    val controller: PasPayeController = new PasPayeController(fakeAuthAction, mockService, cc)

    def makeJsonRequest(body: JsValue, path: String): FakeRequest[JsValue] =
      FakeRequest(POST, path)
        .withHeaders(CONTENT_TYPE -> JSON, ACCEPT -> JSON)
        .withBody(body)
  }
}
