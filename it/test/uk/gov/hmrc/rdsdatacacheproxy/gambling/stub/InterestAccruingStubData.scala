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

import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{InterestAccruingDrilldown, InterestAccruingDrilldownItem}

import java.time.LocalDate

object InterestAccruingStubData {

  def getInterestAccruingData(regNumber: String, interestId: String = "INT001", paginationStart: Int = 1, paginationMaxRows: Int = 10): InterestAccruingDrilldown =
    regNumber match {
      case "XYZ00000000000" =>
        InterestAccruingDrilldown(
          periodStartDate = Some(LocalDate.of(2013, 1, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
          total           = BigDecimal(0.00),
          totalRecords    = 0,
          descriptionCode        = None,
          items           = Seq.empty
        )

      case "XYZ00000000001" =>
        InterestAccruingDrilldown(
          periodStartDate = Some(LocalDate.of(2013, 1, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
          total           = BigDecimal(1250.50),
          totalRecords    = 1,
          descriptionCode        = Some(42),
          items = Seq(
            InterestAccruingDrilldownItem(
              interestOn = BigDecimal(1000.00),
              dateFrom   = LocalDate.of(2013, 6, 1),
              dateTo     = LocalDate.of(2014, 6, 1),
              noOfDays   = BigDecimal(365),
              rate       = BigDecimal(2.5),
              amount     = BigDecimal(1250.50)
            )
          )
        )

      case "XYZ99999999999" =>
        InterestAccruingDrilldown(
          periodStartDate = Some(LocalDate.of(2013, 2, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
          total           = BigDecimal(999.00),
          totalRecords    = 99,
          descriptionCode        = Some(10),
          items           = Seq.empty
        )

      case "ERR00000000000" => throw new RuntimeException("Simulated downstream failure")

      case _ =>
        InterestAccruingDrilldown(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
          total           = BigDecimal(500.00),
          totalRecords    = 2,
          descriptionCode        = Some(5),
          items = Seq(
            InterestAccruingDrilldownItem(
              interestOn = BigDecimal(200.00),
              dateFrom   = LocalDate.of(2013, 4, 1),
              dateTo     = LocalDate.of(2013, 10, 1),
              noOfDays   = BigDecimal(183),
              rate       = BigDecimal(1.5),
              amount     = BigDecimal(250.00)
            ),
            InterestAccruingDrilldownItem(
              interestOn = BigDecimal(300.00),
              dateFrom   = LocalDate.of(2013, 10, 1),
              dateTo     = LocalDate.of(2014, 3, 1),
              noOfDays   = BigDecimal(151),
              rate       = BigDecimal(1.5),
              amount     = BigDecimal(250.00)
            )
          )
        )
    }
}
