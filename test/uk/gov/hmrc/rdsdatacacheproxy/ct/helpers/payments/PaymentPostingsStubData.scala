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

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.{PaymentPostingResponse, PaymentPostingRow}

import java.time.LocalDate

trait PaymentPostingsStubData {

  val paymentPostingsList = List(
    PaymentPostingRow(
      accountingPeriod       = 12820832002L,
      settlementSequenceNo   = 5678L,
      payslipId              = 999123L,
      postingAmount          = 123.45,
      effectiveDateOfPayment = LocalDate.of(2016, 11, 9),
      paymentType            = "BAC"
    ),
    PaymentPostingRow(
      accountingPeriod       = 12820832002L,
      settlementSequenceNo   = 5678L,
      payslipId              = 999123L,
      postingAmount          = 123.45,
      effectiveDateOfPayment = LocalDate.of(2017, 11, 9),
      paymentType            = "BAC"
    )
  )

  val paymentPostingsResponse: PaymentPostingResponse = PaymentPostingResponse(paymentPostings = paymentPostingsList)

  val paymentPostingsResponseWIthEmptyList: PaymentPostingResponse = PaymentPostingResponse(paymentPostings = List.empty)

  def getPaymentPostings(taxRef: Long): PaymentPostingResponse = {
    taxRef match {
      case (1L)        => paymentPostingsResponse
      case (98765432L) => paymentPostingsResponseWIthEmptyList
      case (99999)     => throw new RuntimeException("Error from downstream")
      case (_)         => paymentPostingsResponseWIthEmptyList
    }
  }
}
