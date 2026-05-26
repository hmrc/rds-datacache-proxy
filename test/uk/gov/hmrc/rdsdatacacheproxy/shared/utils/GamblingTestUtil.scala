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

package uk.gov.hmrc.rdsdatacacheproxy.shared.utils

import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.*

import java.time.LocalDate

object GamblingTestUtil {

  final val validRegime = "GbD"

  val validResponseReturnsSubmitted = ReturnsSubmitted(
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

  val validResponseReturnsSubmittedSmall = ReturnsSubmitted(
    periodStartDate    = Some(LocalDate.of(2016, 2, 29)),
    periodEndDate      = Some(LocalDate.of(2017, 6, 15)),
    total              = Some(-301.56),
    totalPeriodRecords = Some(1),
    amountDeclared = Seq(
      AmountDeclared(descriptionCode = Some(4455),
                     periodStartDate = Some(LocalDate.of(2016, 3, 9)),
                     periodEndDate   = Some(LocalDate.of(2016, 5, 20)),
                     amount          = Some(-943.21)
                    )
    )
  )

  val validResponseReallocationsIn = Reallocations(
    periodStartDate = Some(LocalDate.of(2013, 3, 1)),
    periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
    total           = Some(24500.00),
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

  val validResponseReallocationsInSmall = Reallocations(
    periodStartDate = Some(LocalDate.of(2016, 2, 29)),
    periodEndDate   = Some(LocalDate.of(2017, 6, 15)),
    total           = Some(301.56),
    totalRecords    = Some(1),
    items = Seq(
      ReallocationItem(
        dateProcessed = Some(LocalDate.of(2016, 3, 9)),
        amount        = Some(943.21)
      )
    )
  )

  val validResponseOtherAssessments = Assessments(
    periodStartDate = Some(LocalDate.of(2013, 3, 1)),
    periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
    total           = Some(-24500.00),
    totalRecords    = Some(3),
    items = Seq(
      AssessmentItem(
        dateRaised      = Some(LocalDate.of(2014, 1, 1)),
        periodStartDate = Some(LocalDate.of(2014, 4, 1)),
        periodEndDate   = Some(LocalDate.of(2014, 6, 30)),
        amount          = Some(-9500.00)
      ),
      AssessmentItem(
        dateRaised      = Some(LocalDate.of(2014, 1, 2)),
        periodStartDate = Some(LocalDate.of(2014, 1, 1)),
        periodEndDate   = Some(LocalDate.of(2014, 3, 31)),
        amount          = Some(-8000.00)
      ),
      AssessmentItem(
        dateRaised      = Some(LocalDate.of(2014, 1, 3)),
        periodStartDate = Some(LocalDate.of(2013, 10, 1)),
        periodEndDate   = Some(LocalDate.of(2013, 12, 31)),
        amount          = Some(-7000.00)
      )
    )
  )

  val validResponseOtherAssessmentsSmall = Assessments(
    periodStartDate = Some(LocalDate.of(2016, 2, 29)),
    periodEndDate   = Some(LocalDate.of(2017, 6, 15)),
    total           = Some(-301.56),
    totalRecords    = Some(1),
    items = Seq(
      AssessmentItem(dateRaised      = Some(LocalDate.of(2016, 1, 1)),
                     periodStartDate = Some(LocalDate.of(2016, 3, 9)),
                     periodEndDate   = Some(LocalDate.of(2016, 5, 20)),
                     amount          = Some(-943.21)
                    )
    )
  )

  val validResponsePenalties = Penalties(
    periodStartDate = Some(LocalDate.of(2013, 3, 1)),
    periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
    total           = BigDecimal(-24500.00),
    totalRecords    = 3,
    items = Seq(
      PenaltyItem(
        dateRaised      = LocalDate.of(2014, 1, 1),
        descriptionCode = 2680,
        amount          = BigDecimal(-9500.00),
        periodStartDate = LocalDate.of(2014, 4, 1),
        periodEndDate   = LocalDate.of(2014, 6, 30)
      ),
      PenaltyItem(
        dateRaised      = LocalDate.of(2014, 1, 2),
        descriptionCode = 2690,
        amount          = BigDecimal(-8000.00),
        periodStartDate = LocalDate.of(2014, 1, 1),
        periodEndDate   = LocalDate.of(2014, 3, 31)
      ),
      PenaltyItem(
        dateRaised      = LocalDate.of(2014, 1, 3),
        descriptionCode = 2680,
        amount          = BigDecimal(-7000.00),
        periodStartDate = LocalDate.of(2013, 10, 1),
        periodEndDate   = LocalDate.of(2013, 12, 31)
      )
    )
  )

  val validResponsePenaltiesSmall = Penalties(
    periodStartDate = Some(LocalDate.of(2013, 3, 1)),
    periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
    total           = -2500.00,
    totalRecords    = 1,
    items = Seq(
      PenaltyItem(
        dateRaised      = LocalDate.of(2014, 9, 1),
        descriptionCode = 2680,
        amount          = -800.00,
        periodStartDate = LocalDate.of(2014, 4, 1),
        periodEndDate   = LocalDate.of(2014, 6, 30)
      )
    )
  )

  val validResponseAssessmentsInAbsence = AssessmentsInAbsence(
    periodStartDate = Some(LocalDate.of(2013, 3, 1)),
    periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
    total           = -24500.00,
    totalRecords    = 3,
    items = Seq(
      AssessmentsInAbsenceItem(
        dateRaised      = Some(LocalDate.of(2014, 1, 1)),
        periodStartDate = Some(LocalDate.of(2014, 4, 1)),
        periodEndDate   = Some(LocalDate.of(2014, 6, 30)),
        amount          = Some(-9500.00)
      ),
      AssessmentsInAbsenceItem(
        dateRaised      = Some(LocalDate.of(2014, 1, 2)),
        periodStartDate = Some(LocalDate.of(2014, 1, 1)),
        periodEndDate   = Some(LocalDate.of(2014, 3, 31)),
        amount          = Some(-8000.00)
      ),
      AssessmentsInAbsenceItem(
        dateRaised      = Some(LocalDate.of(2014, 1, 3)),
        periodStartDate = Some(LocalDate.of(2013, 10, 1)),
        periodEndDate   = Some(LocalDate.of(2013, 12, 31)),
        amount          = Some(-7000.00)
      )
    )
  )

  val validResponseAssessmentsInAbsenceSmall = AssessmentsInAbsence(
    periodStartDate = Some(LocalDate.of(2016, 2, 29)),
    periodEndDate   = Some(LocalDate.of(2017, 6, 15)),
    total           = -301.56,
    totalRecords    = 1,
    items = Seq(
      AssessmentsInAbsenceItem(dateRaised      = Some(LocalDate.of(2016, 1, 1)),
                               periodStartDate = Some(LocalDate.of(2016, 3, 9)),
                               periodEndDate   = Some(LocalDate.of(2016, 5, 20)),
                               amount          = Some(-943.21)
                              )
    )
  )
}
