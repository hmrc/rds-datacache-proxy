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

import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.InterestBreakdownSummary

import java.time.LocalDate

object InterestBreakdownSummaryStubData {
  def getInterestBreakdownSummaryData(regNumber: String): InterestBreakdownSummary =
    regNumber match {
      case "XYZ00000000000" =>
        InterestBreakdownSummary(
          periodStartDate         = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate           = Some(LocalDate.of(2014, 3, 11)),
          interestAmount          = BigDecimal(-81.84),
          interestAccruingAmount  = BigDecimal(-25.76),
          repaymentInterestAmount = BigDecimal(41.23),
          total                   = BigDecimal(66.37)
        )

      case "XYZ99999999999" =>
        InterestBreakdownSummary(
          periodStartDate         = Some(LocalDate.of(2023, 3, 1)),
          periodEndDate           = Some(LocalDate.of(2024, 3, 11)),
          interestAmount          = BigDecimal(0),
          interestAccruingAmount  = BigDecimal(0),
          repaymentInterestAmount = BigDecimal(0),
          total                   = BigDecimal(0)
        )
      case "ERR00000000000" => throw new RuntimeException("Simulated downstream failure")
      case _ =>
        InterestBreakdownSummary(
          periodStartDate         = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate           = Some(LocalDate.of(2014, 3, 11)),
          interestAmount          = BigDecimal(-171.84),
          interestAccruingAmount  = BigDecimal(-35.76),
          repaymentInterestAmount = BigDecimal(85.76),
          total                   = BigDecimal(121.84)
        )
    }
}
