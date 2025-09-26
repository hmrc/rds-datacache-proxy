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

import uk.gov.hmrc.rdsdatacacheproxy.models.responses.*
import uk.gov.hmrc.rdsdatacacheproxy.utils.StubUtils

import java.time.{LocalDate, LocalDateTime}
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class RdsStub @Inject()() extends RdsDataSource:

  // Remove this once real stubbing exists
  private[repositories] val stubData = new StubUtils()

  private lazy val debits: Seq[DirectDebit] = (1 to 5).map(stubData.randomDirectDebit)

  def getDirectDebits(id: String): Future[UserDebits] =
    Future.successful(UserDebits(debits.size, debits))

  def addFutureWorkingDays(baseDate: LocalDate, offsetWorkingDays: Int): Future[EarliestPaymentDate] =
    Future.successful(EarliestPaymentDate(baseDate.plusDays(offsetWorkingDays)))

  def getDirectDebitReference(paymentReference: String, credId: String, sessionId: String): Future[DDIReference] =
    Future.successful(DDIReference(paymentReference))

  def getDirectDebitPaymentPlans(directDebitReference: String, credId: String):
    Future[DDPaymentPlans] = {
    val filteredDebit = debits.find(_.ddiRefNumber == directDebitReference)
    filteredDebit match {
      case Some(debit) =>
        val plans: Seq[PaymentPlan] = for (i <- 1 to debit.numberOfPayPlans) yield stubData.randomPaymentPlan(i)
        Future.successful(DDPaymentPlans(debit.bankSortCode, debit.bankAccountNumber, debit.bankAccountName, "dd", plans.size, plans))
      case None =>
        Future.failed(new NoSuchElementException(s"No DirectDebit found with ddiRefNumber: $directDebitReference"))
    }
  }

  def getPaymentPlanDetails(directDebitReference: String, credId: String, paymentReference: String): Future[PaymentPlanDetails] = {

    val currentTime = LocalDateTime.MIN

    val paymentPlanDetails = PaymentPlanDetails(
      directDebitDetails = DirectDebitDetail(
        bankSortCode = "sort code",
        bankAccountNumber = "account number",
        bankAccountName = "account name",
        auDdisFlag = "dd",
        submissionDateTime = currentTime),
      paymentPlanDetails = PaymentPlanDetail(
        hodService = "hod service",
        planType = "plan Type",
        paymentReference = paymentReference,
        submissionDateTime = currentTime,
        scheduledPaymentAmount = 1000,
        scheduledPaymentStartDate = currentTime.toLocalDate,
        initialPaymentStartDate = currentTime.toLocalDate,
        initialPaymentAmount = 150,
        scheduledPaymentEndDate = currentTime.toLocalDate,
        scheduledPaymentFrequency = "monthly",
        suspensionStartDate = currentTime.toLocalDate,
        suspensionEndDate = currentTime.toLocalDate,
        balancingPaymentAmount = 600,
        balancingPaymentDate = currentTime.toLocalDate,
        totalLiability = 300,
        paymentPlanEditable = false)
    )
    Future.successful(paymentPlanDetails)
  }

