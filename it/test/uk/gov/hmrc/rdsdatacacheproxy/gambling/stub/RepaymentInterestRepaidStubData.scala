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

import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{RepaymentInterestRepaid, RepaymentInterestRepaidItem}

import java.time.LocalDate

object RepaymentInterestRepaidStubData {
  def getRepaymentInterestRepaidData(regNumber: String, paginationStart: Int = 1, paginationMaxRows: Int = 10): RepaymentInterestRepaid =
    regNumber match {
      case "XYZ00000000000" =>
        RepaymentInterestRepaid(
          periodStartDate = Some(LocalDate.of(2013, 1, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
          total           = BigDecimal(0.00),
          totalRecords    = 0,
          items           = Seq()
        )
      case "XYZ00000000001" =>
        RepaymentInterestRepaid(
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
      case "XYZ00000000010" =>
        RepaymentInterestRepaid(
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

      case "XYZ00000000012" =>
        RepaymentInterestRepaid(
          periodStartDate = Some(LocalDate.of(2013, 1, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
          total           = BigDecimal(7000.00),
          totalRecords    = 4,
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
            ),
            RepaymentInterestRepaidItem(
              transactionDate = LocalDate.of(2014, 9, 18),
              amount          = -500.00
            )
          )
        )
      case "XYZ00000000021" =>
        RepaymentInterestRepaid(
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
      case "XYZ99999999999" =>
        RepaymentInterestRepaid(
          periodStartDate = Some(LocalDate.of(2013, 1, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
          total           = 999.00,
          totalRecords    = 99,
          items           = Seq()
        )
      case "ERR00000000000" => throw new RuntimeException("Simulated downstream failure")
      case _ =>
        RepaymentInterestRepaid(
          periodStartDate = Some(LocalDate.of(2013, 1, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
          total           = 4000.00,
          totalRecords    = 2,
          items = Seq(
            RepaymentInterestRepaidItem(
              transactionDate = LocalDate.of(2014, 9, 1),
              amount          = 4500.00
            ),
            RepaymentInterestRepaidItem(
              transactionDate = LocalDate.of(2014, 4, 1),
              amount          = -500.00
            )
          )
        )
    }

}
