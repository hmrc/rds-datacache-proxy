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
import org.mockito.Mockito.{times, verify, verifyNoMoreInteractions, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{InterestCharges, InterestChargesResponse}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.InterestChargeSummaryDataCacheRepositoryImpl

import scala.concurrent.Future

class InterestChargeServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar {

  "getInterestSummaryList must delegate the repository and return proper response " in new Setup {

    when(mockRepository.getInterestSummary(any())).thenReturn(Future.successful(interestCharges))

    val result: InterestCharges = service.getInterestSummaryList(taxPayerReference).futureValue

    result mustBe interestCharges

    verify(mockRepository, times(1)).getInterestSummary(taxPayerReference)

  }

  "getInterestSummaryList must propagate failure from repository" in new Setup {

    val ex = new RuntimeException("boom")

    when(mockRepository.getInterestSummary(any())).thenReturn(Future.failed(ex))

    val result: Throwable = service.getInterestSummaryList(taxPayerReference).failed.futureValue

    result mustBe ex

    verify(mockRepository).getInterestSummary(taxPayerReference)
    verifyNoMoreInteractions(mockRepository)

  }

  private trait Setup {
    val mockRepository: InterestChargeSummaryDataCacheRepositoryImpl = mock[InterestChargeSummaryDataCacheRepositoryImpl]
    val service = new InterestChargeService(mockRepository)

    val taxPayerReference: String = "123456"

    val interestCharges: InterestCharges = InterestCharges(
      List(
        InterestChargesResponse(
          accountingPeriod      = 12,
          interestChargeSummary = 123.54
        ),
        InterestChargesResponse(
          accountingPeriod      = 98,
          interestChargeSummary = 98.56
        ),
        InterestChargesResponse(
          accountingPeriod      = 6,
          interestChargeSummary = 56.78
        )
      )
    )
  }

}
