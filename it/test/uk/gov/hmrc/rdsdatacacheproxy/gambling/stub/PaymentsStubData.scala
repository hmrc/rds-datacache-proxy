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

import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{PaymentItem, Payments}

import java.time.LocalDate

object PaymentsStubData {
  def getPaymentsData(regNumber: String, paginationStart: Int = 1, paginationMaxRows: Int = 10): Payments =
    regNumber match {
      case "XYZ00000000000" =>
        Payments(
          periodStartDate = Some(LocalDate.of(2013, 1, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
          total           = BigDecimal(0.00),
          totalRecords    = 0,
          items           = Seq()
        )
      case "XYZ00000000001" =>
        Payments(
          periodStartDate = Some(LocalDate.of(2013, 1, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
          total           = BigDecimal(7500.00),
          totalRecords    = 3,
          items = Seq(
            PaymentItem(
              transactionDate = LocalDate.of(2014, 10, 1),
              descriptionCode = "P",
              amount          = 3000.00
            ),
            PaymentItem(
              transactionDate = LocalDate.of(2014, 7, 15),
              descriptionCode = "P",
              amount          = 5000.00
            ),
            PaymentItem(
              transactionDate = LocalDate.of(2013, 6, 1),
              descriptionCode = "F",
              amount          = -500.00
            )
          )
        )
      case "XYZ00000000010" =>
        Payments(
          periodStartDate = Some(LocalDate.of(2013, 1, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
          total           = BigDecimal(7500.00),
          totalRecords    = 1,
          items = Seq(
            PaymentItem(
              transactionDate = LocalDate.of(2014, 10, 1),
              descriptionCode = "P",
              amount          = 3000.00
            )
          )
        )

      case "XYZ00000000012" =>
        Payments(
          periodStartDate = Some(LocalDate.of(2013, 1, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
          total           = BigDecimal(7000.00),
          totalRecords    = 4,
          items = Seq(
            PaymentItem(
              transactionDate = LocalDate.of(2014, 10, 1),
              descriptionCode = "P",
              amount          = 3000.00
            ),
            PaymentItem(
              transactionDate = LocalDate.of(2014, 7, 15),
              descriptionCode = "P",
              amount          = 5000.00
            ),
            PaymentItem(
              transactionDate = LocalDate.of(2013, 6, 1),
              descriptionCode = "F",
              amount          = -500.00
            ),
            PaymentItem(
              transactionDate = LocalDate.of(2013, 6, 1),
              descriptionCode = "F",
              amount          = -500.00
            )
          )
        )
      case "XYZ00000000021" =>
        Payments(
          periodStartDate = Some(LocalDate.of(2013, 1, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
          total           = BigDecimal(7500.00),
          totalRecords    = 3,
          items = Seq(
            PaymentItem(
              transactionDate = LocalDate.of(2014, 10, 1),
              descriptionCode = "P",
              amount          = 3000.00
            ),
            PaymentItem(
              transactionDate = LocalDate.of(2014, 7, 15),
              descriptionCode = "P",
              amount          = 5000.00
            ),
            PaymentItem(
              transactionDate = LocalDate.of(2013, 6, 1),
              descriptionCode = "F",
              amount          = -500.00
            )
          )
        )
      case "XYZ99999999999" =>
        Payments(
          periodStartDate = Some(LocalDate.of(2013, 2, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 11, 3)),
          total           = 999.00,
          totalRecords    = 99,
          items           = Seq()
        )
      case "ERR00000000000" => throw new RuntimeException("Simulated downstream failure")
      case _ =>
        Payments(
          periodStartDate = Some(LocalDate.of(2013, 3, 1)),
          periodEndDate   = Some(LocalDate.of(2014, 3, 11)),
          total           = 4000.00,
          totalRecords    = 2,
          items = Seq(
            PaymentItem(
              transactionDate = LocalDate.of(2014, 9, 1),
              descriptionCode = "P",
              amount          = 4500.00
            ),
            PaymentItem(
              transactionDate = LocalDate.of(2014, 4, 1),
              descriptionCode = "F",
              amount          = -500.00
            )
          )
        )
    }

}
