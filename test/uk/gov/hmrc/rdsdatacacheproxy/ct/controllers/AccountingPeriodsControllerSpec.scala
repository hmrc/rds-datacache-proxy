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

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.AccountingPeriodsHelper
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.AccountingPeriodsService

import scala.concurrent.Future

class AccountingPeriodsControllerSpec extends SpecBase with MockitoSugar with AccountingPeriodsHelper {

  private class Setup {
    val mockService: AccountingPeriodsService = mock[AccountingPeriodsService]
    val controller: AccountingPeriodsController = new AccountingPeriodsController(fakeAuthAction, mockService, cc)
  }

  "AccountingPeriodsControllerSpec" - {
    "return a 200 and a successful response when retrieving accounting periods with one item" in new Setup {
      when(mockService.getAccountPeriods(any[Long]))
        .thenReturn(Future.successful(accountingPeriodsWithOneItem))

      val result: Future[Result] = controller.getAccountPeriods(6212811176L)(fakeRequest)

      status(result)      shouldBe OK
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getAccountPeriods(6212811176L)
    }

    "return a 200 and a successful response when retrieving accounting periods with multiple items" in new Setup {
      when(mockService.getAccountPeriods(any[Long]))
        .thenReturn(Future.successful(accountingPeriodsWithMultipleItems))

      val result: Future[Result] = controller.getAccountPeriods(6212811176L)(fakeRequest)

      status(result)      shouldBe OK
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getAccountPeriods(6212811176L)
    }

    "return a 200 and a successful response when retrieving empty accounting periods" in new Setup {
      when(mockService.getAccountPeriods(any[Long]))
        .thenReturn(Future.successful(emptyAccountingPeriods))

      val result: Future[Result] = controller.getAccountPeriods(1L)(fakeRequest)

      status(result)      shouldBe OK
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getAccountPeriods(1L)
    }

    "return 500 and when repository call fails" in new Setup {
      when(mockService.getAccountPeriods(any[Long]))
        .thenReturn(Future.failed(new RuntimeException("Error")))

      val result: Future[Result] = controller.getAccountPeriods(1L)(fakeRequest)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getAccountPeriods(1L)
    }

  }
}
