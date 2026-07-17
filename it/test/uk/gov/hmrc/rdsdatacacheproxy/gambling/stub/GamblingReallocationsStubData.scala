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

import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{ReallocationItem, Reallocations, ReallocationsDetails, ReallocationsOut}

import java.time.LocalDate

object GamblingReallocationsStubData {
  def getReallocationsInData(regNumber: String, paginationStart: Int = 1, paginationMaxRows: Int = 10): Reallocations =
    regNumber match {
      case "XGM00003122200" =>
        Reallocations(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
          total           = Some(0.00),
          totalRecords    = Some(0),
          items           = Seq()
        )
      case "XYZ00000000001" =>
        Reallocations(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
          total           = Some(2500.00),
          totalRecords    = Some(2),
          items = Seq(
            ReallocationItem(
              dateProcessed = Some(LocalDate.of(2014, 8, 20)),
              amount        = Some(1500.00)
            ),
            ReallocationItem(
              dateProcessed = Some(LocalDate.of(2014, 3, 10)),
              amount        = Some(1000.00)
            ),
            ReallocationItem(
              dateProcessed = Some(LocalDate.of(2013, 10, 1)),
              amount        = Some(3000.00)
            )
          )
        )
      case "XYZ00000000010" =>
        Reallocations(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
          total           = Some(1100.00),
          totalRecords    = Some(1),
          items = Seq(
            ReallocationItem(
              dateProcessed = Some(LocalDate.of(2014, 4, 1)),
              amount        = Some(9500.00)
            )
          )
        )
      case "XYZ00000000012" =>
        Reallocations(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
          total           = Some(3500.00),
          totalRecords    = Some(4),
          items = Seq(
            ReallocationItem(
              dateProcessed = Some(LocalDate.of(2014, 4, 1)),
              amount        = Some(500.00)
            ),
            ReallocationItem(
              dateProcessed = Some(LocalDate.of(2014, 1, 1)),
              amount        = Some(8000.00)
            ),
            ReallocationItem(
              dateProcessed = Some(LocalDate.of(2013, 10, 1)),
              amount        = Some(7000.00)
            ),
            ReallocationItem(
              dateProcessed = Some(LocalDate.of(2013, 10, 1)),
              amount        = Some(5555.00)
            )
          )
        )
      case "XYZ00000000021" =>
        Reallocations(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
          total           = Some(44500.00),
          totalRecords    = Some(3),
          items = Seq(
            ReallocationItem(
              dateProcessed = Some(LocalDate.of(2014, 4, 1)),
              amount        = Some(9500.00)
            ),
            ReallocationItem(
              dateProcessed = Some(LocalDate.of(2014, 1, 1)),
              amount        = Some(8000.00)
            ),
            ReallocationItem(
              dateProcessed = Some(LocalDate.of(2013, 10, 1)),
              amount        = Some(7000.00)
            )
          )
        )
      case "XHM00003133333" =>
        Reallocations(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
          total           = Some(999.00),
          totalRecords    = Some(99),
          items           = Seq()
        )
      case "XXM33333066666" => throw new RuntimeException("Simulated downstream failure")
      case _ =>
        Reallocations(
          periodStartDate = Some(LocalDate.of(2023, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2024, 3, 11)),
          total           = Some(4500.00),
          totalRecords    = Some(1),
          items = Seq(
            ReallocationItem(
              dateProcessed = Some(LocalDate.of(2024, 4, 1)),
              amount        = Some(4500.00)
            )
          )
        )
    }

  def getReallocationsOutData(regNumber: String, paginationStart: Int = 1, paginationMaxRows: Int = 10): ReallocationsOut =
    regNumber match {
      case "XGM00003122200ZZZ" => ReallocationsOut.empty
      case "XYZ00000000001" =>
        ReallocationsOut(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
          total           = -5500.00,
          totalRecords    = 3,
          items = Seq(
            ReallocationsOut.Reallocation(
              dateProcessed = LocalDate.of(2014, 8, 20),
              amount        = -1500.00
            ),
            ReallocationsOut.Reallocation(
              dateProcessed = LocalDate.of(2014, 3, 10),
              amount        = -1000.00
            ),
            ReallocationsOut.Reallocation(
              dateProcessed = LocalDate.of(2013, 10, 1),
              amount        = -3000.00
            )
          )
        )
      case "XYZ00000000010" =>
        ReallocationsOut(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
          total           = -1100.00,
          totalRecords    = 1,
          items = Seq(
            ReallocationsOut.Reallocation(
              dateProcessed = LocalDate.of(2014, 4, 1),
              amount        = -1100.00
            )
          )
        )
      case "XYZ00000000012" =>
        ReallocationsOut(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
          total           = -21055.00,
          totalRecords    = 4,
          items = Seq(
            ReallocationsOut.Reallocation(
              dateProcessed = LocalDate.of(2014, 4, 1),
              amount        = -500.00
            ),
            ReallocationsOut.Reallocation(
              dateProcessed = LocalDate.of(2014, 1, 1),
              amount        = -8000.00
            ),
            ReallocationsOut.Reallocation(
              dateProcessed = LocalDate.of(2013, 10, 1),
              amount        = -7000.00
            ),
            ReallocationsOut.Reallocation(
              dateProcessed = LocalDate.of(2013, 10, 1),
              amount        = -5555.00
            )
          )
        )
      case "XYZ00000000021" =>
        ReallocationsOut(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
          total           = -24500.00,
          totalRecords    = 3,
          items = Seq(
            ReallocationsOut.Reallocation(
              dateProcessed = LocalDate.of(2014, 4, 1),
              amount        = -9500.00
            ),
            ReallocationsOut.Reallocation(
              dateProcessed = LocalDate.of(2014, 1, 1),
              amount        = -8000.00
            ),
            ReallocationsOut.Reallocation(
              dateProcessed = LocalDate.of(2013, 10, 1),
              amount        = -7000.00
            )
          )
        )
      case "XXM33333066666" => throw new RuntimeException("Simulated downstream failure")
      case _ =>
        ReallocationsOut(
          periodStartDate = Some(LocalDate.of(2023, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2024, 3, 11)),
          total           = -4500.00,
          totalRecords    = 1,
          items = Seq(
            ReallocationsOut.Reallocation(
              dateProcessed = LocalDate.of(2024, 4, 1),
              amount        = -4500.00
            )
          )
        )
    }

  def getReallocationsDetailData(regNumber: String): ReallocationsDetails =
    regNumber match {
      case "XGM00003122200" =>
        ReallocationsDetails(
          periodStartDate        = Option(LocalDate.of(2023, 3, 1)),
          periodEndDate          = Option(LocalDate.of(2024, 3, 11)),
          reallocationsInAmount  = 0.00,
          reallocationsOutAmount = 0.00,
          total                  = 0.00
        )
      case "XXM33333066666" => throw new RuntimeException("Simulated downstream failure")
      case _ =>
        ReallocationsDetails(
          periodStartDate        = Option(LocalDate.of(2023, 3, 1)),
          periodEndDate          = Option(LocalDate.of(2024, 3, 11)),
          reallocationsInAmount  = 30.00,
          reallocationsOutAmount = 50.00,
          total                  = -20.00
        )
    }
}
