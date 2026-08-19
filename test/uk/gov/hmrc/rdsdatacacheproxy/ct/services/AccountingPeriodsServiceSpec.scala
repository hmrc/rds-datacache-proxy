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
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.AccountingPeriodsHelper
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.AccountingPeriodsRepository

import scala.concurrent.Future

class AccountingPeriodsServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar with AccountingPeriodsHelper {

  private trait Setup {

    val mockRepo: AccountingPeriodsRepository = mock[AccountingPeriodsRepository]

    val service = new AccountingPeriodsService(mockRepo)
  }

  "AccountingPeriodsServiceSpec" - {
    "must return accounting periods with one item" in new Setup {
      when(mockRepo.getAccountPeriods(any[Long]))
        .thenReturn(Future.successful(accountingPeriodsWithOneItem))

      val result = service.getAccountPeriods(6212811176L).futureValue

      result mustBe accountingPeriodsWithOneItem

      verify(mockRepo, times(1)).getAccountPeriods(6212811176L)
    }

    "must return accounting periods with multiple items" in new Setup {
      when(mockRepo.getAccountPeriods(any[Long]))
        .thenReturn(Future.successful(accountingPeriodsWithMultipleItems))

      val result = service.getAccountPeriods(6212811176L).futureValue

      result mustBe accountingPeriodsWithMultipleItems

      verify(mockRepo, times(1)).getAccountPeriods(6212811176L)

    }

    "must return empty accounting periods" in new Setup {
      when(mockRepo.getAccountPeriods(any[Long]))
        .thenReturn(Future.successful(emptyAccountingPeriods))

      val result = service.getAccountPeriods(1L).futureValue

      result mustBe emptyAccountingPeriods

      verify(mockRepo, times(1)).getAccountPeriods(1L)
    }

    "must propagate failure from repository" in new Setup {
      val exception = new RuntimeException("Error")

      when(mockRepo.getAccountPeriods(any[Long]))
        .thenReturn(Future.failed(exception))

      val result = service.getAccountPeriods(1L).failed.futureValue

      result mustBe exception

      verify(mockRepo, times(1)).getAccountPeriods(1L)
    }
  }

}
