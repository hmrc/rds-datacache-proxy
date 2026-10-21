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

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.{PaymentSearchResult, PaymentSearchResultResponse}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.payments.PaymentSearchResultRepository

import java.time.LocalDate
import scala.concurrent.Future

trait PaymentSearchResultStubData {

  val emptyPaymentSearchResultResponse: PaymentSearchResultResponse = PaymentSearchResultResponse(paymentSearchResult = List.empty)

  val singlePaymentSearchResultResponse: PaymentSearchResultResponse = PaymentSearchResultResponse(
    paymentSearchResult = List(
      PaymentSearchResult(
        accountingPeriod       = Some(1),
        payslipId              = Some(1),
        amount                 = Some(37500),
        effectiveDateOfPayment = Some(LocalDate.of(2007, 7, 14)),
        paymentType            = Some("BGP"),
        bankSortCode           = Some(114433),
        bankAccountNumber      = Some(1004005),
        chequeNumber           = Some(100301),
        paymentAmount          = Some(31250),
        chequeId               = Some(916)
      )
    )
  )

  val multiplePaymentSearchResultResponse: PaymentSearchResultResponse = PaymentSearchResultResponse(
    paymentSearchResult = List(
      PaymentSearchResult(
        accountingPeriod       = Some(1),
        payslipId              = Some(1),
        amount                 = Some(37500),
        effectiveDateOfPayment = Some(LocalDate.of(2007, 7, 14)),
        paymentType            = Some("BGP"),
        bankSortCode           = Some(114433),
        bankAccountNumber      = Some(1004005),
        chequeNumber           = Some(100301),
        paymentAmount          = Some(31250),
        chequeId               = Some(916)
      ),
      PaymentSearchResult(
        accountingPeriod       = Some(1),
        payslipId              = Some(2),
        amount                 = Some(31250),
        effectiveDateOfPayment = Some(LocalDate.of(2006, 10, 14)),
        paymentType            = Some("BGP"),
        bankSortCode           = Some(114433),
        bankAccountNumber      = Some(1004005),
        chequeNumber           = Some(100301),
        paymentAmount          = Some(31250),
        chequeId               = Some(917)
      ),
      PaymentSearchResult(
        accountingPeriod       = Some(1),
        payslipId              = Some(3),
        amount                 = Some(37500),
        effectiveDateOfPayment = Some(LocalDate.of(2005, 7, 14)),
        paymentType            = Some("BGP"),
        bankSortCode           = Some(114433),
        bankAccountNumber      = Some(1004005),
        chequeNumber           = Some(100301),
        paymentAmount          = Some(31250),
        chequeId               = Some(918)
      )
    )
  )

  class PaymentSearchResultRdsStub extends PaymentSearchResultRepository {
    def getPaymentSearchResult(
      taxRef: Long,
      fromDate: Option[LocalDate],
      toDate: Option[LocalDate]
    ): Future[PaymentSearchResultResponse] = {
      (taxRef, fromDate.toString, toDate.toString) match {
        case (10L, _, _)  => Future.successful(singlePaymentSearchResultResponse)
        case (20L, _, _)  => Future.successful(multiplePaymentSearchResultResponse)
        case (2L, _, _)   => Future.successful(emptyPaymentSearchResultResponse)
        case (200L, _, _) => throw new Error("Downstream error")
      }
    }
  }
}
