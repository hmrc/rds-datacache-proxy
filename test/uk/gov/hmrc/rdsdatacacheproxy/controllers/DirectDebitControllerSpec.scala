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

package uk.gov.hmrc.rdsdatacacheproxy.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.models.requests.WorkingDaysOffsetRequest
import uk.gov.hmrc.rdsdatacacheproxy.models.responses.{DirectDebit, EarliestPaymentDate, UserDebits}
import uk.gov.hmrc.rdsdatacacheproxy.models.{MonthlyReturn, UserMonthlyReturns}
import uk.gov.hmrc.rdsdatacacheproxy.services.{DirectDebitService, MonthlyReturnService}

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.Future

class DirectDebitControllerSpec extends SpecBase with MockitoSugar {
  "DirectDebitController" - {

    "retrieveDirectDebits" - {
      "return 200 and a successful response when DB returns records" in new SetUp {
        when(mockDirectDebitService.retrieveDirectDebits(any[String]))
          .thenReturn(Future.successful(nonEmptyResponse))
        val result: Future[Result] = controller.retrieveDirectDebits()(fakeRequest)

        status(result) shouldBe OK
        contentType(result) shouldBe Some("application/json")
        contentAsJson(result) shouldBe Json.toJson(nonEmptyResponse)
      }

      "return 200 and an empty records when no data returned from DB" in new SetUp {
        when(mockDirectDebitService.retrieveDirectDebits(any[String]))
          .thenReturn(Future.successful(emptyResponse))
        val result: Future[Result] = controller.retrieveDirectDebits()(fakeRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(emptyResponse)
      }
    }

  }

  private class SetUp {
    val mockDirectDebitService: DirectDebitService = mock[DirectDebitService]

    val emptyResponse: UserDebits =
      UserDebits(0, Seq.empty)

    val nonEmptyResponse: UserDebits =
      UserDebits(1, Seq(DirectDebit(
        "0123456789",
        LocalDateTime.of(2025, 12, 12, 12, 12),
        "123456",
        "12345678",
        "DDBank",
        false,
        2
      )))

    val controller =
      new DirectDebitController(fakeAuthAction, mockDirectDebitService, cc)
  }
}