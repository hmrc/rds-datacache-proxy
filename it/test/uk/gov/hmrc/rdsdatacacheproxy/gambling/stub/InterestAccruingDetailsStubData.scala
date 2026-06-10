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

import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{InterestAccruingDetailsItem, InterestAccruingDetails}

import java.time.LocalDate

object InterestAccruingDetailsStubData {

  def getInterestAccruingDetailsData(regNumber: String, paginationStart: Int = 1, paginationMaxRows: Int = 10): InterestAccruingDetails =
    regNumber match {
      case "XYZ00000000000" =>
        InterestAccruingDetails(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
          total           = Some(0.00),
          totalRecords    = Some(0),
          items           = Seq()
        )
      case "XYZ00000000001" =>
        InterestAccruingDetails(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
          total           = Some(-24500.00),
          totalRecords    = Some(3),
          items = Seq(
            InterestAccruingDetailsItem(
              descriptionCode = Some(1),
              amount          = Some(-9500.00),
              interestId      = Some("SAFE-CHG-00001"),
              periodStartDate = Some(LocalDate.of(2014, 4, 1)),
              periodEndDate   = Some(LocalDate.of(2014, 6, 30))
            ),
            InterestAccruingDetailsItem(
              descriptionCode = Some(2),
              amount          = Some(-8000.00),
              interestId      = Some("SAFE-CHG-00002"),
              periodStartDate = Some(LocalDate.of(2014, 1, 1)),
              periodEndDate   = Some(LocalDate.of(2014, 3, 31))
            ),
            InterestAccruingDetailsItem(
              descriptionCode = Some(3),
              amount          = Some(-7000.00),
              interestId      = Some("SAFE-CHG-00003"),
              periodStartDate = Some(LocalDate.of(2013, 10, 1)),
              periodEndDate   = Some(LocalDate.of(2013, 12, 31))
            )
          )
        )
      case "XYZ00000000010" =>
        InterestAccruingDetails(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
          total           = Some(-1000.00),
          totalRecords    = Some(1),
          items = Seq(
            InterestAccruingDetailsItem(
              descriptionCode = Some(1),
              amount          = Some(-9500.00),
              interestId      = Some("SAFE-CHG-00001"),
              periodStartDate = Some(LocalDate.of(2014, 4, 1)),
              periodEndDate   = Some(LocalDate.of(2014, 6, 30))
            )
          )
        )
      case "XYZ00000000012" =>
        InterestAccruingDetails(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
          total           = Some(-34500.00),
          totalRecords    = Some(4),
          items = Seq(
            InterestAccruingDetailsItem(
              descriptionCode = Some(1),
              amount          = Some(-9500.00),
              interestId      = Some("SAFE-CHG-00001"),
              periodStartDate = Some(LocalDate.of(2014, 4, 1)),
              periodEndDate   = Some(LocalDate.of(2014, 6, 30))
            ),
            InterestAccruingDetailsItem(
              descriptionCode = Some(2),
              amount          = Some(-8000.00),
              interestId      = Some("SAFE-CHG-00002"),
              periodStartDate = Some(LocalDate.of(2014, 1, 1)),
              periodEndDate   = Some(LocalDate.of(2014, 3, 31))
            ),
            InterestAccruingDetailsItem(
              descriptionCode = Some(3),
              amount          = Some(-7000.00),
              interestId      = Some("SAFE-CHG-00003"),
              periodStartDate = Some(LocalDate.of(2013, 10, 1)),
              periodEndDate   = Some(LocalDate.of(2013, 12, 31))
            ),
            InterestAccruingDetailsItem(
              descriptionCode = Some(4),
              amount          = Some(5555.00),
              interestId      = Some("SAFE-CHG-00004"),
              periodStartDate = Some(LocalDate.of(2013, 10, 1)),
              periodEndDate   = Some(LocalDate.of(2013, 12, 31))
            )
          )
        )
      case "XYZ00000000021" =>
        InterestAccruingDetails(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
          total           = Some(-44500.00),
          totalRecords    = Some(3),
          items = Seq(
            InterestAccruingDetailsItem(
              descriptionCode = Some(1),
              amount          = Some(-9500.00),
              interestId      = Some("SAFE-CHG-00001"),
              periodStartDate = Some(LocalDate.of(2014, 4, 1)),
              periodEndDate   = Some(LocalDate.of(2014, 6, 30))
            ),
            InterestAccruingDetailsItem(
              descriptionCode = Some(2),
              amount          = Some(-8000.00),
              interestId      = Some("SAFE-CHG-00002"),
              periodStartDate = Some(LocalDate.of(2014, 1, 1)),
              periodEndDate   = Some(LocalDate.of(2014, 3, 31))
            ),
            InterestAccruingDetailsItem(
              descriptionCode = Some(3),
              amount          = Some(-7000.00),
              interestId      = Some("SAFE-CHG-00003"),
              periodStartDate = Some(LocalDate.of(2013, 10, 1)),
              periodEndDate   = Some(LocalDate.of(2013, 12, 31))
            )
          )
        )
      case "XYZ99999999999" =>
        InterestAccruingDetails(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
          total           = Some(999.00),
          totalRecords    = Some(99),
          items           = Seq()
        )
      case "ERR00000000000" => throw new RuntimeException("Simulated downstream failure")
      case _ =>
        InterestAccruingDetails(
          periodStartDate = Some(LocalDate.of(2023, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2024, 3, 11)),
          total           = Some(-4500.00),
          totalRecords    = Some(1),
          items = Seq(
            InterestAccruingDetailsItem(
              descriptionCode = Some(1),
              amount          = Some(-4500.00),
              interestId      = Some("SAFE-CHG-DEFAULT"),
              periodStartDate = Some(LocalDate.of(2024, 4, 1)),
              periodEndDate   = Some(LocalDate.of(2024, 6, 30))
            )
          )
        )
    }
}
