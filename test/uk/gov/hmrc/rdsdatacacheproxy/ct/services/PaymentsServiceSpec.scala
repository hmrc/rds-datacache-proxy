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

package uk.gov.hmrc.rdsdatacacheproxy.ct.services

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, verifyNoMoreInteractions, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.PaymentTransactions
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.PaymentsCtDataCacheRepository

import java.time.LocalDate
import scala.concurrent.Future

class PaymentsServiceSpec extends AnyWordSpec with Matchers with ScalaFutures with MockitoSugar {

  private class Setup {
    val mockRepository: PaymentsCtDataCacheRepository = mock[PaymentsCtDataCacheRepository]
    val service = new PaymentsService(mockRepository)

    val taxRef: Long = 1234567L
    val accPeriod: Long = 1L

    val payments: List[PaymentTransactions] = List(
      PaymentTransactions(amount = 123.44, paymentType   = "CP", effectiveDateOfPayment = LocalDate.of(2026, 1, 1)),
      PaymentTransactions(amount = 3213.44, paymentType  = "CP", effectiveDateOfPayment = LocalDate.of(2026, 2, 1)),
      PaymentTransactions(amount = 56785.45, paymentType = "CP", effectiveDateOfPayment = LocalDate.of(2026, 1, 23))
    )

  }

  "getPayments returns list of Payment Transactions retrieved from repository" in new Setup {

    when(mockRepository.getPayments(any[Long], any[Long]))
      .thenReturn(Future.successful(payments))

    val result: List[PaymentTransactions] = service.getPayments(taxRef, accPeriod).futureValue

    result mustBe payments

    verify(mockRepository).getPayments(taxRef, accPeriod)
  }

  "getPayments returns failure from repository" in new Setup {

    val ex = new RuntimeException("boom")

    when(mockRepository.getPayments(any(), any())).thenReturn(Future.failed(ex))

    val result: Throwable = service.getPayments(taxRef, accPeriod).failed.futureValue

    result mustBe ex

    verify(mockRepository).getPayments(taxRef, accPeriod)
    verifyNoMoreInteractions(mockRepository)

  }

}
