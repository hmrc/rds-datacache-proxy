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

import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{Penalties, PenaltyItem}

import java.time.LocalDate

object PenaltiesStubData {
  def getPenaltiesData(regNumber: String, paginationStart: Int = 1, paginationMaxRows: Int = 10): Penalties =
    regNumber match {
      case "XYZ00000000000" =>
        Penalties(
          periodStartDate    = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate      = Some(LocalDate.of(2014, 3, 11)),
          total              = BigDecimal(0.00),
          totalRecords       = 0,
          items              = Seq()
        )
      case "XYZ00000000001" =>
        Penalties(
          periodStartDate    = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate      = Some(LocalDate.of(2014, 3, 11)),
          total              = BigDecimal(-1200.00),
          totalRecords       = 3,
          items     = Seq(
            PenaltyItem(
              dateRaised      = LocalDate.of(2014, 9, 1),
              descriptionCode = 2680,
              amount          = -800.00,
              periodStartDate = LocalDate.of(2014, 4, 1),
              periodEndDate   = LocalDate.of(2014, 6, 30)
            ),
            PenaltyItem(
              dateRaised      = LocalDate.of(2014, 4, 1),
              descriptionCode = 2690,
              amount          = -400.00,
              periodStartDate = LocalDate.of(2014, 1, 1),
              periodEndDate   = LocalDate.of(2014, 3, 31)
            ),
            PenaltyItem(
              dateRaised      = LocalDate.of(2013, 9, 1),
              descriptionCode = 2680,
              amount          = -1400.00,
              periodStartDate = LocalDate.of(2013, 4, 1),
              periodEndDate   = LocalDate.of(2013, 6, 30)
            )
          )
        )
      case "XYZ00000000010" =>
        Penalties(
          periodStartDate    = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate      = Some(LocalDate.of(2014, 3, 11)),
          total              = -2500.00,
          totalRecords       = 1,
          items     = Seq(
            PenaltyItem(
              dateRaised      = LocalDate.of(2014, 9, 1),
              descriptionCode = 2680,
              amount          = -800.00,
              periodStartDate = LocalDate.of(2014, 4, 1),
              periodEndDate   = LocalDate.of(2014, 6, 30)
            )
          )
        )

      case "XYZ00000000012" =>
        Penalties(
          periodStartDate    = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate      = Some(LocalDate.of(2014, 3, 11)),
          total              = -7600.00,
          totalRecords       = 4,
          items     = Seq(
            PenaltyItem(
              dateRaised      = LocalDate.of(2014, 9, 1),
              descriptionCode = 2680,
              amount          = -800.00,
              periodStartDate = LocalDate.of(2014, 4, 1),
              periodEndDate   = LocalDate.of(2014, 6, 30)
            ),
            PenaltyItem(
              dateRaised      = LocalDate.of(2014, 4, 1),
              descriptionCode = 2690,
              amount          = -400.00,
              periodStartDate = LocalDate.of(2014, 1, 1),
              periodEndDate   = LocalDate.of(2014, 3, 31)
            ),
            PenaltyItem(
              dateRaised      = LocalDate.of(2013, 9, 1),
              descriptionCode = 2680,
              amount          = -1400.00,
              periodStartDate = LocalDate.of(2013, 4, 1),
              periodEndDate   = LocalDate.of(2013, 6, 30)
            ),
            PenaltyItem(
              dateRaised      = LocalDate.of(2013, 9, 1),
              descriptionCode = 2690,
              amount          = -5000.00,
              periodStartDate = LocalDate.of(2013, 4, 1),
              periodEndDate   = LocalDate.of(2013, 6, 30)
            )
          )
        )
      case "XYZ00000000021" =>
        Penalties(
          periodStartDate    = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate      = Some(LocalDate.of(2014, 3, 11)),
          total              = -27000.00,
          totalRecords       = 3,
          items     = Seq(
            PenaltyItem(
              dateRaised      = LocalDate.of(2014, 9, 1),
              descriptionCode = 2680,
              amount          = -9000.00,
              periodStartDate = LocalDate.of(2014, 4, 1),
              periodEndDate   = LocalDate.of(2014, 6, 30)
            ),
            PenaltyItem(
              dateRaised      = LocalDate.of(2014, 4, 1),
              descriptionCode = 2690,
              amount          = -10000.00,
              periodStartDate = LocalDate.of(2014, 1, 1),
              periodEndDate   = LocalDate.of(2014, 3, 31)
            ),
            PenaltyItem(
              dateRaised      = LocalDate.of(2013, 9, 1),
              descriptionCode = 2680,
              amount          = -8000.00,
              periodStartDate = LocalDate.of(2013, 4, 1),
              periodEndDate   = LocalDate.of(2013, 6, 30)
            )
          )
        )
      case "XYZ99999999999" =>
        Penalties(
          periodStartDate    = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate      = Some(LocalDate.of(2014, 3, 11)),
          total              = 999.00,
          totalRecords       = 99,
          items              = Seq()
        )
      case "ERR00000000000" => throw new RuntimeException("Simulated downstream failure")
      case _ =>
        Penalties(
          periodStartDate    = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate      = Some(LocalDate.of(2014, 3, 11)),
          total              = -4000.00,
          totalRecords       = 2,
          items     = Seq(
            PenaltyItem(
              dateRaised      = LocalDate.of(2014, 9, 1),
              descriptionCode = 2680,
              amount          = -3500.00,
              periodStartDate = LocalDate.of(2014, 4, 1),
              periodEndDate   = LocalDate.of(2014, 6, 30)
            ),
            PenaltyItem(
              dateRaised      = LocalDate.of(2014, 4, 1),
              descriptionCode = 2690,
              amount          = -500.00,
              periodStartDate = LocalDate.of(2014, 1, 1),
              periodEndDate   = LocalDate.of(2014, 3, 31)
            )
          )

        )
    }

}
