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

import uk.gov.hmrc.rdsdatacacheproxy.ndds.models.requests.PaymentPlanDuplicateCheckRequest
import uk.gov.hmrc.rdsdatacacheproxy.ndds.models.responses.*
import uk.gov.hmrc.rdsdatacacheproxy.ndds.utils.StubUtils

import java.time.{LocalDate, LocalDateTime}
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class RdsStub @Inject() () extends RdsDataSource:

  // Remove this once real stubbing exists
  private[repositories] val stubData = new StubUtils()

  private lazy val debits: Seq[DirectDebit] = (1 to 5).map(stubData.randomDirectDebit)

  def getDirectDebits(id: String): Future[UserDebits] = {
    if (id == "0000000009000200") {
      Future.successful(UserDebits(0, Seq.empty))
    } else {
      Future.successful(UserDebits(debits.size, debits))
    }
  }

  def addFutureWorkingDays(baseDate: LocalDate, offsetWorkingDays: Int): Future[EarliestPaymentDate] =
    Future.successful(EarliestPaymentDate(baseDate.plusDays(offsetWorkingDays)))

  def getDirectDebitReference(paymentReference: String, credId: String, sessionId: String): Future[DDIReference] =
    Future.successful(DDIReference(paymentReference))

  def getDirectDebitPaymentPlans(directDebitReference: String, credId: String): Future[DDPaymentPlans] = {
    val filteredDebit = debits.find(_.ddiRefNumber == directDebitReference)
    filteredDebit match {
      case Some(debit) =>
        val plans: Seq[PaymentPlan] = for (i <- 1 to debit.numberOfPayPlans) yield stubData.randomPaymentPlan(i)
        Future.successful(DDPaymentPlans(debit.bankSortCode, debit.bankAccountNumber, debit.bankAccountName, "dd", plans.size, plans))
      case None =>
        Future.failed(new NoSuchElementException(s"No DirectDebit found with ddiRefNumber: $directDebitReference"))
    }
  }

  def getPaymentPlanDetails(directDebitReference: String, credId: String, paymentPlanReference: String): Future[PaymentPlanDetails] = {

    val currentTime = LocalDateTime.now().withNano(0)

    val (playType, frequency, scheduledPaymentStartDate, scheduledPaymentEndDate, suspensionStartDate, suspensionEndDate) = Map(
      "0000000009000201" -> ("01", Some(2), Some(currentTime.toLocalDate.plusDays(4)), Some(currentTime.toLocalDate.plusMonths(10)), None, None),
      "0000000009000202" -> ("02", Some(5), Some(currentTime.toLocalDate.plusDays(4)), Some(currentTime.toLocalDate.plusMonths(12)), None, None),
      "0000000009000204" -> ("02", Some(5), Some(currentTime.toLocalDate.plusDays(4)), Some(currentTime.toLocalDate.plusMonths(12)), None, None),
      "0000000009000205" -> ("01", None, Some(currentTime.toLocalDate.plusDays(4)), Some(currentTime.toLocalDate.plusMonths(10)), None, None),
      "0000000009000203" -> ("03", None, Some(currentTime.toLocalDate.plusDays(4)), Some(currentTime.toLocalDate.plusMonths(10)), None, None),
      "0000000009000206" -> ("02",
                             Some(5),
                             Some(currentTime.toLocalDate.plusDays(5)),
                             Some(currentTime.toLocalDate.plusMonths(12)),
                             Some(currentTime.toLocalDate.plusMonths(1)),
                             Some(currentTime.toLocalDate.plusMonths(2))
                            )
    ).getOrElse(credId, ("04", None, Some(currentTime.toLocalDate.plusDays(4)), Some(currentTime.toLocalDate.plusMonths(10)), None, None))

    val paymentPlanDetails = PaymentPlanDetails(
      directDebitDetails = DirectDebitDetail(bankSortCode = Some("123456"),
                                             bankAccountNumber  = Some("12345678"),
                                             bankAccountName    = Some("Bank Ltd"),
                                             auDdisFlag         = true,
                                             submissionDateTime = currentTime
                                            ),
      paymentPlanDetails = PaymentPlanDetail(
        hodService                = "CESA",
        planType                  = playType,
        paymentReference          = "4558540144K",
        submissionDateTime        = currentTime.minusDays(5),
        scheduledPaymentAmount    = Some(100),
        scheduledPaymentStartDate = scheduledPaymentStartDate,
        initialPaymentStartDate   = Some(currentTime.toLocalDate),
        initialPaymentAmount      = Some(100),
        scheduledPaymentEndDate   = scheduledPaymentEndDate,
        scheduledPaymentFrequency = frequency,
        suspensionStartDate       = suspensionStartDate,
        suspensionEndDate         = suspensionEndDate,
        balancingPaymentAmount    = Some(100),
        balancingPaymentDate      = Some(currentTime.toLocalDate),
        totalLiability            = Some(1200),
        paymentPlanEditable       = false
      )
    )
    Future.successful(paymentPlanDetails)
  }

  def lockPaymentPlan(paymentPlanReference: String, credId: String): Future[PaymentPlanLock] = {
    Future.successful(PaymentPlanLock(lockSuccessful = true))
  }

  def isDuplicatePaymentPlan(
    directDebitReference: String,
    credId: String,
    request: PaymentPlanDuplicateCheckRequest
  ): Future[DuplicateCheckResponse] = {

    val flag = Map(
      "0000000009000201" -> false,
      "0000000009000202" -> false,
      "0000000009000204" -> true,
      "0000000009000205" -> true
    ).getOrElse(credId, false)

    Future.successful(DuplicateCheckResponse(flag))
  }
