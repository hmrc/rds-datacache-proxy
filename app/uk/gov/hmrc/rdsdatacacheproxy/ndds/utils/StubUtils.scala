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

package uk.gov.hmrc.rdsdatacacheproxy.ndds.utils

import uk.gov.hmrc.rdsdatacacheproxy.ndds.models.responses.{DirectDebit, PaymentPlan}

import java.time.LocalDateTime
import scala.util.Random

class StubUtils {

  private def r(range: Int) = {
    val random = s"${Random.nextInt(range) + 1}"
    val prepend = if (random.length == 1) "0" else ""
    prepend + random
  }

  def randomDirectDebit(i: Int, hasPagination: Boolean): DirectDebit = {

    val ddiRefNumber = s"9905502$i"

    val paymentPlanCount = getPaymentCount(ddiRefNumber, hasPagination)

    val (accountNumber, sortCode, bankName) = getBankDetails(ddiRefNumber)

    val date = s"${Random.nextInt(5) + 2022}" +
      s"-${r(12)}" +
      s"-${r(28)}"
    DirectDebit.apply(
      ddiRefNumber = ddiRefNumber,
      LocalDateTime.parse(s"${date}T00:00:00"),
      sortCode,
      accountNumber,
      bankName,
      Random.nextBoolean(),
      paymentPlanCount
    )
  }

  def randomPaymentPlan(i: Int): PaymentPlan = {
    val date = s"${Random.nextInt(5) + 2022}" +
      s"-${r(12)}" +
      s"-${r(28)}"

    val planTypes = Seq("01", "02")
    val randomPlanType = planTypes(Random.nextInt(planTypes.length))

    val hodServices = Seq("COTA", "NIDN", "SAFE", "PAYE", "CESA", "SDLT", "NTC", "VAT", "MGD")

    val randomHodService = hodServices(Random.nextInt(hodServices.length))

    PaymentPlan.apply(
      scheduledPaymentAmount = i * 100.0,
      planRefNumber          = s"20000080$i",
      planType               = randomPlanType,
      paymentReference       = s"{$i}400256374K",
      hodService             = randomHodService,
      submissionDateTime     = LocalDateTime.parse(s"${date}T00:00:00")
    )
  }

  def getPaymentCount(ddiRefNumber: String, hasPagination: Boolean): Int = {
    val ddRef = ddiRefNumber.toInt
    if (hasPagination) {
      ddRef % 16
    } else {
      (ddRef % 3) + 1 // making sure that there will be at least one
    }
  }

  def getBankDetails(ddiRefNumber: String): (String, String, String) = {
    val ddRef = ddiRefNumber.toInt
    val bankName = "BankLtd"
    if (ddRef % 5 == 1) {
      ("12344321", "001003", bankName)
    } else if (ddRef % 5 == 2) {
      ("12349876", "235678", bankName)
    } else if (ddRef % 5 == 3) {
      ("45671235", "983427", bankName)
    } else if (ddRef % 5 == 4) {
      ("98051256", "872537", bankName)
    } else {
      ("76894567", "286517", bankName)
    }
  }
}
