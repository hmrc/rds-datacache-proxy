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

package uk.gov.hmrc.rdsdatacacheproxy.ct.stub.payments

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.{PaymentAllocationChequeDetails, PaymentAllocationChequeRow}

import java.time.LocalDate

object PaymentAllocationChequeDetailsStubData {

  val paymentAllocationChequeDetailsRow: List[PaymentAllocationChequeRow] = List(
    PaymentAllocationChequeRow(
      accountingPeriod       = 12820832002L,
      apEndDate              = LocalDate.of(2016, 11, 9),
      amount                 = BigDecimal(7205720.982),
      effectiveDateOfPayment = LocalDate.of(2026, 11, 12)
    ),
    PaymentAllocationChequeRow(
      accountingPeriod       = 1232002L,
      apEndDate              = LocalDate.of(2017, 11, 9),
      amount                 = BigDecimal(7200.982),
      effectiveDateOfPayment = LocalDate.of(2026, 12, 12)
    )
  )
  
  val paymentAllocationChequeDetails: PaymentAllocationChequeDetails = PaymentAllocationChequeDetails(paymentAllocationDetails = paymentAllocationChequeDetailsRow)

  val emptyPaymentAllocationChequeDetails: PaymentAllocationChequeDetails = PaymentAllocationChequeDetails(paymentAllocationDetails = List.empty)

  def getPaymentAllocationChequeDetails(taxRef: Long, chequeId: Long): PaymentAllocationChequeDetails = {
    (taxRef, chequeId) match {
      case (122323L, 16L)  => paymentAllocationChequeDetails
      case (9798232L, 37L) => throw new RuntimeException("Error from downstream")
      case (_, _)          => emptyPaymentAllocationChequeDetails
    }
  }

}
