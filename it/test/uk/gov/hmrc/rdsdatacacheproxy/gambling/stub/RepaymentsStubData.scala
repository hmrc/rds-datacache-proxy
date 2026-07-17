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

import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{ActualRepaymentItem, ActualRepayments, RepaymentsSummary}

import java.time.LocalDate

object RepaymentsStubData {
  def getRepaymentsSummaryData(regNumber: String): RepaymentsSummary =
    regNumber match {
      case "XGM00003122200" =>
        RepaymentsSummary(
          periodStartDate                = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate                  = Some(LocalDate.of(2014, 3, 11)),
          actualRepaymentsAmount         = BigDecimal(71.84),
          repaymentsInterestRepaidAmount = BigDecimal(-35.76),
          total                          = BigDecimal(36.08)
        )

      case "XGM00003155555" =>
        RepaymentsSummary(
          periodStartDate                = Some(LocalDate.of(2023, 3, 1)),
          periodEndDate                  = Some(LocalDate.of(2024, 3, 11)),
          actualRepaymentsAmount         = BigDecimal(0),
          repaymentsInterestRepaidAmount = BigDecimal(0),
          total                          = BigDecimal(0)
        )
      case "XXM33333066666" => throw new RuntimeException("Simulated downstream failure")
      case _ =>
        RepaymentsSummary(
          periodStartDate                = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate                  = Some(LocalDate.of(2014, 3, 11)),
          actualRepaymentsAmount         = BigDecimal(171.84),
          repaymentsInterestRepaidAmount = BigDecimal(-35.76),
          total                          = BigDecimal(136.08)
        )
    }

  def getActualRepaymentsData(regNumber: String): ActualRepayments =
    regNumber match {
      case "XGM00003122200" =>
        ActualRepayments(
          periodStartDate  = Some(LocalDate.of(2013, 1, 1)),
          periodEndDate    = Some(LocalDate.of(2014, 11, 3)),
          total            = BigDecimal(-3250.00),
          totalRecords     = 2,
          items = Seq(
            ActualRepaymentItem(transactionDate = LocalDate.of(2014, 9, 15), amount = BigDecimal(-1500.00)),
            ActualRepaymentItem(transactionDate = LocalDate.of(2014, 6, 30), amount = BigDecimal(-1750.00))
          )
        )

      case "XYZ99999999999" =>
        ActualRepayments(
          periodStartDate  = Some(LocalDate.of(2023, 3, 1)),
          periodEndDate    = Some(LocalDate.of(2024, 3, 11)),
          total            = BigDecimal(0),
          totalRecords     = 0,
          items = Seq.empty
        )
      case "XXM33333066666" => throw new RuntimeException("Simulated downstream failure")
      case _ =>
        ActualRepayments(
          periodStartDate  = Some(LocalDate.of(2013, 1, 1)),
          periodEndDate    = Some(LocalDate.of(2014, 11, 3)),
          total            = BigDecimal(-1750.00),
          totalRecords     = 1,
          items = Seq(
            ActualRepaymentItem(transactionDate = LocalDate.of(2014, 6, 30), amount = BigDecimal(-1750.00))
          )
        )
    }

}
