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

import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{InterestDetails, InterestDetailItem}

import java.time.LocalDate

object RepaymentInterestDetailsStubData {
  def getRepaymentInterestDetailsData(regNumber: String, paginationStart: Int = 1, paginationMaxRows: Int = 10): InterestDetails =
    regNumber match {
      case "XYZ00000000000" =>
        InterestDetails(
          periodStartDate = Some(LocalDate.of(2013, 1, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
          total           = BigDecimal(0.00),
          totalRecords    = 0,
          items           = Seq()
        )
      case "XYZ00000000001" =>
        InterestDetails(
          periodStartDate = Some(LocalDate.of(2013, 1, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
          total           = BigDecimal(-2600.00),
          totalRecords    = 3,
          items = Seq(
            InterestDetailItem(
              descriptionCode = 2740,
              amount          = -800.00,
              interestId      = "SAFE-CHG-00003",
              periodStartDate = LocalDate.of(2014, 1, 1),
              periodEndDate   = LocalDate.of(2014, 3, 31)
            ),
            InterestDetailItem(
              descriptionCode = 2740,
              amount          = -400.00,
              interestId      = "SAFE-CHG-00004",
              periodStartDate = LocalDate.of(2014, 10, 1),
              periodEndDate   = LocalDate.of(2014, 12, 31)
            ),
            InterestDetailItem(
              descriptionCode = 2740,
              amount          = -1400.00,
              interestId      = "SAFE-CHG-00005",
              periodStartDate = LocalDate.of(2013, 4, 1),
              periodEndDate   = LocalDate.of(2013, 6, 30)
            )
          )
        )
      case "XYZ00000000010" =>
        InterestDetails(
          periodStartDate = Some(LocalDate.of(2013, 1, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
          total           = BigDecimal(-45.00),
          totalRecords    = 1,
          items = Seq(
            InterestDetailItem(
              descriptionCode = 2740,
              amount          = -45.00,
              interestId      = "SAFE-CHG-00001",
              periodStartDate = LocalDate.of(2014, 7, 22),
              periodEndDate   = LocalDate.of(2014, 10, 31)
            )
          )
        )

      case "XYZ00000000012" =>
        InterestDetails(
          periodStartDate = Some(LocalDate.of(2013, 1, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
          total           = BigDecimal(-3100.00),
          totalRecords    = 4,
          items = Seq(
            InterestDetailItem(
              descriptionCode = 2740,
              amount          = -800.00,
              interestId      = "SAFE-CHG-00003",
              periodStartDate = LocalDate.of(2014, 1, 1),
              periodEndDate   = LocalDate.of(2014, 3, 31)
            ),
            InterestDetailItem(
              descriptionCode = 2740,
              amount          = -400.00,
              interestId      = "SAFE-CHG-00004",
              periodStartDate = LocalDate.of(2014, 10, 1),
              periodEndDate   = LocalDate.of(2014, 12, 31)
            ),
            InterestDetailItem(
              descriptionCode = 2740,
              amount          = -1400.00,
              interestId      = "SAFE-CHG-00005",
              periodStartDate = LocalDate.of(2013, 4, 1),
              periodEndDate   = LocalDate.of(2013, 6, 30)
            ),
            InterestDetailItem(
              descriptionCode = 2740,
              amount          = -500.00,
              interestId      = "SAFE-CHG-00006",
              periodStartDate = LocalDate.of(2014, 9, 1),
              periodEndDate   = LocalDate.of(2014, 9, 30)
            )
          )
        )
      case "XYZ00000000021" =>
        InterestDetails(
          periodStartDate = Some(LocalDate.of(2013, 1, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
          total           = BigDecimal(-2600.00),
          totalRecords    = 3,
          items = Seq(
            InterestDetailItem(
              descriptionCode = 2740,
              amount          = -800.00,
              interestId      = "SAFE-CHG-00003",
              periodStartDate = LocalDate.of(2014, 1, 1),
              periodEndDate   = LocalDate.of(2014, 3, 31)
            ),
            InterestDetailItem(
              descriptionCode = 2740,
              amount          = -400.00,
              interestId      = "SAFE-CHG-00004",
              periodStartDate = LocalDate.of(2014, 10, 1),
              periodEndDate   = LocalDate.of(2014, 12, 31)
            ),
            InterestDetailItem(
              descriptionCode = 2740,
              amount          = -1400.00,
              interestId      = "SAFE-CHG-00005",
              periodStartDate = LocalDate.of(2013, 4, 1),
              periodEndDate   = LocalDate.of(2013, 6, 30)
            )
          )
        )
      case "XYZ99999999999" =>
        InterestDetails(
          periodStartDate = Some(LocalDate.of(2013, 1, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
          total           = 999.00,
          totalRecords    = 99,
          items           = Seq()
        )
      case "ERR00000000000" => throw new RuntimeException("Simulated downstream failure")
      case _ =>
        InterestDetails(
          periodStartDate = Some(LocalDate.of(2013, 1, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
          total           = -1200.00,
          totalRecords    = 2,
          items = Seq(
            InterestDetailItem(
              descriptionCode = 2740,
              amount          = -800.00,
              interestId      = "SAFE-CHG-00003",
              periodStartDate = LocalDate.of(2014, 1, 1),
              periodEndDate   = LocalDate.of(2014, 3, 31)
            ),
            InterestDetailItem(
              descriptionCode = 2740,
              amount          = -400.00,
              interestId      = "SAFE-CHG-00004",
              periodStartDate = LocalDate.of(2014, 10, 1),
              periodEndDate   = LocalDate.of(2014, 12, 31)
            )
          )
        )
    }

}
