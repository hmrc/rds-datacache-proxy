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

package uk.gov.hmrc.rdsdatacacheproxy.euvat.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.euvat.models.responses.TradersKnownFacts
import uk.gov.hmrc.rdsdatacacheproxy.euvat.services.EuVatService
import uk.gov.hmrc.rdsdatacacheproxy.euvat.base.SpecBase

import java.time.LocalDateTime
import scala.concurrent.Future

class EuVatControllerSpec extends SpecBase with MockitoSugar {
  "EuVatController" - {

    "retrieveTraderKnownFacts" - {
      "return 200 and a successful response when DB returns records" in new SetUp {
        when(mockEuVatService.retrieveTraderByVrn(any[String]))
          .thenReturn(Future.successful(Some(knownFactsResponse)))
        val result: Future[Result] = controller.retrieveTraderByVrn()(fakeRequest)

        status(result)        shouldBe OK
        contentType(result)   shouldBe Some("application/json")
        contentAsJson(result) shouldBe Json.toJson(knownFactsResponse)
      }

      "return 200 and an empty records when no data returned from DB" in new SetUp {
        when(mockEuVatService.retrieveTraderByVrn(any[String]))
          .thenReturn(Future.successful(Some(emptyKnownFactsResponse)))
        val result: Future[Result] = controller.retrieveTraderByVrn()(fakeRequest)

        status(result)        shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(emptyKnownFactsResponse)
      }

      "return 500 and log error when DB call fails" in new SetUp {
        val exception = new RuntimeException("DB error")
        when(mockEuVatService.retrieveTraderByVrn(any[String]))
          .thenReturn(Future.failed(exception))
        val result: Future[Result] = controller.retrieveTraderByVrn()(fakeRequest)

        status(result)        shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) should include("Failed to retrieve traders known facts")
      }
    }

  }

  private class SetUp {
    val mockEuVatService: EuVatService = mock[EuVatService]

    val emptyKnownFactsResponse: TradersKnownFacts =
      TradersKnownFacts(0, None, None, None, None, None, None, None, None, None, None, None)

    val knownFactsResponse: TradersKnownFacts =
      TradersKnownFacts(
        123456789,
        Some("TestData"),
        Some("Line 1"),
        Some("Line 2"),
        Some("Line 3"),
        Some("Line 4"),
        Some("Line 5"),
        Some("NE3 9TG"),
        Some("7020"),
        Some(LocalDateTime.of(2025, 1, 11, 10, 38)),
        Some(LocalDateTime.of(2026, 1, 11, 10, 38)),
        Some("N")
      )

    val controller = new EuVatController(fakeAuthAction, mockEuVatService, cc)

  }
}
