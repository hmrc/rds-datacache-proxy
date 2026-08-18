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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{InterestAccrual, InterestAccruals}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.InterestAccrualListDatacacheRepository

import java.time.LocalDate
import scala.concurrent.Future

class InterestAccrualListServiceServiceSpec extends AnyWordSpec with Matchers with ScalaFutures with MockitoSugar {

  private class Setup {
    val mockRepository: InterestAccrualListDatacacheRepository = mock[InterestAccrualListDatacacheRepository]
    val service = new InterestAccrualService(mockRepository)

    val taxRef: Long = 1L
    val accPeriod: Long = 1L
    val interestType: String = "IDB"

    val interestAccrualListItems: InterestAccruals = InterestAccruals(
      List(
        InterestAccrual(
          computationAmount       = 1,
          interestAccrualFromDate = LocalDate.of(2021, 3, 7),
          interestAccrualToDate   = LocalDate.of(2021, 5, 7),
          interestRate            = 2,
          interestAmount          = 10,
          apEndDate               = LocalDate.of(2021, 6, 7)
        ),
        InterestAccrual(
          computationAmount       = 1,
          interestAccrualFromDate = LocalDate.of(2021, 6, 10),
          interestAccrualToDate   = LocalDate.of(2021, 8, 10),
          interestRate            = 3,
          interestAmount          = 23,
          apEndDate               = LocalDate.of(2021, 9, 10)
        )
      )
    )

  }

  "getInterestAcrrualList returns list of Interest Accrual List retrieved from repository" in new Setup {

    when(mockRepository.getInterestAccrualList(any[Long], any[Long], any[String]))
      .thenReturn(Future.successful(interestAccrualListItems))

    val result: InterestAccruals = service.getInterestAccrualList(taxRef, accPeriod, interestType).futureValue

    result mustBe interestAccrualListItems

    verify(mockRepository).getInterestAccrualList(taxRef, accPeriod, interestType)
  }

  "getInterestAcrrualList returns failure from repository" in new Setup {

    val ex = new RuntimeException("boom")

    when(mockRepository.getInterestAccrualList(any(), any(), any())).thenReturn(Future.failed(ex))

    val result: Throwable = service.getInterestAccrualList(taxRef, accPeriod, interestType).failed.futureValue

    result mustBe ex

    verify(mockRepository).getInterestAccrualList(taxRef, accPeriod, interestType)
    verifyNoMoreInteractions(mockRepository)

  }

}
