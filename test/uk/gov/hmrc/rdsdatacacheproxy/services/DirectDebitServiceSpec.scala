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

package uk.gov.hmrc.rdsdatacacheproxy.services

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.rdsdatacacheproxy.repositories.RdsStub
import uk.gov.hmrc.rdsdatacacheproxy.models.responses.{DDIReference, DDPaymentPlans, DirectDebit, DirectDebitDetail, EarliestPaymentDate, PaymentPlanDetail, PaymentPlanDetails, UserDebits}

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}

class DirectDebitServiceSpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with MockitoSugar
    with IntegrationPatience:

  implicit val ec: ExecutionContext = global

  private val mockConnector = mock[RdsStub]
  private val service = new DirectDebitService(mockConnector)

  def expected(i: Int): DirectDebit =
    DirectDebit.apply(
      s"defaultRef$i",
      LocalDateTime.parse("2020-02-02T22:22:22"),
      "00-00-00",
      "00000000",
      "Bank Ltd",
      false,
      i
    )

  "DirectDebitService" should:
    "succeed" when:
      "retrieving no Direct Debits" in:
        when(mockConnector.getDirectDebits(any()))
          .thenReturn(Future.successful(UserDebits(0, Seq())))

          val result = service.retrieveDirectDebits("testId").futureValue
          result shouldBe UserDebits(0, Seq())

      "retrieving Direct Debits" in:
        when(mockConnector.getDirectDebits(any()))
          .thenReturn(
            Future.successful(UserDebits(1, Seq(expected(1)))),
            Future.successful(UserDebits(3, Seq(expected(2),expected(3),expected(4)))),
          )
          val result = service.retrieveDirectDebits("testId").futureValue
          result shouldBe UserDebits(1, Seq(expected(1)))
          val result2 = service.retrieveDirectDebits("testId").futureValue
          result2 shouldBe UserDebits(3, Seq(expected(2),expected(3),expected(4)))

      "retrieving Earliest Payment Date" in:
        when(mockConnector.addFutureWorkingDays(any(), any()))
          .thenReturn(Future.successful(EarliestPaymentDate(LocalDate.of(2025, 10, 20))))
        val result = service.addFutureWorkingDays(LocalDate.of(2025, 10, 15), 5).futureValue
        result shouldBe EarliestPaymentDate(LocalDate.of(2025, 10, 20))

      "retrieving DDI reference number" in :
        when(mockConnector.getDirectDebitReference(any(), any(), any()))
          .thenReturn(Future.successful(DDIReference("xyz")))
        val result = service.getDDIReference("xyz", "123", "session-345").futureValue
        result shouldBe DDIReference("xyz")

      "retrieving no Direct Debits Payment Plans" in :
        val paymentPlans = DDPaymentPlans("sort code", "account number", "account name", "dd", 0, Seq())
        when(mockConnector.getDirectDebitPaymentPlans(any(), any()))
          .thenReturn(Future.successful(paymentPlans))

        val result = service.getDirectDebitPaymentPlans("ddReference", "testId").futureValue
        result shouldBe paymentPlans

      "retrieving Payment Plan Details" in {

        val currentTime = LocalDateTime.now()

        val paymentPlanDetails = PaymentPlanDetails(
          directDebitDetails = DirectDebitDetail(
            bankSortCode = Some("sort code"),
            bankAccountNumber = Some("account number"),
            bankAccountName = Some("account name"),
            auDdisFlag = Some("dd"),
            submissionDateTime = currentTime),
          paymentPlanDetails = PaymentPlanDetail(
            hodService = "01",
            planType = "SA",
            paymentReference = "payment Reference",
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
        when(mockConnector.getPaymentPlanDetails(any(), any(), any()))
          .thenReturn(Future.successful(paymentPlanDetails))

        val result = service.getPaymentPlanDetails("ddReference", "testId", "payment Reference").futureValue
        result shouldBe paymentPlanDetails
      }


    "fail" when:
      "retrieving Direct Debits" in:
        when(mockConnector.getDirectDebits(any()))
          .thenReturn(Future.failed(new Exception("bang")))

        val result = intercept[Exception](service.retrieveDirectDebits("testId").futureValue)
        result.getMessage should include("bang")
