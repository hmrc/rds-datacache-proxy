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
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.payments.AllAccountingPeriodsHelper
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.payments.AllAccountingPeriodsRepository

import scala.concurrent.Future

class AllAccountingPeriodsServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar with AllAccountingPeriodsHelper {

  private trait Setup {

    val mockRepo: AllAccountingPeriodsRepository = mock[AllAccountingPeriodsRepository]

    val service = new AllAccountingPeriodsService(mockRepo)

  }

  "AllAccountingPeriodsServiceSpec" - {
    "must return all accounting periods" in new Setup {
      when(mockRepo.getAccountingPeriodList(any[Long]))
        .thenReturn(Future.successful(allAccountingPeriods))

      val result = service.getAccountingPeriodList(6212811176L).futureValue

      result mustBe allAccountingPeriods

      verify(mockRepo, times(1)).getAccountingPeriodList(6212811176L)
    }

    "must return multiple items of all accounting periods" in new Setup {
      when(mockRepo.getAccountingPeriodList(any[Long]))
        .thenReturn(Future.successful(multipleAllAccountingPeriods))

      val result = service.getAccountingPeriodList(6212811176L).futureValue

      result mustBe multipleAllAccountingPeriods

      verify(mockRepo, times(1)).getAccountingPeriodList(6212811176L)

    }

    "must return empty list of all accounting periods" in new Setup {
      when(mockRepo.getAccountingPeriodList(any[Long]))
        .thenReturn(Future.successful(emptyAllAccountingPeriods))

      val result = service.getAccountingPeriodList(1L).futureValue

      result mustBe emptyAllAccountingPeriods

      verify(mockRepo, times(1)).getAccountingPeriodList(1L)
    }

    "must propagate failure from repository" in new Setup {
      val exception = new RuntimeException("Error")

      when(mockRepo.getAccountingPeriodList(any[Long]))
        .thenReturn(Future.failed(exception))

      val result = service.getAccountingPeriodList(1L).failed.futureValue

      result mustBe exception

      verify(mockRepo, times(1)).getAccountingPeriodList(1L)
    }
  }

}
