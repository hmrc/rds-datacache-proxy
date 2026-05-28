/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.stub

import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.RepaymentsSummary

import java.time.LocalDate

object RepaymentsStubData {
  def getRepaymentsSummaryData(regNumber: String): RepaymentsSummary =
    regNumber match {
      case "XYZ00000000000" =>
        RepaymentsSummary(
          periodStartDate                = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate                  = Some(LocalDate.of(2014, 3, 11)),
          actualRepaymentsAmount         = BigDecimal(71.84),
          repaymentsInterestRepaidAmount = BigDecimal(-35.76),
          total                          = BigDecimal(36.08)
        )

      case "XYZ99999999999" =>
        RepaymentsSummary(
          periodStartDate                = Some(LocalDate.of(2023, 3, 1)),
          periodEndDate                  = Some(LocalDate.of(2024, 3, 11)),
          actualRepaymentsAmount         = BigDecimal(0),
          repaymentsInterestRepaidAmount = BigDecimal(0),
          total                          = BigDecimal(0)
        )
      case "ERR00000000000" => throw new RuntimeException("Simulated downstream failure")
      case _ =>
        RepaymentsSummary(
          periodStartDate                = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate                  = Some(LocalDate.of(2014, 3, 11)),
          actualRepaymentsAmount         = BigDecimal(171.84),
          repaymentsInterestRepaidAmount = BigDecimal(-35.76),
          total                          = BigDecimal(136.08)
        )
    }

}
