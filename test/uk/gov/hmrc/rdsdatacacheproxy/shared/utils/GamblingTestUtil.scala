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

  val validResponseAssessments = Assessments(
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

  val validResponseAssessmentsSmall = Assessments(
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

  val validResponseRepaymentsSummary = RepaymentsSummary(
    periodStartDate                = Some(LocalDate.of(2013, 3, 1)),
    periodEndDate                  = Some(LocalDate.of(2014, 3, 11)),
    actualRepaymentsAmount         = BigDecimal(71.84),
    repaymentsInterestRepaidAmount = BigDecimal(-35.76),
    total                          = BigDecimal(36.08)
  )

  val validResponseInterestOverview = InterestOverview(
    periodStartDate         = Some(LocalDate.of(2013, 3, 1)),
    periodEndDate           = Some(LocalDate.of(2014, 3, 11)),
    interestAmount          = BigDecimal(-81.84),
    interestAccruingAmount  = BigDecimal(-25.76),
    repaymentInterestAmount = BigDecimal(41.23),
    total                   = BigDecimal(66.37)
  )

  val validResponsePayments = Payments(
    periodStartDate = Some(LocalDate.of(2013, 1, 1)),
    periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
    total           = BigDecimal(7500.00),
    totalRecords    = 3,
    items = Seq(
      PaymentItem(
        transactionDate = LocalDate.of(2014, 10, 1),
        descriptionCode = "E",
        amount          = 3000.00
      ),
      PaymentItem(
        transactionDate = LocalDate.of(2014, 7, 15),
        descriptionCode = "E",
        amount          = 5000.00
      ),
      PaymentItem(
        transactionDate = LocalDate.of(2013, 6, 1),
        descriptionCode = "C",
        amount          = -500.00
      )
    )
  )

  val validResponsePaymentsSmall = Payments(
    periodStartDate = Some(LocalDate.of(2013, 1, 1)),
    periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
    total           = BigDecimal(3000.00),
    totalRecords    = 1,
    items = Seq(
      PaymentItem(
        transactionDate = LocalDate.of(2014, 10, 1),
        descriptionCode = "E",
        amount          = 3000.00
      )
    )
  )

  val validResponseActualRepayments = ActualRepayments(
    periodStartDate = Some(LocalDate.of(2013, 1, 1)),
    periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
    total           = BigDecimal(-3250.00),
    totalRecords    = 2,
    items = Seq(
      ActualRepaymentItem(transactionDate = LocalDate.of(2014, 9, 15), amount = BigDecimal(-1500.00)),
      ActualRepaymentItem(transactionDate = LocalDate.of(2014, 6, 30), amount = BigDecimal(-1750.00))
    )
  )

  val validResponseRepaymentInterestRepaid = RepaymentInterestRepaid(
    periodStartDate = Some(LocalDate.of(2013, 1, 1)),
    periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
    total           = BigDecimal(7500.00),
    totalRecords    = 3,
    items = Seq(
      RepaymentInterestRepaidItem(
        transactionDate = LocalDate.of(2014, 10, 1),
        amount          = 3000.00
      ),
      RepaymentInterestRepaidItem(
        transactionDate = LocalDate.of(2014, 7, 15),
        amount          = 5000.00
      ),
      RepaymentInterestRepaidItem(
        transactionDate = LocalDate.of(2014, 7, 22),
        amount          = -500.00
      )
    )
  )

  val validResponseStatementOverview = StatementOverview(
    gtrPeriodStartDate = Some(LocalDate.of(2013, 1, 1)),
    gtrPeriodEndDate   = Some(LocalDate.of(2014, 11, 3)),
    total              = BigDecimal("-15562.47"),
    balance            = BigDecimal("-500.00"),
    amountDeclared     = BigDecimal("-24500.00"),
    assessments        = BigDecimal("-4500.00"),
    penalties          = BigDecimal("-1200.00"),
    adjustments        = BigDecimal("-250.00"),
    reallocations      = BigDecimal("-1500.00"),
    otherAssessments   = BigDecimal("-3500.00"),
    interest           = BigDecimal("-1624.97"),
    payments           = BigDecimal("22012.50"),
    repayments         = None
  )

  val validResponseInterestAccruingDrilldown = InterestAccruingDrilldown(
    periodStartDate = Some(LocalDate.of(2013, 1, 1)),
    periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
    total           = BigDecimal(1250.50),
    totalRecords    = 1,
    descriptionCode = Some(42),
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

  val validResponseInterestAccruingDrilldownEmpty = InterestAccruingDrilldown(
    periodStartDate = Some(LocalDate.of(2013, 1, 1)),
    periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
    total           = BigDecimal(0.00),
    totalRecords    = 0,
    descriptionCode = None,
    items           = Seq.empty
  )

  val validResponseRepaymentInterestRepaidSmall = RepaymentInterestRepaid(
    periodStartDate = Some(LocalDate.of(2013, 1, 1)),
    periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
    total           = BigDecimal(-45.00),
    totalRecords    = 1,
    items = Seq(
      RepaymentInterestRepaidItem(
        transactionDate = LocalDate.of(2014, 7, 22),
        amount          = -45.00
      )
    )
  )

  val validResponseInterestDetails = InterestDetails(
    periodStartDate = Some(LocalDate.of(2013, 3, 1)),
    periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
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

  val validResponseInterestDetailsSmall = InterestDetails(
    periodStartDate = Some(LocalDate.of(2013, 3, 1)),
    periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
    total           = -800.00,
    totalRecords    = 1,
    items = Seq(
      InterestDetailItem(
        descriptionCode = 2740,
        amount          = -800.00,
        interestId      = "SAFE-CHG-00003",
        periodStartDate = LocalDate.of(2014, 1, 1),
        periodEndDate   = LocalDate.of(2014, 3, 31)
      )
    )
  )

  val validResponseInterestDrilldown = InterestDrilldown(
    periodStartDate = Some(LocalDate.of(2013, 1, 1)),
    periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
    total           = BigDecimal(1250.50),
    totalRecords    = 1,
    descCode        = Some(42),
    items = Seq(
      InterestDrilldownItem(
        interestOn = BigDecimal(1000.00),
        dateFrom   = LocalDate.of(2013, 6, 1),
        dateTo     = LocalDate.of(2014, 6, 1),
        noOfDays   = BigDecimal(365),
        rate       = BigDecimal(2.5),
        amount     = BigDecimal(1250.50)
      )
    )
  )

  val validResponseInterestDrilldownEmpty = InterestDrilldown(
    periodStartDate = Some(LocalDate.of(2013, 1, 1)),
    periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
    total           = BigDecimal(0.00),
    totalRecords    = 0,
    descCode        = None,
    items           = Seq.empty
  )

  val validResponseInterestAccruingDetails = InterestAccruingDetails(
    periodStartDate = Some(LocalDate.of(2013, 1, 1)),
    periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
    total           = BigDecimal(7500.00),
    totalRecords    = 3,
    items = Seq(
      InterestAccruingDetailsItem(
        descriptionCode = 1,
        amount          = 3000.00,
        interestId      = "SAFE-CHG-00001",
        periodStartDate = LocalDate.of(2014, 10, 1),
        periodEndDate   = LocalDate.of(2014, 10, 31)
      ),
      InterestAccruingDetailsItem(
        descriptionCode = 2,
        amount          = 5000.00,
        interestId      = "SAFE-CHG-00002",
        periodStartDate = LocalDate.of(2014, 7, 15),
        periodEndDate   = LocalDate.of(2014, 7, 31)
      ),
      InterestAccruingDetailsItem(
        descriptionCode = 3,
        amount          = -500.00,
        interestId      = "SAFE-CHG-00003",
        periodStartDate = LocalDate.of(2013, 6, 1),
        periodEndDate   = LocalDate.of(2013, 6, 30)
      )
    )
  )

  val validResponseInterestAccruingDetailsSmall = InterestAccruingDetails(
    periodStartDate = Some(LocalDate.of(2013, 1, 1)),
    periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
    total           = BigDecimal(3000.00),
    totalRecords    = 1,
    items = Seq(
      InterestAccruingDetailsItem(
        descriptionCode = 1,
        amount          = BigDecimal(3000.00),
        interestId      = "SAFE-CHG-00001",
        periodStartDate = LocalDate.of(2014, 10, 1),
        periodEndDate   = LocalDate.of(2014, 10, 31)
      )
    )
  )

  val validResponseSubmittedReturns = SubmittedReturns(
    items = Seq(
      SubmittedReturnsItem(
        consec_no      = 12345,
        mgd_period     = "01/01/2025 - 30/03/2025",
        submitted_date = LocalDate.of(2025, 4, 1),
        ack_ref        = "123456789012345"
      ),
      SubmittedReturnsItem(
        consec_no      = 22345,
        mgd_period     = "01/04/2025 - 30/06/2025",
        submitted_date = LocalDate.of(2025, 7, 1),
        ack_ref        = "12345"
      ),
      SubmittedReturnsItem(
        consec_no      = 111222,
        mgd_period     = "10/02/2024 - 29/04/2024",
        submitted_date = LocalDate.of(2024, 5, 1),
        ack_ref        = "111222111222"
      )
    )
  )

  val validResponseSubmittedReturnsSmall = SubmittedReturns(
    items = Seq(
      SubmittedReturnsItem(
        consec_no      = 12345,
        mgd_period     = "01/01/2025 - 30/03/2025",
        submitted_date = LocalDate.of(2025, 4, 1),
        ack_ref        = "123456789012345"
      )
    )
  )
}
