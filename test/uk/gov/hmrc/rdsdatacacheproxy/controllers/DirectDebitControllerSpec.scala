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
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.models.requests.PaymentPlanDuplicateCheckRequest
import uk.gov.hmrc.rdsdatacacheproxy.models.responses.{DirectDebit, DuplicateCheckResponse, UserDebits}
import uk.gov.hmrc.rdsdatacacheproxy.services.DirectDebitService

import java.time.LocalDateTime
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

    "isDuplicatePaymentPlan method" - {
      "return 200 and a successful response when duplicate payment plan exist" in new SetUp {
        when(
          mockDirectDebitService.isDuplicatePaymentPlan(
            any[String],
            any[String],
            any[PaymentPlanDuplicateCheckRequest]
          )
        ).thenReturn(Future.successful(DuplicateCheckResponse(true)))

        val request: FakeRequest[PaymentPlanDuplicateCheckRequest] =
          FakeRequest()
            .withBody(duplicateCheckRequest)

        val result: Future[Result] =
          controller.isDuplicatePaymentPlan("testDuplicatePaymentPlan")(request)

        status(result) shouldBe OK
        contentType(result) shouldBe Some("application/json")
        contentAsJson(result) shouldBe Json.toJson(DuplicateCheckResponse(true))
      }

      "return 500 and log error when DB call fails" in new SetUp {
        val exception = new RuntimeException("DB error")

        when(
          mockDirectDebitService.isDuplicatePaymentPlan(
            any[String],
            any[String],
            any[PaymentPlanDuplicateCheckRequest]
          )
        ).thenReturn(Future.failed(exception))

        val request: FakeRequest[PaymentPlanDuplicateCheckRequest] =
          FakeRequest()
            .withBody(duplicateCheckRequest)

        val result: Future[Result] =
          controller.isDuplicatePaymentPlan("testDuplicatePaymentPlan")(request)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) should include("Failed to retrieve earliest data from oracle database.")
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

    val duplicateCheckRequest: PaymentPlanDuplicateCheckRequest = PaymentPlanDuplicateCheckRequest(
      directDebitReference = "testRef",
      paymentPlanReference = "payment ref 123",
      planType = "type 1",
      paymentService = "CESA",
      paymentReference = "payment ref",
      paymentAmount = 120.00,
      totalLiability = 780.00,
      paymentFrequency = "WEEKLY"
    )
  }
}