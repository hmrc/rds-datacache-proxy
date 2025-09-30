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
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.models.responses.{DDPaymentPlans, DirectDebit, DirectDebitDetail, PaymentPlan, PaymentPlanDetail, PaymentPlanDetails, UserDebits}
import uk.gov.hmrc.rdsdatacacheproxy.services.DirectDebitService

import java.time.LocalDateTime
import scala.concurrent.Future

class DirectDebitControllerSpec extends SpecBase with MockitoSugar {
  "DirectDebitController" - {

    "retrieveDirectDebits" - {
      "return 200 and a successful response when DB returns records" in new SetUp {
        when(mockDirectDebitService.retrieveDirectDebits(any[String]))
          .thenReturn(Future.successful(nonEmptyDirectDebitResponse))
        val result: Future[Result] = controller.retrieveDirectDebits()(fakeRequest)

        status(result) shouldBe OK
        contentType(result) shouldBe Some("application/json")
        contentAsJson(result) shouldBe Json.toJson(nonEmptyDirectDebitResponse)
      }

      "return 200 and an empty records when no data returned from DB" in new SetUp {
        when(mockDirectDebitService.retrieveDirectDebits(any[String]))
          .thenReturn(Future.successful(emptyDirectDebitResponse))
        val result: Future[Result] = controller.retrieveDirectDebits()(fakeRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(emptyDirectDebitResponse)
      }

      "return 500 and log error when DB call fails" in new SetUp {
        val exception = new RuntimeException("DB error")
        when(mockDirectDebitService.retrieveDirectDebits(any[String]))
          .thenReturn(Future.failed(exception))

        val result: Future[Result] = controller.retrieveDirectDebits()(fakeRequest)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) should include("Failed to retrieve earliest data from oracle database.")
      }
    }

    "retrieveDirectDebitPaymentPlans" - {
      "return 200 and a successful response when DB returns records" in new SetUp {
        when(mockDirectDebitService.getDirectDebitPaymentPlans(any[String], any[String]))
          .thenReturn(Future.successful(nonEmptyDirectDebitPaymentPlanResponse))
        val result: Future[Result] = controller.retrieveDirectDebitPaymentPlans("test reference")(fakeRequest)

        status(result) shouldBe OK
        contentType(result) shouldBe Some("application/json")
        contentAsJson(result) shouldBe Json.toJson(nonEmptyDirectDebitPaymentPlanResponse)
      }

      "return 200 and an empty records when no data returned from DB" in new SetUp {
        when(mockDirectDebitService.getDirectDebitPaymentPlans(any[String], any[String]))
          .thenReturn(Future.successful(emptyDirectDebitPaymentPlanResponse))
        val result: Future[Result] = controller.retrieveDirectDebitPaymentPlans("test reference")(fakeRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(emptyDirectDebitPaymentPlanResponse)
      }

      "return 500 and log error when DB call fails" in new SetUp {
        val exception = new RuntimeException("DB error")
        when(mockDirectDebitService.getDirectDebitPaymentPlans(any[String], any[String]))
          .thenReturn(Future.failed(exception))

        val result: Future[Result] = controller.retrieveDirectDebitPaymentPlans("test reference")(fakeRequest)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) should include("Failed to retrieve earliest data from oracle database.")
      }
    }

    "retrievePaymentPlanDetails" - {
      "return 200 and a successful response when DB returns records" in new SetUp {
        when(mockDirectDebitService.getPaymentPlanDetails(any[String], any[String], any[String]))
          .thenReturn(Future.successful(paymentPlanDetailsResponse))
        val result: Future[Result] = controller.retrievePaymentPlanDetails("dd reference", "test reference")(fakeRequest)

        status(result) shouldBe OK
        contentType(result) shouldBe Some("application/json")
        contentAsJson(result) shouldBe Json.toJson(paymentPlanDetailsResponse)
      }

      "return 500 and log error when DB call fails" in new SetUp {
        val exception = new RuntimeException("DB error")
        when(mockDirectDebitService.getPaymentPlanDetails(any[String], any[String], any[String]))
          .thenReturn(Future.failed(exception))

        val result: Future[Result] = controller.retrievePaymentPlanDetails("dd reference", "test reference")(fakeRequest)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) should include("Failed to retrieve earliest data from oracle database.")
      }
    }
  }

  private class SetUp {
    val mockDirectDebitService: DirectDebitService = mock[DirectDebitService]

    val emptyDirectDebitResponse: UserDebits =
      UserDebits(0, Seq.empty)

    val nonEmptyDirectDebitResponse: UserDebits =
      UserDebits(1, Seq(DirectDebit(
        "0123456789",
        LocalDateTime.of(2025, 12, 12, 12, 12),
        "123456",
        "12345678",
        "DDBank",
        false,
        2
      )))

    val emptyDirectDebitPaymentPlanResponse: DDPaymentPlans =
      DDPaymentPlans("sort code", "account number", "account name", "dd", 0, Seq())

    val nonEmptyDirectDebitPaymentPlanResponse: DDPaymentPlans =
      DDPaymentPlans("sort code", "account number", "account name", "dd", 0,
        Seq(
          PaymentPlan(
            150.0,
            "plan reference",
            "singlePaymentPlan",
            "payment reference",
            "dd",
            LocalDateTime.parse("2020-02-02T22:22:22")
          )
        ))

    private val currentTime = LocalDateTime.MIN

    val paymentPlanDetailsResponse = PaymentPlanDetails(
      directDebitDetails = DirectDebitDetail(
        bankSortCode = Some("sort code"),
        bankAccountNumber = Some("account number"),
        bankAccountName = Some("account name"),
        auDdisFlag = true,
        submissionDateTime = currentTime),
      paymentPlanDetails = PaymentPlanDetail(
        hodService = "hod service",
        planType = "plan Type",
        paymentReference = "payment reference",
        submissionDateTime = currentTime,
        scheduledPaymentAmount = Some(1000),
        scheduledPaymentStartDate = Some(currentTime.toLocalDate),
        initialPaymentStartDate = Some(currentTime.toLocalDate),
        initialPaymentAmount = Some(150),
        scheduledPaymentEndDate = Some(currentTime.toLocalDate),
        scheduledPaymentFrequency = Some("1"),
        suspensionStartDate = Some(currentTime.toLocalDate),
        suspensionEndDate = Some(currentTime.toLocalDate),
        balancingPaymentAmount = Some(600),
        balancingPaymentDate = Some(currentTime.toLocalDate),
        totalLiability = Some(300),
        paymentPlanEditable = false)
    )

    val controller =
      new DirectDebitController(fakeAuthAction, mockDirectDebitService, cc)
  }
}