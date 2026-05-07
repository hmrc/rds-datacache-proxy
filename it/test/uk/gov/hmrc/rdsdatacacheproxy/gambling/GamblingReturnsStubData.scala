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

import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{AmountDeclared, Assessments, OtherAssessments, ReturnsSubmitted}

import java.time.LocalDate

object GamblingReturnsStubData {
  def getReturnsSubmittedData(regNumber: String, paginationStart: Int = 1, paginationMaxRows: Int = 10): ReturnsSubmitted =
    regNumber match {
      case "XYZ00000000000" =>
        ReturnsSubmitted(
          periodStartDate    = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate      = Some(LocalDate.of(2014, 3, 11)),
          total              = Some(0.00),
          totalPeriodRecords = Some(0),
          amountDeclared     = Seq()
        )
      case "XYZ00000000001" =>
        ReturnsSubmitted(
          periodStartDate    = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate      = Some(LocalDate.of(2014, 3, 11)),
          total              = Some(-24500.00),
          totalPeriodRecords = Some(3),
          amountDeclared = Seq(
            AmountDeclared(descriptionCode = Some(2650),
                           periodStartDate = Some(LocalDate.of(2014, 4, 1)),
                           periodEndDate   = Some(LocalDate.of(2014, 6, 30)),
                           amount          = Some(-9500.00)
                          ),
            AmountDeclared(descriptionCode = Some(2650),
                           periodStartDate = Some(LocalDate.of(2014, 1, 1)),
                           periodEndDate   = Some(LocalDate.of(2014, 3, 31)),
                           amount          = Some(-8000.00)
                          ),
            AmountDeclared(descriptionCode = Some(2650),
                           periodStartDate = Some(LocalDate.of(2013, 10, 1)),
                           periodEndDate   = Some(LocalDate.of(2013, 12, 31)),
                           amount          = Some(-7000.00)
                          )
          )
        )
      case "XYZ00000000010" =>
        ReturnsSubmitted(
          periodStartDate    = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate      = Some(LocalDate.of(2014, 3, 11)),
          total              = Some(-1000.00),
          totalPeriodRecords = Some(1),
          amountDeclared = Seq(
            AmountDeclared(descriptionCode = Some(2650),
                           periodStartDate = Some(LocalDate.of(2014, 4, 1)),
                           periodEndDate   = Some(LocalDate.of(2014, 6, 30)),
                           amount          = Some(-9500.00)
                          )
          )
        )
      case "XYZ00000000012" =>
        ReturnsSubmitted(
          periodStartDate    = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate      = Some(LocalDate.of(2014, 3, 11)),
          total              = Some(-34500.00),
          totalPeriodRecords = Some(4),
          amountDeclared = Seq(
            AmountDeclared(descriptionCode = Some(2650),
                           periodStartDate = Some(LocalDate.of(2014, 4, 1)),
                           periodEndDate   = Some(LocalDate.of(2014, 6, 30)),
                           amount          = Some(-9500.00)
                          ),
            AmountDeclared(descriptionCode = Some(2650),
                           periodStartDate = Some(LocalDate.of(2014, 1, 1)),
                           periodEndDate   = Some(LocalDate.of(2014, 3, 31)),
                           amount          = Some(-8000.00)
                          ),
            AmountDeclared(descriptionCode = Some(2650),
                           periodStartDate = Some(LocalDate.of(2013, 10, 1)),
                           periodEndDate   = Some(LocalDate.of(2013, 12, 31)),
                           amount          = Some(-7000.00)
                          ),
            AmountDeclared(descriptionCode = Some(3650),
                           periodStartDate = Some(LocalDate.of(2013, 10, 1)),
                           periodEndDate   = Some(LocalDate.of(2013, 12, 31)),
                           amount          = Some(5555.00)
                          )
          )
        )
      case "XYZ00000000021" =>
        ReturnsSubmitted(
          periodStartDate    = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate      = Some(LocalDate.of(2014, 3, 11)),
          total              = Some(-44500.00),
          totalPeriodRecords = Some(3),
          amountDeclared = Seq(
            AmountDeclared(descriptionCode = Some(2650),
                           periodStartDate = Some(LocalDate.of(2014, 4, 1)),
                           periodEndDate   = Some(LocalDate.of(2014, 6, 30)),
                           amount          = Some(-9500.00)
                          ),
            AmountDeclared(descriptionCode = Some(2650),
                           periodStartDate = Some(LocalDate.of(2014, 1, 1)),
                           periodEndDate   = Some(LocalDate.of(2014, 3, 31)),
                           amount          = Some(-8000.00)
                          ),
            AmountDeclared(descriptionCode = Some(2650),
                           periodStartDate = Some(LocalDate.of(2013, 10, 1)),
                           periodEndDate   = Some(LocalDate.of(2013, 12, 31)),
                           amount          = Some(-7000.00)
                          )
          )
        )
      case "XYZ99999999999" =>
        ReturnsSubmitted(
          periodStartDate    = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate      = Some(LocalDate.of(2014, 3, 11)),
          total              = Some(999.00),
          totalPeriodRecords = Some(99),
          amountDeclared     = Seq()
        )
      case "ERR00000000000" => throw new RuntimeException("Simulated downstream failure")
      case _ =>
        ReturnsSubmitted(
          periodStartDate    = Some(LocalDate.of(2023, 3, 1)),
          periodEndDate      = Some(LocalDate.of(2024, 3, 11)),
          total              = Some(-4500.00),
          totalPeriodRecords = Some(1),
          amountDeclared = Seq(
            AmountDeclared(descriptionCode = Some(2650),
                           periodStartDate = Some(LocalDate.of(2024, 4, 1)),
                           periodEndDate   = Some(LocalDate.of(2024, 6, 30)),
                           amount          = Some(-4500.00)
                          )
          )
        )
    }

  def getOtherAssessmentsData(regNumber: String, paginationStart: Int = 1, paginationMaxRows: Int = 10): OtherAssessments =
    regNumber match {
      case "XYZ00000000000" =>
        OtherAssessments(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate = Some(LocalDate.of(2014, 3, 11)),
          total = Some(0.00),
          totalPeriodRecords = Some(0),
          assessments = Seq()
        )
      case "XYZ00000000001" =>
        OtherAssessments(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate = Some(LocalDate.of(2014, 3, 11)),
          total = Some(-24500.00),
          totalPeriodRecords = Some(3),
          assessments = Seq(
            Assessments(dateRaised = Some(LocalDate.of(2014, 4, 1)),
              periodStartDate = Some(LocalDate.of(2014, 4, 1)),
              periodEndDate = Some(LocalDate.of(2014, 6, 30)),
              amount = Some(-9500.00)
            ),
            Assessments(dateRaised = Some(LocalDate.of(2014, 4, 2)),
              periodStartDate = Some(LocalDate.of(2014, 1, 1)),
              periodEndDate = Some(LocalDate.of(2014, 3, 31)),
              amount = Some(-8000.00)
            ),
            Assessments(dateRaised = Some(LocalDate.of(2014, 4, 3)),
              periodStartDate = Some(LocalDate.of(2013, 10, 1)),
              periodEndDate = Some(LocalDate.of(2013, 12, 31)),
              amount = Some(-7000.00)
            )
          )
        )
      case "XYZ00000000010" =>
        OtherAssessments(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate = Some(LocalDate.of(2014, 3, 11)),
          total = Some(-1000.00),
          totalPeriodRecords = Some(1),
          assessments = Seq(
            Assessments(dateRaised = Some(LocalDate.of(2014, 4, 1)),
              periodStartDate = Some(LocalDate.of(2014, 4, 1)),
              periodEndDate = Some(LocalDate.of(2014, 6, 30)),
              amount = Some(-9500.00)
            )
          )
        )
      case "XYZ00000000012" =>
        OtherAssessments(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate = Some(LocalDate.of(2014, 3, 11)),
          total = Some(-34500.00),
          totalPeriodRecords = Some(4),
          assessments = Seq(
            Assessments(dateRaised = Some(LocalDate.of(2014, 4, 1)),
              periodStartDate = Some(LocalDate.of(2014, 4, 1)),
              periodEndDate = Some(LocalDate.of(2014, 6, 30)),
              amount = Some(-9500.00)
            ),
            Assessments(dateRaised = Some(LocalDate.of(2014, 4, 2)),
              periodStartDate = Some(LocalDate.of(2014, 1, 1)),
              periodEndDate = Some(LocalDate.of(2014, 3, 31)),
              amount = Some(-8000.00)
            ),
            Assessments(dateRaised = Some(LocalDate.of(2014, 4, 3)),
              periodStartDate = Some(LocalDate.of(2013, 10, 1)),
              periodEndDate = Some(LocalDate.of(2013, 12, 31)),
              amount = Some(-7000.00)
            ),
            Assessments(dateRaised = Some(LocalDate.of(2014, 4, 4)),
              periodStartDate = Some(LocalDate.of(2013, 10, 1)),
              periodEndDate = Some(LocalDate.of(2013, 12, 31)),
              amount = Some(5555.00)
            )
          )
        )
      case "XYZ00000000021" =>
        OtherAssessments(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate = Some(LocalDate.of(2014, 3, 11)),
          total = Some(-44500.00),
          totalPeriodRecords = Some(3),
          assessments = Seq(
            Assessments(dateRaised = Some(LocalDate.of(2014, 4, 1)),
              periodStartDate = Some(LocalDate.of(2014, 4, 1)),
              periodEndDate = Some(LocalDate.of(2014, 6, 30)),
              amount = Some(-9500.00)
            ),
            Assessments(dateRaised = Some(LocalDate.of(2014, 4, 2)),
              periodStartDate = Some(LocalDate.of(2014, 1, 1)),
              periodEndDate = Some(LocalDate.of(2014, 3, 31)),
              amount = Some(-8000.00)
            ),
            Assessments(dateRaised = Some(LocalDate.of(2014, 4, 3)),
              periodStartDate = Some(LocalDate.of(2013, 10, 1)),
              periodEndDate = Some(LocalDate.of(2013, 12, 31)),
              amount = Some(-7000.00)
            )
          )
        )
      case "XYZ99999999999" =>
        OtherAssessments(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate = Some(LocalDate.of(2014, 3, 11)),
          total = Some(999.00),
          totalPeriodRecords = Some(99),
          assessments = Seq()
        )
      case "ERR00000000000" => throw new RuntimeException("Simulated downstream failure")
      case _ =>
        OtherAssessments(
          periodStartDate = Some(LocalDate.of(2023, 3, 1)),
          periodEndDate = Some(LocalDate.of(2024, 3, 11)),
          total = Some(-4500.00),
          totalPeriodRecords = Some(1),
          assessments = Seq(
            Assessments(dateRaised = Some(LocalDate.of(2014, 4, 1)),
              periodStartDate = Some(LocalDate.of(2024, 4, 1)),
              periodEndDate = Some(LocalDate.of(2024, 6, 30)),
              amount = Some(-4500.00)
            )
          )
        )
    }
}
