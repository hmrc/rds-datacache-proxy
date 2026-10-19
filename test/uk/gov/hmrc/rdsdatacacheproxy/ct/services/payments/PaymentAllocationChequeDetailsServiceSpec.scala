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

package uk.gov.hmrc.rdsdatacacheproxy.ct.services.payments

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.{PaymentAllocationChequeDetails, PaymentAllocationChequeRow}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.payments.PaymentAllocationChequeDetailsRepository

import java.time.LocalDate
import scala.concurrent.Future

class PaymentAllocationChequeDetailsServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar {

  private trait Setup {

    val mockRepo: PaymentAllocationChequeDetailsRepository = mock[PaymentAllocationChequeDetailsRepository]

    val service = new PaymentAllocationChequeDetailsService(mockRepo)

    val taxRef: Long = 98762425L
    val chequeId: Long = 63L

    private val paymentAllocationChequeDetailsRow = List(
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

    val paymentAllocationChequeDetails: PaymentAllocationChequeDetails =
      PaymentAllocationChequeDetails(paymentAllocationDetails = paymentAllocationChequeDetailsRow)
  }

  "PaymentAllocationChequeDetailsService must return list of PaymentAllocationChequeDetails" in new Setup {
    when(mockRepo.getPaymentAllocationChequeDetails(any(), any()))
      .thenReturn(Future.successful(paymentAllocationChequeDetails))

    val result: PaymentAllocationChequeDetails = service.getPaymentAllocationChequeDetails(taxRef, chequeId).futureValue

    result mustBe paymentAllocationChequeDetails

    verify(mockRepo, times(1)).getPaymentAllocationChequeDetails(any(), any())

  }
  "PaymentAllocationChequeDetailsService must propagate failure from repository" in new Setup {

    val exception = new RuntimeException("Failure from Repo")

    when(mockRepo.getPaymentAllocationChequeDetails(any(), any()))
      .thenReturn(Future.failed(exception))

    val result: Throwable = service.getPaymentAllocationChequeDetails(taxRef, chequeId).failed.futureValue

    result mustBe exception

    verify(mockRepo, times(1)).getPaymentAllocationChequeDetails(taxRef, chequeId)

  }

}
