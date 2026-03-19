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

package uk.gov.hmrc.rdsdatacacheproxy.nova.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.nova.models.{TraderDetailsResponse, TraderResponse}
import uk.gov.hmrc.rdsdatacacheproxy.nova.repositories.NovaDataSource

import scala.concurrent.Future

class NovaTraderControllerSpec extends SpecBase {

  private val testTrader = TraderResponse(
    vrn                   = "123456789",
    status                = Some("REGD"),
    traderName            = Some("Test Trader Ltd"),
    tradingName           = Some("Test Trader"),
    addressLine1          = Some("1 Test Street"),
    addressLine2          = Some("Test Town"),
    addressLine3          = None,
    addressLine4          = None,
    postcode              = Some("TE1 1ST"),
    email                 = Some("test@test.com"),
    phoneNumber           = Some("01234567890"),
    mobileNumber          = None,
    tradeClass            = Some("47"),
    tradeClassDescription = Some("Retail trade"),
    organisationType      = Some("LIMITED_COMPANY"),
    effectiveRegDate      = Some("2000-01-01"),
    ceasedDate            = None,
    certIssuedDate        = Some("2000-01-10"),
    nextReturnPeDate      = Some("2026-03-31"),
    returnStagger         = Some("MAR"),
    redundant             = false,
    insolvent             = false,
    missingTrader         = false
  )

  private val testDetailsNoClient = TraderDetailsResponse(userTrader = testTrader, clientTrader = None)

  private val testClientTrader = testTrader.copy(vrn = "987654321", traderName = Some("Client Ltd"))
  private val testDetailsWithClient = TraderDetailsResponse(userTrader = testTrader, clientTrader = Some(testClientTrader))

  "NovaTraderController#getTraderDetails" - {

    "return 200 with trader details when no clientVrn is supplied (Individual/Organisation)" in new SetUp {
      when(mockNovaDataSource.getTraderDetails(any[String], any[Option[String]]))
        .thenReturn(Future.successful(Some(testDetailsNoClient)))

      val result: Future[Result] = controller.getTraderDetails("123456789", None)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(testDetailsNoClient)
      verify(mockNovaDataSource).getTraderDetails("123456789", None)
    }

    "return 200 with both user and client trader details when clientVrn is supplied (Agent)" in new SetUp {
      when(mockNovaDataSource.getTraderDetails(any[String], any[Option[String]]))
        .thenReturn(Future.successful(Some(testDetailsWithClient)))

      val result: Future[Result] = controller.getTraderDetails("123456789", Some("987654321"))(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(testDetailsWithClient)
      verify(mockNovaDataSource).getTraderDetails("123456789", Some("987654321"))
    }

    "return 404 when repository returns None" in new SetUp {
      when(mockNovaDataSource.getTraderDetails(any[String], any[Option[String]]))
        .thenReturn(Future.successful(None))

      val result: Future[Result] = controller.getTraderDetails("123456789", None)(fakeRequest)

      status(result) mustBe NOT_FOUND
      verify(mockNovaDataSource).getTraderDetails("123456789", None)
    }

    "return 400 when userVrn is empty" in new SetUp {
      val result: Future[Result] = controller.getTraderDetails("", None)(fakeRequest)

      status(result) mustBe BAD_REQUEST
      contentType(result) mustBe Some("application/json")
      (contentAsJson(result) \ "error").as[String] mustBe "userVrn must be provided"
      verifyNoInteractions(mockNovaDataSource)
    }

    "return 400 when userVrn is whitespace only" in new SetUp {
      val result: Future[Result] = controller.getTraderDetails("   ", None)(fakeRequest)

      status(result) mustBe BAD_REQUEST
      contentType(result) mustBe Some("application/json")
      (contentAsJson(result) \ "error").as[String] mustBe "userVrn must be provided"
      verifyNoInteractions(mockNovaDataSource)
    }

    "return 500 when the repository call fails" in new SetUp {
      when(mockNovaDataSource.getTraderDetails(any[String], any[Option[String]]))
        .thenReturn(Future.failed(new RuntimeException("Database error")))

      val result: Future[Result] = controller.getTraderDetails("123456789", None)(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some("application/json")
      (contentAsJson(result) \ "error").as[String] mustBe "Failed to retrieve trader details"
    }

    "strip whitespace from clientVrn before passing to repository" in new SetUp {
      when(mockNovaDataSource.getTraderDetails(any[String], any[Option[String]]))
        .thenReturn(Future.successful(Some(testDetailsWithClient)))

      controller.getTraderDetails("123456789", Some("  987654321  "))(fakeRequest).futureValue

      verify(mockNovaDataSource).getTraderDetails("123456789", Some("987654321"))
    }

    "treat whitespace-only clientVrn as None" in new SetUp {
      when(mockNovaDataSource.getTraderDetails(any[String], any[Option[String]]))
        .thenReturn(Future.successful(Some(testDetailsNoClient)))

      controller.getTraderDetails("123456789", Some("   "))(fakeRequest).futureValue

      verify(mockNovaDataSource).getTraderDetails("123456789", None)
    }
  }

  private class SetUp {
    val mockNovaDataSource: NovaDataSource = mock[NovaDataSource]
    val controller: NovaTraderController = new NovaTraderController(fakeAuthAction, mockNovaDataSource, cc)
  }
}
