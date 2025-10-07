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

import uk.gov.hmrc.rdsdatacacheproxy.models.CisTaxpayer
import uk.gov.hmrc.rdsdatacacheproxy.models.responses.{DirectDebit, PaymentPlan}

import java.time.LocalDateTime
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
      ddiRefNumber = s"99055002$i",
      LocalDateTime.parse(s"${date}T00:00:00"),
      s"${r(99)}-${r(99)}-${r(99)}",
      Seq.fill(8)(Random.nextInt(10)).mkString,
      "BankLtd",
      Random.nextBoolean(),
      Random.nextInt(3) + 1
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

  def createCisTaxpayer(
    uniqueId: String = "1",
    taxOfficeNumber: String = "123",
    taxOfficeRef: String = "AB456",
    employerName1: Option[String] = Some("TEST LTD")
  ): CisTaxpayer =
    CisTaxpayer(
      uniqueId          = uniqueId,
      taxOfficeNumber   = taxOfficeNumber,
      taxOfficeRef      = taxOfficeRef,
      aoDistrict        = Some("123"),
      aoPayType         = Some("M"),
      aoCheckCode       = Some("XY"),
      aoReference       = Some("1234567XY"),
      validBusinessAddr = Some("Y"),
      correlation       = Some("corr-abc"),
      ggAgentId         = Some("AGENT-001"),
      employerName1     = employerName1,
      employerName2     = None,
      agentOwnRef       = Some("AG-REF-001"),
      schemeName        = Some("CIS Scheme"),
      utr               = Some("1234567890"),
      enrolledSig       = Some("Y")
    )
}
