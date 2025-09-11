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

package uk.gov.hmrc.rdsdatacacheproxy.utils

import uk.gov.hmrc.rdsdatacacheproxy.models.responses.{DirectDebit, PaymentPlan}
import uk.gov.hmrc.rdsdatacacheproxy.models.MonthlyReturn

import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicLong
import scala.util.Random

class StubUtils {

  private def r(range: Int) = {
    val random = s"${Random.nextInt(range) + 1}"
    val prepend = if (random.length == 1) "0" else ""
    prepend + random
  }

  def randomDirectDebit(i: Int): DirectDebit = {
    val date = s"${Random.nextInt(5) + 2022}" +
      s"-${r(12)}" +
      s"-${r(28)}"
    DirectDebit.apply(
      ddiRefNumber = s"defaultRef$i",
      LocalDateTime.parse(s"${date}T00:00:00"),
      s"${r(99)}-${r(99)}-${r(99)}",
      Seq.fill(8)(Random.nextInt(10)).mkString,
      "BankLtd",
      Random.nextBoolean(),
      Random.nextInt(100)
    )
  }

  private val mrId = new AtomicLong(1000000L)

  def randomMonthlyReturn(): MonthlyReturn = {
    val id = mrId.incrementAndGet()
    val year = 2023 + Random.nextInt(3)
    val month = 1 + Random.nextInt(12)
    val now = LocalDateTime.now().minusDays(Random.nextInt(365).toLong)
      .withHour(0).withMinute(0).withSecond(0).withNano(0)

    MonthlyReturn(
      monthlyReturnId = id,
      taxYear = year,
      taxMonth = month,
      nilReturnIndicator = Some(if (Random.nextBoolean()) "Y" else "N"),
      decEmpStatusConsidered = Some(if (Random.nextBoolean()) "Y" else "N"),
      decAllSubsVerified = Some(if (Random.nextBoolean()) "Y" else "N"),
      decInformationCorrect = Some(if (Random.nextBoolean()) "Y" else "N"),
      decNoMoreSubPayments = Some(if (Random.nextBoolean()) "Y" else "N"),
      decNilReturnNoPayments = Some(if (Random.nextBoolean()) "Y" else "N"),
      status = Some(Seq("STARTED", "SUBMITTED")(Random.nextInt(2))),
      lastUpdate = Some(now),
      amendment = Some(if (Random.nextBoolean()) "Y" else "N"),
      supersededBy = None
    )
  }

  def randomPaymentPlan(i: Int): PaymentPlan = {
    val date = s"${Random.nextInt(5) + 2022}" +
      s"-${r(12)}" +
      s"-${r(28)}"
    PaymentPlan.apply(
      scheduledPaymentAmount = i * 100.0,
      planRefNumber = "ddpaymentReference",
      planType = s"planType$i",
      paymentReference = s"payReference$i",
      hodService = s"planHoldService$i",
      submissionDateTime = LocalDateTime.parse(s"${date}T00:00:00")
    )
  }
}
