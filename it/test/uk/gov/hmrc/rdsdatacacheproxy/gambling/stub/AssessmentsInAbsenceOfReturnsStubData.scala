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

import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{AssessmentsInAbsenceOfReturnsItem, AssessmentsInAbsenceOfReturns}

import java.time.LocalDate

object AssessmentsInAbsenceOfReturnsStubData {

  def getAssessmentsWithoutReturnData(regNumber: String, paginationStart: Int = 1, paginationMaxRows: Int = 10): AssessmentsInAbsenceOfReturns =
    regNumber match {
      case "XYZ00000000000" =>
        AssessmentsInAbsenceOfReturns(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
          total           = 0.00,
          totalRecords    = 0,
          items           = Seq()
        )
      case "XYZ00000000001" =>
        AssessmentsInAbsenceOfReturns(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
          total           = -24500.00,
          totalRecords    = 3,
          items = Seq(
            AssessmentsInAbsenceOfReturnsItem(
              dateRaised      = Some(LocalDate.of(2014, 4, 1)),
              periodStartDate = Some(LocalDate.of(2014, 4, 1)),
              periodEndDate   = Some(LocalDate.of(2014, 6, 30)),
              amount          = Some(-9500.00)
            ),
            AssessmentsInAbsenceOfReturnsItem(
              dateRaised      = Some(LocalDate.of(2014, 4, 2)),
              periodStartDate = Some(LocalDate.of(2014, 1, 1)),
              periodEndDate   = Some(LocalDate.of(2014, 3, 31)),
              amount          = Some(-8000.00)
            ),
            AssessmentsInAbsenceOfReturnsItem(
              dateRaised      = Some(LocalDate.of(2014, 4, 3)),
              periodStartDate = Some(LocalDate.of(2013, 10, 1)),
              periodEndDate   = Some(LocalDate.of(2013, 12, 31)),
              amount          = Some(-7000.00)
            )
          )
        )
      case "XYZ00000000010" =>
        AssessmentsInAbsenceOfReturns(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
          total           = -1000.00,
          totalRecords    = 1,
          items = Seq(
            AssessmentsInAbsenceOfReturnsItem(
              dateRaised      = Some(LocalDate.of(2014, 4, 1)),
              periodStartDate = Some(LocalDate.of(2014, 4, 1)),
              periodEndDate   = Some(LocalDate.of(2014, 6, 30)),
              amount          = Some(-9500.00)
            )
          )
        )
      case "XYZ00000000012" =>
        AssessmentsInAbsenceOfReturns(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
          total           = -34500.00,
          totalRecords    = 4,
          items = Seq(
            AssessmentsInAbsenceOfReturnsItem(
              dateRaised      = Some(LocalDate.of(2014, 4, 1)),
              periodStartDate = Some(LocalDate.of(2014, 4, 1)),
              periodEndDate   = Some(LocalDate.of(2014, 6, 30)),
              amount          = Some(-9500.00)
            ),
            AssessmentsInAbsenceOfReturnsItem(
              dateRaised      = Some(LocalDate.of(2014, 4, 2)),
              periodStartDate = Some(LocalDate.of(2014, 1, 1)),
              periodEndDate   = Some(LocalDate.of(2014, 3, 31)),
              amount          = Some(-8000.00)
            ),
            AssessmentsInAbsenceOfReturnsItem(
              dateRaised      = Some(LocalDate.of(2014, 4, 3)),
              periodStartDate = Some(LocalDate.of(2013, 10, 1)),
              periodEndDate   = Some(LocalDate.of(2013, 12, 31)),
              amount          = Some(-7000.00)
            ),
            AssessmentsInAbsenceOfReturnsItem(
              dateRaised      = Some(LocalDate.of(2014, 4, 4)),
              periodStartDate = Some(LocalDate.of(2013, 10, 1)),
              periodEndDate   = Some(LocalDate.of(2013, 12, 31)),
              amount          = Some(5555.00)
            )
          )
        )
      case "XYZ00000000021" =>
        AssessmentsInAbsenceOfReturns(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
          total           = -44500.00,
          totalRecords    = 3,
          items = Seq(
            AssessmentsInAbsenceOfReturnsItem(
              dateRaised      = Some(LocalDate.of(2014, 4, 1)),
              periodStartDate = Some(LocalDate.of(2014, 4, 1)),
              periodEndDate   = Some(LocalDate.of(2014, 6, 30)),
              amount          = Some(-9500.00)
            ),
            AssessmentsInAbsenceOfReturnsItem(
              dateRaised      = Some(LocalDate.of(2014, 4, 2)),
              periodStartDate = Some(LocalDate.of(2014, 1, 1)),
              periodEndDate   = Some(LocalDate.of(2014, 3, 31)),
              amount          = Some(-8000.00)
            ),
            AssessmentsInAbsenceOfReturnsItem(
              dateRaised      = Some(LocalDate.of(2014, 4, 3)),
              periodStartDate = Some(LocalDate.of(2013, 10, 1)),
              periodEndDate   = Some(LocalDate.of(2013, 12, 31)),
              amount          = Some(-7000.00)
            )
          )
        )
      case "XYZ99999999999" =>
        AssessmentsInAbsenceOfReturns(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
          total           = 999.00,
          totalRecords    = 99,
          items           = Seq()
        )
      case "ERR00000000000" => throw new RuntimeException("Simulated downstream failure")
      case _ =>
        AssessmentsInAbsenceOfReturns(
          periodStartDate = Some(LocalDate.of(2023, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2024, 3, 11)),
          total           = -4500.00,
          totalRecords    = 1,
          items = Seq(
            AssessmentsInAbsenceOfReturnsItem(
              dateRaised      = Some(LocalDate.of(2014, 4, 1)),
              periodStartDate = Some(LocalDate.of(2024, 4, 1)),
              periodEndDate   = Some(LocalDate.of(2024, 6, 30)),
              amount          = Some(-4500.00)
            )
          )
        )
    }
}
