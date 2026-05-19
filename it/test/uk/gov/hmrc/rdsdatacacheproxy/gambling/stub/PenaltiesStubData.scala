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

import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{Penalties, PenaltiesItem}

import java.time.LocalDate

object PenaltiesStubData {
  def getPenaltiesData(regNumber: String, paginationStart: Int = 1, paginationMaxRows: Int = 10): Penalties =
    regNumber match {
      case "XYZ00000000000" =>
        Penalties(
          periodStartDate    = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate      = Some(LocalDate.of(2014, 3, 11)),
          total              = Some(0.00),
          totalRecords       = Some(0),
          items              = Seq()
        )
      case "XYZ00000000001" =>
        Penalties(
          periodStartDate    = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate      = Some(LocalDate.of(2014, 3, 11)),
          total              = Some(-1200.00),
          totalRecords       = Some(3),
          items     = Seq(
            PenaltiesItem(
              dateRaised      = Some(LocalDate.of(2014, 9, 1)),
              descriptionCode = Some(2680),
              amount          = Some(-800.00),
              periodStartDate = Some(LocalDate.of(2014, 4, 1)),
              periodEndDate   = Some(LocalDate.of(2014, 6, 30))
            ),
            PenaltiesItem(
              dateRaised      = Some(LocalDate.of(2014, 4, 1)),
              descriptionCode = Some(2690),
              amount          = Some(-400.00),
              periodStartDate = Some(LocalDate.of(2014, 1, 1)),
              periodEndDate   = Some(LocalDate.of(2014, 3, 31))
            ),
            PenaltiesItem(
              dateRaised      = Some(LocalDate.of(2013, 9, 1)),
              descriptionCode = Some(2680),
              amount          = Some(-1400.00),
              periodStartDate = Some(LocalDate.of(2013, 4, 1)),
              periodEndDate   = Some(LocalDate.of(2013, 6, 30))
            )
          )
        )
      case "XYZ00000000010" =>
        Penalties(
          periodStartDate    = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate      = Some(LocalDate.of(2014, 3, 11)),
          total              = Some(-2500.00),
          totalRecords       = Some(1),
          items     = Seq(
            PenaltiesItem(
              dateRaised      = Some(LocalDate.of(2014, 9, 1)),
              descriptionCode = Some(2680),
              amount          = Some(-800.00),
              periodStartDate = Some(LocalDate.of(2014, 4, 1)),
              periodEndDate   = Some(LocalDate.of(2014, 6, 30))
            )
          )
        )

      case "XYZ00000000012" =>
        Penalties(
          periodStartDate    = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate      = Some(LocalDate.of(2014, 3, 11)),
          total              = Some(-7600.00),
          totalRecords       = Some(4),
          items     = Seq(
            PenaltiesItem(
              dateRaised      = Some(LocalDate.of(2014, 9, 1)),
              descriptionCode = Some(2680),
              amount          = Some(-800.00),
              periodStartDate = Some(LocalDate.of(2014, 4, 1)),
              periodEndDate   = Some(LocalDate.of(2014, 6, 30))
            ),
            PenaltiesItem(
              dateRaised      = Some(LocalDate.of(2014, 4, 1)),
              descriptionCode = Some(2690),
              amount          = Some(-400.00),
              periodStartDate = Some(LocalDate.of(2014, 1, 1)),
              periodEndDate   = Some(LocalDate.of(2014, 3, 31))
            ),
            PenaltiesItem(
              dateRaised      = Some(LocalDate.of(2013, 9, 1)),
              descriptionCode = Some(2680),
              amount          = Some(-1400.00),
              periodStartDate = Some(LocalDate.of(2013, 4, 1)),
              periodEndDate   = Some(LocalDate.of(2013, 6, 30))
            ),
            PenaltiesItem(
              dateRaised      = Some(LocalDate.of(2013, 9, 1)),
              descriptionCode = Some(2690),
              amount          = Some(-5000.00),
              periodStartDate = Some(LocalDate.of(2013, 4, 1)),
              periodEndDate   = Some(LocalDate.of(2013, 6, 30))
            )
          )
        )
      case "XYZ00000000021" =>
        Penalties(
          periodStartDate    = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate      = Some(LocalDate.of(2014, 3, 11)),
          total              = Some(-27000.00),
          totalRecords       = Some(3),
          items     = Seq(
            PenaltiesItem(
              dateRaised      = Some(LocalDate.of(2014, 9, 1)),
              descriptionCode = Some(2680),
              amount          = Some(-9000.00),
              periodStartDate = Some(LocalDate.of(2014, 4, 1)),
              periodEndDate   = Some(LocalDate.of(2014, 6, 30))
            ),
            PenaltiesItem(
              dateRaised      = Some(LocalDate.of(2014, 4, 1)),
              descriptionCode = Some(2690),
              amount          = Some(-10000.00),
              periodStartDate = Some(LocalDate.of(2014, 1, 1)),
              periodEndDate   = Some(LocalDate.of(2014, 3, 31))
            ),
            PenaltiesItem(
              dateRaised      = Some(LocalDate.of(2013, 9, 1)),
              descriptionCode = Some(2680),
              amount          = Some(-8000.00),
              periodStartDate = Some(LocalDate.of(2013, 4, 1)),
              periodEndDate   = Some(LocalDate.of(2013, 6, 30))
            )
          )
        )
      case "XYZ99999999999" =>
        Penalties(
          periodStartDate    = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate      = Some(LocalDate.of(2014, 3, 11)),
          total              = Some(999.00),
          totalRecords       = Some(99),
          items              = Seq()
        )
      case "ERR00000000000" => throw new RuntimeException("Simulated downstream failure")
      case _ =>
        Penalties(
          periodStartDate    = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate      = Some(LocalDate.of(2014, 3, 11)),
          total              = Some(-4000.00),
          totalRecords       = Some(2),
          items     = Seq(
            PenaltiesItem(
              dateRaised      = Some(LocalDate.of(2014, 9, 1)),
              descriptionCode = Some(2680),
              amount          = Some(-3500.00),
              periodStartDate = Some(LocalDate.of(2014, 4, 1)),
              periodEndDate   = Some(LocalDate.of(2014, 6, 30))
            ),
            PenaltiesItem(
              dateRaised      = Some(LocalDate.of(2014, 4, 1)),
              descriptionCode = Some(2690),
              amount          = Some(-500.00),
              periodStartDate = Some(LocalDate.of(2014, 1, 1)),
              periodEndDate   = Some(LocalDate.of(2014, 3, 31))
            )
          )

        )
    }

}
