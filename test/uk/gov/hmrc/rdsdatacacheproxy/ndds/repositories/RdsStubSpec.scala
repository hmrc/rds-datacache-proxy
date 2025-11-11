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

package uk.gov.hmrc.rdsdatacacheproxy.ndds.repositories

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.rdsdatacacheproxy.ndds.models.requests.PaymentPlanDuplicateCheckRequest
import uk.gov.hmrc.rdsdatacacheproxy.ndds.utils.StubUtils
import uk.gov.hmrc.rdsdatacacheproxy.ndds.models.responses.*

import java.time.{LocalDate, LocalDateTime}
import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext.Implicits.global

class RdsStubSpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience:

  val connector: RdsStub = new RdsStub() {
    override val stubData: StubUtils = new StubUtils {
      override def randomDirectDebit(i: Int, hasPagination: Boolean): DirectDebit =
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
  def expected(i: Int): DirectDebit = connector.stubData.randomDirectDebit(i, false)

  "RDSStub" should:
    "return a DirectDebit" in:
      val result = connector.getDirectDebits("123").futureValue

      result shouldBe UserDebits(5, Seq(expected(1), expected(2), expected(3), expected(4), expected(5)))

    "return a DirectDebit without data when credId ends with 7g0" in:
      val result = connector.getDirectDebits("00000000090007g0").futureValue

      result shouldBe UserDebits(0, Seq.empty)

    "return earliest payment date" in:
      val result = connector.addFutureWorkingDays(LocalDate.of(2025, 12, 15), 10).futureValue

      result shouldBe EarliestPaymentDate(LocalDate.of(2025, 12, 25))

    "return ddi reference number" in:
      val result = connector.getDirectDebitReference("xyz", "000123", "session-123").futureValue

      result.ddiRefNumber.length shouldBe 10

    "return a DirectDebit Payment Plans" in {
      val result = connector.getDirectDebitPaymentPlans("123100", "credId").futureValue

      result.bankSortCode      shouldBe "286517"
      result.bankAccountNumber shouldBe "76894567"
      result.bankAccountName   shouldBe "BankLtd"
      result.paymentPlanCount  shouldBe 2
    }

    "return a Single Payment Plan Details when credId is 0000000009000201" in {
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
          submissionDateTime        = currentTime.minusDays(5),
          scheduledPaymentAmount    = Some(100),
          scheduledPaymentStartDate = Some(currentTime.toLocalDate.plusDays(4)),
          initialPaymentStartDate   = Some(currentTime.toLocalDate),
          initialPaymentAmount      = Some(100),
          scheduledPaymentEndDate   = Some(currentTime.toLocalDate.plusMonths(10)),
          scheduledPaymentFrequency = Some(2),
          suspensionStartDate       = None,
          suspensionEndDate         = None,
          balancingPaymentAmount    = Some(100),
          balancingPaymentDate      = Some(currentTime.toLocalDate),
          totalLiability            = Some(1200),
          paymentPlanEditable       = false
        )
      )

      val result = connector.getPaymentPlanDetails("dd reference", "0000000009000201", "payment reference").futureValue

      result shouldBe paymentPlanDetails
    }

    "return a Single Payment Plan Details when credId ends with 1a5" in {
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
          submissionDateTime        = currentTime.minusDays(5),
          scheduledPaymentAmount    = Some(100),
          scheduledPaymentStartDate = Some(currentTime.toLocalDate.plusDays(4)),
          initialPaymentStartDate   = Some(currentTime.toLocalDate),
          initialPaymentAmount      = Some(100),
          scheduledPaymentEndDate   = Some(currentTime.toLocalDate.plusMonths(10)),
          scheduledPaymentFrequency = Some(2),
          suspensionStartDate       = None,
          suspensionEndDate         = None,
          balancingPaymentAmount    = Some(100),
          balancingPaymentDate      = Some(currentTime.toLocalDate),
          totalLiability            = Some(1200),
          paymentPlanEditable       = false
        )
      )

      val result = connector.getPaymentPlanDetails("dd reference", "00000000090001a5", "payment reference").futureValue

      result shouldBe paymentPlanDetails
    }

    "return a Single Payment Plan Details and credId is 0000000009000205" in {
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
          submissionDateTime        = currentTime.minusDays(5),
          scheduledPaymentAmount    = Some(100),
          scheduledPaymentStartDate = Some(currentTime.toLocalDate.plusDays(4)),
          initialPaymentStartDate   = Some(currentTime.toLocalDate),
          initialPaymentAmount      = Some(100),
          scheduledPaymentEndDate   = Some(currentTime.toLocalDate.plusMonths(10)),
          scheduledPaymentFrequency = None,
          suspensionStartDate       = None,
          suspensionEndDate         = None,
          balancingPaymentAmount    = Some(100),
          balancingPaymentDate      = Some(currentTime.toLocalDate),
          totalLiability            = Some(1200),
          paymentPlanEditable       = false
        )
      )

      val result = connector.getPaymentPlanDetails("dd reference", "0000000009000205", "payment plan reference").futureValue

      result shouldBe paymentPlanDetails
    }

    "return a Budget Payment Plan Details when credId is 0000000009000202" in {
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
          submissionDateTime        = currentTime.minusDays(5),
          scheduledPaymentAmount    = Some(100),
          scheduledPaymentStartDate = Some(currentTime.toLocalDate.plusDays(4)),
          initialPaymentStartDate   = Some(currentTime.toLocalDate),
          initialPaymentAmount      = Some(100),
          scheduledPaymentEndDate   = Some(currentTime.toLocalDate.plusMonths(12)),
          scheduledPaymentFrequency = Some(5),
          suspensionStartDate       = None,
          suspensionEndDate         = None,
          balancingPaymentAmount    = Some(100),
          balancingPaymentDate      = Some(currentTime.toLocalDate),
          totalLiability            = Some(1200),
          paymentPlanEditable       = false
        )
      )

      val result = connector.getPaymentPlanDetails("dd reference", "0000000009000202", "payment reference").futureValue

      result shouldBe paymentPlanDetails
    }

    "return a Budget Payment Plan Details when credId ends with 2b6" in {
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
          submissionDateTime        = currentTime.minusDays(5),
          scheduledPaymentAmount    = Some(100),
          scheduledPaymentStartDate = Some(currentTime.toLocalDate.plusDays(4)),
          initialPaymentStartDate   = Some(currentTime.toLocalDate),
          initialPaymentAmount      = Some(100),
          scheduledPaymentEndDate   = Some(currentTime.toLocalDate.plusMonths(12)),
          scheduledPaymentFrequency = Some(5),
          suspensionStartDate       = None,
          suspensionEndDate         = None,
          balancingPaymentAmount    = Some(100),
          balancingPaymentDate      = Some(currentTime.toLocalDate),
          totalLiability            = Some(1200),
          paymentPlanEditable       = false
        )
      )

      val result = connector.getPaymentPlanDetails("dd reference", "00000000090002b6", "payment reference").futureValue

      result shouldBe paymentPlanDetails
    }

    "return a Budget Payment Plan Details when credId is 0000000009000204" in {
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
          submissionDateTime        = currentTime.minusDays(5),
          scheduledPaymentAmount    = Some(100),
          scheduledPaymentStartDate = Some(currentTime.toLocalDate.plusDays(4)),
          initialPaymentStartDate   = Some(currentTime.toLocalDate),
          initialPaymentAmount      = Some(100),
          scheduledPaymentEndDate   = Some(currentTime.toLocalDate.plusMonths(12)),
          scheduledPaymentFrequency = Some(5),
          suspensionStartDate       = None,
          suspensionEndDate         = None,
          balancingPaymentAmount    = Some(100),
          balancingPaymentDate      = Some(currentTime.toLocalDate),
          totalLiability            = Some(1200),
          paymentPlanEditable       = false
        )
      )

      val result = connector.getPaymentPlanDetails("dd reference", "0000000009000204", "payment plan reference").futureValue

      result shouldBe paymentPlanDetails
    }

    "return a Budget Payment Plan Details when credId is 0000000009000206" in {
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
          submissionDateTime        = currentTime.minusDays(5),
          scheduledPaymentAmount    = Some(100),
          scheduledPaymentStartDate = Some(currentTime.toLocalDate.plusDays(5)),
          initialPaymentStartDate   = Some(currentTime.toLocalDate),
          initialPaymentAmount      = Some(100),
          scheduledPaymentEndDate   = Some(currentTime.toLocalDate.plusMonths(12)),
          scheduledPaymentFrequency = Some(5),
          suspensionStartDate       = Some(currentTime.toLocalDate.plusMonths(1)),
          suspensionEndDate         = Some(currentTime.toLocalDate.plusMonths(2)),
          balancingPaymentAmount    = Some(100),
          balancingPaymentDate      = Some(currentTime.toLocalDate),
          totalLiability            = Some(1200),
          paymentPlanEditable       = false
        )
      )

      val result = connector.getPaymentPlanDetails("dd reference", "0000000009000206", "payment plan reference").futureValue

      result shouldBe paymentPlanDetails
    }

    "return a Budget Payment Plan Details when credId ends with 4d8" in {
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
          submissionDateTime        = currentTime.minusDays(5),
          scheduledPaymentAmount    = Some(100),
          scheduledPaymentStartDate = Some(currentTime.toLocalDate.plusDays(5)),
          initialPaymentStartDate   = Some(currentTime.toLocalDate),
          initialPaymentAmount      = Some(100),
          scheduledPaymentEndDate   = Some(currentTime.toLocalDate.plusMonths(12)),
          scheduledPaymentFrequency = Some(5),
          suspensionStartDate       = Some(currentTime.toLocalDate.plusMonths(1)),
          suspensionEndDate         = Some(currentTime.toLocalDate.plusMonths(2)),
          balancingPaymentAmount    = Some(100),
          balancingPaymentDate      = Some(currentTime.toLocalDate),
          totalLiability            = Some(1200),
          paymentPlanEditable       = false
        )
      )

      val result = connector.getPaymentPlanDetails("dd reference", "00000000090004d8", "payment plan reference").futureValue

      result shouldBe paymentPlanDetails
    }

    "return a Budget Payment Plan Details when credId ends with 6f0" in {
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
          submissionDateTime        = currentTime.minusDays(5),
          scheduledPaymentAmount    = Some(100),
          scheduledPaymentStartDate = Some(currentTime.toLocalDate.plusDays(5)),
          initialPaymentStartDate   = Some(currentTime.toLocalDate),
          initialPaymentAmount      = Some(100),
          scheduledPaymentEndDate   = Some(currentTime.toLocalDate.plusMonths(12)),
          scheduledPaymentFrequency = Some(5),
          suspensionStartDate       = Some(currentTime.toLocalDate.plusMonths(1)),
          suspensionEndDate         = Some(currentTime.toLocalDate.plusMonths(2)),
          balancingPaymentAmount    = Some(100),
          balancingPaymentDate      = Some(currentTime.toLocalDate),
          totalLiability            = Some(1200),
          paymentPlanEditable       = false
        )
      )

      val result = connector.getPaymentPlanDetails("dd reference", "00000000090006f0", "payment plan reference").futureValue

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
          submissionDateTime        = currentTime.minusDays(5),
          scheduledPaymentAmount    = Some(100),
          scheduledPaymentStartDate = Some(currentTime.toLocalDate.plusDays(4)),
          initialPaymentStartDate   = Some(currentTime.toLocalDate),
          initialPaymentAmount      = Some(100),
          scheduledPaymentEndDate   = Some(currentTime.toLocalDate.plusMonths(10)),
          scheduledPaymentFrequency = None,
          suspensionStartDate       = None,
          suspensionEndDate         = None,
          balancingPaymentAmount    = Some(100),
          balancingPaymentDate      = Some(currentTime.toLocalDate),
          totalLiability            = Some(1200),
          paymentPlanEditable       = false
        )
      )

      val result = connector.getPaymentPlanDetails("dd reference", "0000000009000203", "payment reference").futureValue

      result shouldBe paymentPlanDetails
    }

    "return a Tax Credit Repayment Plan Details credId ends with 5e9" in {
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
          submissionDateTime        = currentTime.minusDays(5),
          scheduledPaymentAmount    = Some(100),
          scheduledPaymentStartDate = Some(currentTime.toLocalDate.plusDays(4)),
          initialPaymentStartDate   = Some(currentTime.toLocalDate),
          initialPaymentAmount      = Some(100),
          scheduledPaymentEndDate   = Some(currentTime.toLocalDate.plusMonths(10)),
          scheduledPaymentFrequency = None,
          suspensionStartDate       = None,
          suspensionEndDate         = None,
          balancingPaymentAmount    = Some(100),
          balancingPaymentDate      = Some(currentTime.toLocalDate),
          totalLiability            = Some(1200),
          paymentPlanEditable       = false
        )
      )

      val result = connector.getPaymentPlanDetails("dd reference", "00000000090005e9", "payment reference").futureValue

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
          submissionDateTime        = currentTime.minusDays(5),
          scheduledPaymentAmount    = Some(100),
          scheduledPaymentStartDate = Some(currentTime.toLocalDate.plusDays(4)),
          initialPaymentStartDate   = Some(currentTime.toLocalDate),
          initialPaymentAmount      = Some(100),
          scheduledPaymentEndDate   = Some(currentTime.toLocalDate.plusMonths(10)),
          scheduledPaymentFrequency = None,
          suspensionStartDate       = None,
          suspensionEndDate         = None,
          balancingPaymentAmount    = Some(100),
          balancingPaymentDate      = Some(currentTime.toLocalDate),
          totalLiability            = Some(1200),
          paymentPlanEditable       = false
        )
      )

      val result = connector.getPaymentPlanDetails("dd reference", "123", "payment reference").futureValue

      result shouldBe paymentPlanDetails
    }

    "return a VPP Payment Plan Details when credId ends with 3c7" in {
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
          submissionDateTime        = currentTime.minusDays(5),
          scheduledPaymentAmount    = Some(100),
          scheduledPaymentStartDate = Some(currentTime.toLocalDate.plusDays(4)),
          initialPaymentStartDate   = Some(currentTime.toLocalDate),
          initialPaymentAmount      = Some(100),
          scheduledPaymentEndDate   = Some(currentTime.toLocalDate.plusMonths(10)),
          scheduledPaymentFrequency = None,
          suspensionStartDate       = None,
          suspensionEndDate         = None,
          balancingPaymentAmount    = Some(100),
          balancingPaymentDate      = Some(currentTime.toLocalDate),
          totalLiability            = Some(1200),
          paymentPlanEditable       = false
        )
      )

      val result = connector.getPaymentPlanDetails("dd reference", "1233c7", "payment reference").futureValue

      result shouldBe paymentPlanDetails
    }

  "return PaymentPlanLock" in {
    val result = connector.lockPaymentPlan("payment reference", "123").futureValue

    result shouldBe PaymentPlanLock(lockSuccessful = true)
  }

  "return false if not a duplicate payment plan and credId ending with 01" in {
    val currentDate = LocalDate.now()

    val duplicateCheckRequest: PaymentPlanDuplicateCheckRequest = PaymentPlanDuplicateCheckRequest(
      directDebitReference = "testRef",
      paymentPlanReference = "payment ref 123",
      planType             = "01",
      paymentService       = "CESA",
      paymentReference     = "payment ref",
      paymentAmount        = Some(120.00),
      totalLiability       = Some(780.00),
      paymentFrequency     = Some(1),
      paymentStartDate     = currentDate
    )

    val result: DuplicateCheckResponse = connector.isDuplicatePaymentPlan("testRef", "0000000009000201", duplicateCheckRequest).futureValue

    result shouldBe DuplicateCheckResponse(false)
  }

  "return true if a duplicate payment plan and credId with 05" in {
    val currentDate = LocalDate.now()

    val duplicateCheckRequest: PaymentPlanDuplicateCheckRequest = PaymentPlanDuplicateCheckRequest(
      directDebitReference = "testRef",
      paymentPlanReference = "payment ref 123",
      planType             = "01",
      paymentService       = "CESA",
      paymentReference     = "payment ref",
      paymentAmount        = Some(120.00),
      totalLiability       = Some(780.00),
      paymentFrequency     = None,
      paymentStartDate     = currentDate
    )

    val result: DuplicateCheckResponse = connector.isDuplicatePaymentPlan("testRef", "0000000009000205", duplicateCheckRequest).futureValue

    result shouldBe DuplicateCheckResponse(true)
  }

  "return false if not a duplicate payment plan and credId ending with 02" in {
    val currentDate = LocalDate.now()

    val duplicateCheckRequest: PaymentPlanDuplicateCheckRequest = PaymentPlanDuplicateCheckRequest(
      directDebitReference = "testRef",
      paymentPlanReference = "payment ref 123",
      planType             = "02",
      paymentService       = "CESA",
      paymentReference     = "payment ref",
      paymentAmount        = Some(120.00),
      totalLiability       = Some(780.00),
      paymentFrequency     = Some(1),
      paymentStartDate     = currentDate
    )

    val result: DuplicateCheckResponse = connector.isDuplicatePaymentPlan("dd reference", "0000000009000202", duplicateCheckRequest).futureValue

    result shouldBe DuplicateCheckResponse(false)
  }

  "return true if duplicate payment plan and credId ending with 04" in {
    val currentDate = LocalDate.now()

    val duplicateCheckRequest: PaymentPlanDuplicateCheckRequest = PaymentPlanDuplicateCheckRequest(
      directDebitReference = "testRef",
      paymentPlanReference = "payment ref 123",
      planType             = "02",
      paymentService       = "CESA",
      paymentReference     = "payment ref",
      paymentAmount        = Some(120.00),
      totalLiability       = Some(780.00),
      paymentFrequency     = Some(1),
      paymentStartDate     = currentDate
    )

    val result: DuplicateCheckResponse = connector.isDuplicatePaymentPlan("dd reference", "0000000009000204", duplicateCheckRequest).futureValue

    result shouldBe DuplicateCheckResponse(true)
  }

  "return advance notice details and credId ending with 08" in {
    val currentTime = LocalDateTime.now().withNano(0)

    val result = connector.isAdvanceNoticePresent("payment reference", "0000000009000208").futureValue

    result shouldBe AdvanceNoticeResponse(
      totalAmount = Some(500),
      dueDate     = Some(currentTime.toLocalDate.plusMonths(1))
    )
  }

  "return advance notice details as None and credId ending with 09" in {
    val result = connector.isAdvanceNoticePresent("0000000009000209", "payment reference").futureValue

    result shouldBe AdvanceNoticeResponse(
      totalAmount = None,
      dueDate     = None
    )
  }
