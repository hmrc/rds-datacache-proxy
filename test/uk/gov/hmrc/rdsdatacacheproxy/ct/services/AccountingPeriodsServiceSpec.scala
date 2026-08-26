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
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.AccountingPeriodsHelper
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.RdsAccountingPeriod
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.AccountingPeriodsRepository

import scala.concurrent.Future

class AccountingPeriodsServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar with AccountingPeriodsHelper {

  "getAccountingPeriods must delegate to repository and return AccountingPeriods" in new Setup {
    when(mockRepo.getAccountingPeriods(any())).thenReturn(Future.successful(accountingPeriodsWithMultipleItems))

    val result: RdsAccountingPeriod = service.getAccountingPeriods(taxRef).futureValue

    result mustBe accountingPeriodsWithMultipleItems

    verify(mockRepo, times(1)).getAccountingPeriods(any())
  }

  "getAccountingPeriods must propagate failure from repository " in new Setup {
    val exception = new RuntimeException("Boom")

    when(mockRepo.getAccountingPeriods(any())).thenReturn(Future.failed(exception))

    val result: Throwable = service.getAccountingPeriods(taxRef).failed.futureValue

    result mustBe exception

    result.getMessage must include("Boom")

    verify(mockRepo, times(1)).getAccountingPeriods(any())

    verifyNoMoreInteractions(mockRepo)

  }

  trait Setup {
    val mockRepo: AccountingPeriodsRepository = mock[AccountingPeriodsRepository]
    val service = new AccountingPeriodsService(mockRepo)
    val taxRef: Long = 1234L
  }
}
