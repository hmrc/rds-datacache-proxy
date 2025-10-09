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

package uk.gov.hmrc.rdsdatacacheproxy.repositories

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.rdsdatacacheproxy.models.requests.PaymentPlanDuplicateCheckRequest
import uk.gov.hmrc.rdsdatacacheproxy.utils.StubUtils
import uk.gov.hmrc.rdsdatacacheproxy.models.responses.*

import java.time.{LocalDate, LocalDateTime}
import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext.Implicits.global

class RDSStubSpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience:

  val connector: RdsStub = new RdsStub() {
    override val stubData: StubUtils = new StubUtils {
      override def randomDirectDebit(i: Int): DirectDebit =
        DirectDebit.apply(
          ddiRefNumber = s"defaultRef$i",
          LocalDateTime.parse("2020-02-02T22:22:22"),
          "00-00-00",
          "00000000",
          "BankLtd",
          false,
          i
        )
    }
  }
  def expected(i: Int): DirectDebit = connector.stubData.randomDirectDebit(i)

  "RDSStub" should:
    "return a DirectDebit" in:
      val result = connector.getDirectDebits("123").futureValue

      result shouldBe UserDebits(5, Seq(expected(1), expected(2), expected(3), expected(4), expected(5)))

    "return earliest payment date" in:
      val result = connector.addFutureWorkingDays(LocalDate.of(2025, 12, 15), 10).futureValue

      result shouldBe EarliestPaymentDate(LocalDate.of(2025, 12, 25))

    "return ddi reference number" in:
      val result = connector.getDirectDebitReference("xyz", "000123", "session-123").futureValue

      result shouldBe DDIReference("xyz")

    "return a DirectDebit Payment Plans" in {
      val directDebits = connector.getDirectDebits("123").futureValue

      val result = connector.getDirectDebitPaymentPlans(directDebits.directDebitList.head.ddiRefNumber, "credId").futureValue

      result.paymentPlanCount shouldBe directDebits.directDebitList.head.numberOfPayPlans
    }

    "return error when DirectDebit is not found" in {
      val result = connector.getDirectDebitPaymentPlans("invalid dd reference", "credId")

      result.recover { case ex: NoSuchElementException =>
        ex.getMessage should include("No DirectDebit found with ddiRefNumber: invalid dd reference")
      }
    }

    "return a Single Payment Plan Details" in {
      val currentTime = LocalDateTime.now().withNano(0)

      val paymentPlanDetails = PaymentPlanDetails(
        directDebitDetails = DirectDebitDetail(bankSortCode = Some("123456"),
                                               bankAccountNumber  = Some("12345678"),
                                               bankAccountName    = Some("Bank Ltd"),
                                               auDdisFlag         = true,
                                               submissionDateTime = currentTime
                                              ),
        paymentPlanDetails = PaymentPlanDetail(
          hodService                = "CESA",
          planType                  = "01",
          paymentReference          = "4558540144K",
          submissionDateTime        = currentTime,
          scheduledPaymentAmount    = Some(1000),
          scheduledPaymentStartDate = Some(currentTime.toLocalDate.plusDays(4)),
          initialPaymentStartDate   = Some(currentTime.toLocalDate),
          initialPaymentAmount      = Some(150),
          scheduledPaymentEndDate   = Some(currentTime.toLocalDate.plusMonths(10)),
          scheduledPaymentFrequency = Some(2),
          suspensionStartDate       = Some(currentTime.toLocalDate),
          suspensionEndDate         = Some(currentTime.toLocalDate),
          balancingPaymentAmount    = Some(600),
          balancingPaymentDate      = Some(currentTime.toLocalDate),
          totalLiability            = Some(300),
          paymentPlanEditable       = false
        )
      )

      val result = connector.getPaymentPlanDetails("dd reference", "0000000009000201", "payment reference").futureValue

      result shouldBe paymentPlanDetails
    }

    "return a Single Payment Plan Details and credId ending with 05" in {
      val currentTime = LocalDateTime.now().withNano(0)

      val paymentPlanDetails = PaymentPlanDetails(
        directDebitDetails = DirectDebitDetail(bankSortCode = Some("123456"),
                                               bankAccountNumber  = Some("12345678"),
                                               bankAccountName    = Some("Bank Ltd"),
                                               auDdisFlag         = true,
                                               submissionDateTime = currentTime
                                              ),
        paymentPlanDetails = PaymentPlanDetail(
          hodService                = "CESA",
          planType                  = "01",
          paymentReference          = "4558540144K",
          submissionDateTime        = currentTime,
          scheduledPaymentAmount    = Some(1000),
          scheduledPaymentStartDate = Some(currentTime.toLocalDate.plusDays(4)),
          initialPaymentStartDate   = Some(currentTime.toLocalDate),
          initialPaymentAmount      = Some(150),
          scheduledPaymentEndDate   = Some(currentTime.toLocalDate.plusMonths(10)),
          scheduledPaymentFrequency = Some(2),
          suspensionStartDate       = Some(currentTime.toLocalDate),
          suspensionEndDate         = Some(currentTime.toLocalDate),
          balancingPaymentAmount    = Some(600),
          balancingPaymentDate      = Some(currentTime.toLocalDate),
          totalLiability            = Some(300),
          paymentPlanEditable       = false
        )
      )

      val result = connector.getPaymentPlanDetails("dd reference", "0000000009000205", "payment plan reference").futureValue

      result shouldBe paymentPlanDetails
    }

    "return a Budget Payment Plan Details" in {
      val currentTime = LocalDateTime.now().withNano(0)

      val paymentPlanDetails = PaymentPlanDetails(
        directDebitDetails = DirectDebitDetail(bankSortCode = Some("123456"),
                                               bankAccountNumber  = Some("12345678"),
                                               bankAccountName    = Some("Bank Ltd"),
                                               auDdisFlag         = true,
                                               submissionDateTime = currentTime
                                              ),
        paymentPlanDetails = PaymentPlanDetail(
          hodService                = "CESA",
          planType                  = "02",
          paymentReference          = "4558540144K",
          submissionDateTime        = currentTime,
          scheduledPaymentAmount    = Some(1000),
          scheduledPaymentStartDate = Some(currentTime.toLocalDate.plusDays(4)),
          initialPaymentStartDate   = Some(currentTime.toLocalDate),
          initialPaymentAmount      = Some(150),
          scheduledPaymentEndDate   = Some(currentTime.toLocalDate.plusMonths(10)),
          scheduledPaymentFrequency = Some(5),
          suspensionStartDate       = Some(currentTime.toLocalDate),
          suspensionEndDate         = Some(currentTime.toLocalDate),
          balancingPaymentAmount    = Some(600),
          balancingPaymentDate      = Some(currentTime.toLocalDate),
          totalLiability            = Some(300),
          paymentPlanEditable       = false
        )
      )

      val result = connector.getPaymentPlanDetails("dd reference", "0000000009000202", "payment reference").futureValue

      result shouldBe paymentPlanDetails
    }

    "return a Budget Payment Plan Details when credId ending with 04" in {
      val currentTime = LocalDateTime.now().withNano(0)

      val paymentPlanDetails = PaymentPlanDetails(
        directDebitDetails = DirectDebitDetail(bankSortCode = Some("123456"),
                                               bankAccountNumber  = Some("12345678"),
                                               bankAccountName    = Some("Bank Ltd"),
                                               auDdisFlag         = true,
                                               submissionDateTime = currentTime
                                              ),
        paymentPlanDetails = PaymentPlanDetail(
          hodService                = "CESA",
          planType                  = "02",
          paymentReference          = "4558540144K",
          submissionDateTime        = currentTime,
          scheduledPaymentAmount    = Some(1000),
          scheduledPaymentStartDate = Some(currentTime.toLocalDate.plusDays(4)),
          initialPaymentStartDate   = Some(currentTime.toLocalDate),
          initialPaymentAmount      = Some(150),
          scheduledPaymentEndDate   = Some(currentTime.toLocalDate.plusMonths(10)),
          scheduledPaymentFrequency = Some(5),
          suspensionStartDate       = Some(currentTime.toLocalDate),
          suspensionEndDate         = Some(currentTime.toLocalDate),
          balancingPaymentAmount    = Some(600),
          balancingPaymentDate      = Some(currentTime.toLocalDate),
          totalLiability            = Some(300),
          paymentPlanEditable       = false
        )
      )

      val result = connector.getPaymentPlanDetails("dd reference", "0000000009000204", "payment plan reference").futureValue

      result shouldBe paymentPlanDetails
    }

    "return a Tax Credit Repayment Plan Details" in {
      val currentTime = LocalDateTime.now().withNano(0)

      val paymentPlanDetails = PaymentPlanDetails(
        directDebitDetails = DirectDebitDetail(bankSortCode = Some("123456"),
                                               bankAccountNumber  = Some("12345678"),
                                               bankAccountName    = Some("Bank Ltd"),
                                               auDdisFlag         = true,
                                               submissionDateTime = currentTime
                                              ),
        paymentPlanDetails = PaymentPlanDetail(
          hodService                = "CESA",
          planType                  = "03",
          paymentReference          = "4558540144K",
          submissionDateTime        = currentTime,
          scheduledPaymentAmount    = Some(1000),
          scheduledPaymentStartDate = Some(currentTime.toLocalDate.plusDays(4)),
          initialPaymentStartDate   = Some(currentTime.toLocalDate),
          initialPaymentAmount      = Some(150),
          scheduledPaymentEndDate   = Some(currentTime.toLocalDate.plusMonths(10)),
          scheduledPaymentFrequency = None,
          suspensionStartDate       = Some(currentTime.toLocalDate),
          suspensionEndDate         = Some(currentTime.toLocalDate),
          balancingPaymentAmount    = Some(600),
          balancingPaymentDate      = Some(currentTime.toLocalDate),
          totalLiability            = Some(300),
          paymentPlanEditable       = false
        )
      )

      val result = connector.getPaymentPlanDetails("dd reference", "0000000009000203", "payment reference").futureValue

      result shouldBe paymentPlanDetails
    }

    "return a VPP Payment Plan Details" in {
      val currentTime = LocalDateTime.now().withNano(0)

      val paymentPlanDetails = PaymentPlanDetails(
        directDebitDetails = DirectDebitDetail(bankSortCode = Some("123456"),
                                               bankAccountNumber  = Some("12345678"),
                                               bankAccountName    = Some("Bank Ltd"),
                                               auDdisFlag         = true,
                                               submissionDateTime = currentTime
                                              ),
        paymentPlanDetails = PaymentPlanDetail(
          hodService                = "CESA",
          planType                  = "04",
          paymentReference          = "4558540144K",
          submissionDateTime        = currentTime,
          scheduledPaymentAmount    = Some(1000),
          scheduledPaymentStartDate = Some(currentTime.toLocalDate.plusDays(4)),
          initialPaymentStartDate   = Some(currentTime.toLocalDate),
          initialPaymentAmount      = Some(150),
          scheduledPaymentEndDate   = Some(currentTime.toLocalDate.plusMonths(10)),
          scheduledPaymentFrequency = None,
          suspensionStartDate       = Some(currentTime.toLocalDate),
          suspensionEndDate         = Some(currentTime.toLocalDate),
          balancingPaymentAmount    = Some(600),
          balancingPaymentDate      = Some(currentTime.toLocalDate),
          totalLiability            = Some(300),
          paymentPlanEditable       = false
        )
      )

      val result = connector.getPaymentPlanDetails("dd reference", "123", "payment reference").futureValue

      result shouldBe paymentPlanDetails
    }

    "return true if duplicate payment plan" in {

      val duplicateCheckRequest: PaymentPlanDuplicateCheckRequest = PaymentPlanDuplicateCheckRequest(
        directDebitReference = "testRef",
        paymentPlanReference = "payment ref 123",
        planType             = "01",
        paymentService       = "CESA",
        paymentReference     = "payment ref",
        paymentAmount        = 120.00,
        totalLiability       = 780.00,
        paymentFrequency     = "1"
      )

      val result: DuplicateCheckResponse = connector.isDuplicatePaymentPlan("testRef", "0000000009000201", duplicateCheckRequest).futureValue

      result shouldBe DuplicateCheckResponse(true)
    }

    "return true if duplicate payment plan and credId with 05" in {

      val duplicateCheckRequest: PaymentPlanDuplicateCheckRequest = PaymentPlanDuplicateCheckRequest(
        directDebitReference = "testRef",
        paymentPlanReference = "payment ref 123",
        planType             = "01",
        paymentService       = "CESA",
        paymentReference     = "payment ref",
        paymentAmount        = 120.00,
        totalLiability       = 780.00,
        paymentFrequency     = "1"
      )

      val result: DuplicateCheckResponse = connector.isDuplicatePaymentPlan("testRef", "0000000009000205", duplicateCheckRequest).futureValue

      result shouldBe DuplicateCheckResponse(false)
    }

    "return false if not a duplicate payment plan" in {

      val duplicateCheckRequest: PaymentPlanDuplicateCheckRequest = PaymentPlanDuplicateCheckRequest(
        directDebitReference = "testRef",
        paymentPlanReference = "payment ref 123",
        planType             = "02",
        paymentService       = "CESA",
        paymentReference     = "payment ref",
        paymentAmount        = 120.00,
        totalLiability       = 780.00,
        paymentFrequency     = "WEEKLY"
      )

      val result: DuplicateCheckResponse = connector.isDuplicatePaymentPlan("dd reference", "0000000009000202", duplicateCheckRequest).futureValue

      result shouldBe DuplicateCheckResponse(true)
    }

    "return false if not a duplicate payment plan and credId ending with 04" in {

      val duplicateCheckRequest: PaymentPlanDuplicateCheckRequest = PaymentPlanDuplicateCheckRequest(
        directDebitReference = "testRef",
        paymentPlanReference = "payment ref 123",
        planType             = "02",
        paymentService       = "CESA",
        paymentReference     = "payment ref",
        paymentAmount        = 120.00,
        totalLiability       = 780.00,
        paymentFrequency     = "WEEKLY"
      )

      val result: DuplicateCheckResponse = connector.isDuplicatePaymentPlan("dd reference", "0000000009000204", duplicateCheckRequest).futureValue

      result shouldBe DuplicateCheckResponse(false)
    }
