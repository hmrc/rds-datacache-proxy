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

package uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.payments

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.{PaymentPostingRow, PaymentPostingsResponse}

import java.time.LocalDate

trait PaymentPostingsStubData {

  val emptyPaymentPostings: PaymentPostingsResponse =
    PaymentPostingsResponse(List.empty)

  val singlePaymentPosting: PaymentPostingsResponse =
    PaymentPostingsResponse(
      List(
        PaymentPostingRow(
          accountingPeriod       = 202408L,
          settlementSequenceNo   = 123456L,
          payslipId              = 987654L,
          postingAmount          = 1500.75,
          effectiveDateOfPayment = LocalDate.of(2024, 9, 15),
          paymentType            = "BAC"
        )
      )
    )

  val multiplePaymentPostings: PaymentPostingsResponse =
    PaymentPostingsResponse(
      List(
        PaymentPostingRow(
          accountingPeriod       = 202408L,
          settlementSequenceNo   = 123456L,
          payslipId              = 987654L,
          postingAmount          = 1500.75,
          effectiveDateOfPayment = LocalDate.of(2024, 9, 15),
          paymentType            = "BAC"
        ),
        PaymentPostingRow(
          accountingPeriod       = 202407L,
          settlementSequenceNo   = 642736L,
          payslipId              = 983123L,
          postingAmount          = 2712.63,
          effectiveDateOfPayment = LocalDate.of(2024, 8, 20),
          paymentType            = "BAC"
        ),
        PaymentPostingRow(
          accountingPeriod       = 202404L,
          settlementSequenceNo   = 99423764L,
          payslipId              = 985624L,
          postingAmount          = 2416.89,
          effectiveDateOfPayment = LocalDate.of(2024, 5, 26),
          paymentType            = "BAC"
        )
      )
    )

  def getPaymentPostingsFromStubs(taxRef: Long): PaymentPostingsResponse = {
    taxRef match {
      case 7293742L => singlePaymentPosting
      case 93837L   => multiplePaymentPostings
      case 124252L  => emptyPaymentPostings
      case (99999)  => throw new RuntimeException("Error from downstream")
      case _        => singlePaymentPosting
    }
  }
}
