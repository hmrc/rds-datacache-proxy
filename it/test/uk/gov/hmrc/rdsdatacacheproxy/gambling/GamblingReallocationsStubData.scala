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

package uk.gov.hmrc.rdsdatacacheproxy.gambling

import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{ReallocationsIn, ReallocationsInAmount}

import java.time.LocalDate

object GamblingReallocationsStubData {
  def getReallocationsInData(regNumber: String, paginationStart: Int = 1, paginationMaxRows: Int = 10): ReallocationsIn =
    regNumber match {
      case "XYZ00000000000" =>
        ReallocationsIn(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate = Some(LocalDate.of(2014, 3, 11)),
          total = Some(0.00),
          totalPeriodRecords = Some(0),
          reallocationsInAmount = Seq()
        )
      case "XYZ00000000001" =>
        ReallocationsIn(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate = Some(LocalDate.of(2014, 3, 11)),
          total = Some(2500.00),
          totalPeriodRecords = Some(2),
          reallocationsInAmount = Seq(
            ReallocationsInAmount(
              dateProcessed = Some(LocalDate.of(2014, 8, 20)),
              amount = Some(1500.00)
            ),
            ReallocationsInAmount(
              dateProcessed = Some(LocalDate.of(2014, 3, 10)),
              amount = Some(1000.00)
            ),
            ReallocationsInAmount(
              dateProcessed = Some(LocalDate.of(2013, 10, 1)),
              amount = Some(3000.00)
            )
          )
        )
      case "XYZ00000000010" =>
        ReallocationsIn(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate = Some(LocalDate.of(2014, 3, 11)),
          total = Some(1100.00),
          totalPeriodRecords = Some(1),
          reallocationsInAmount = Seq(
            ReallocationsInAmount(
              dateProcessed = Some(LocalDate.of(2014, 4, 1)),
              amount = Some(9500.00)
            )
          )
        )
      case "XYZ00000000012" =>
        ReallocationsIn(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate = Some(LocalDate.of(2014, 3, 11)),
          total = Some(3500.00),
          totalPeriodRecords = Some(4),
          reallocationsInAmount = Seq(
            ReallocationsInAmount(
              dateProcessed = Some(LocalDate.of(2014, 4, 1)),
              amount = Some(500.00)
            ),
            ReallocationsInAmount(
              dateProcessed = Some(LocalDate.of(2014, 1, 1)),
              amount = Some(8000.00)
            ),
            ReallocationsInAmount(
              dateProcessed = Some(LocalDate.of(2013, 10, 1)),
              amount = Some(7000.00)
            ),
            ReallocationsInAmount(
              dateProcessed = Some(LocalDate.of(2013, 10, 1)),
              amount = Some(5555.00)
            )
          )
        )
      case "XYZ00000000021" =>
        ReallocationsIn(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate = Some(LocalDate.of(2014, 3, 11)),
          total = Some(44500.00),
          totalPeriodRecords = Some(3),
          reallocationsInAmount = Seq(
            ReallocationsInAmount(
              dateProcessed = Some(LocalDate.of(2014, 4, 1)),
              amount = Some(9500.00)
            ),
            ReallocationsInAmount(
              dateProcessed = Some(LocalDate.of(2014, 1, 1)),
              amount = Some(8000.00)
            ),
            ReallocationsInAmount(
              dateProcessed = Some(LocalDate.of(2013, 10, 1)),
              amount = Some(7000.00)
            )
          )
        )
      case "XYZ99999999999" =>
        ReallocationsIn(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate = Some(LocalDate.of(2014, 3, 11)),
          total = Some(999.00),
          totalPeriodRecords = Some(99),
          reallocationsInAmount = Seq()
        )
      case "ERR00000000000" => throw new RuntimeException("Simulated downstream failure")
      case _ =>
        ReallocationsIn(
          periodStartDate = Some(LocalDate.of(2023, 3, 1)),
          periodEndDate = Some(LocalDate.of(2024, 3, 11)),
          total = Some(4500.00),
          totalPeriodRecords = Some(1),
          reallocationsInAmount = Seq(
            ReallocationsInAmount(
              dateProcessed = Some(LocalDate.of(2024, 4, 1)),
              amount = Some(4500.00)
            )
          )
        )
    }

}
