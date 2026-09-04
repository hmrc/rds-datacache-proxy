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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.TaxTransactionsItem
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.TaxTransactionsDataCacheRepository

import java.time.LocalDate
import scala.concurrent.Future

class TaxTransactionsServiceSpec extends AnyWordSpec with Matchers with ScalaFutures with MockitoSugar {

  private class Setup {
    val mockRepository: TaxTransactionsDataCacheRepository = mock[TaxTransactionsDataCacheRepository]
    val service = new TaxTransactionsService(mockRepository)

    val taxRef: Long = 1234567L
    val accPeriod: Long = 1L

    val taxTransactions: List[TaxTransactionsItem] =
      List(
        TaxTransactionsItem(currentAmount = 1234.44, assessmentType = "A", taxDate = LocalDate.of(2026, 1, 1), correctionClaimSignal = Some("1")),
        TaxTransactionsItem(currentAmount = 2345.44, assessmentType = "D", taxDate = LocalDate.of(2026, 2, 1), correctionClaimSignal = Some("1")),
        TaxTransactionsItem(currentAmount = 6754.44, assessmentType = "E", taxDate = LocalDate.of(2026, 3, 1), correctionClaimSignal = Some("1"))
      )

  }

  "getTaxTransactions returns list of Tax Transactions retrieved from repository" in new Setup {

    when(mockRepository.getTaxTransactions(any[Long], any[Long]))
      .thenReturn(Future.successful(taxTransactions))

    val result: List[TaxTransactionsItem] = service.getTaxTransactions(taxRef, accPeriod).futureValue

    result mustBe taxTransactions

    verify(mockRepository).getTaxTransactions(taxRef, accPeriod)
  }

  "getTaxTransactions returns failure from repository" in new Setup {

    val ex = new RuntimeException("boom")

    when(mockRepository.getTaxTransactions(any(), any())).thenReturn(Future.failed(ex))

    val result: Throwable = service.getTaxTransactions(taxRef, accPeriod).failed.futureValue

    result mustBe ex

    verify(mockRepository).getTaxTransactions(taxRef, accPeriod)
    verifyNoMoreInteractions(mockRepository)

  }

}
