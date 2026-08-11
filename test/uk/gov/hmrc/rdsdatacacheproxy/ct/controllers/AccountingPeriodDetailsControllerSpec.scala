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
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.AccountingPeriodDetailsStubData
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.APBalanced
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.AccountingPeriodDetailsRepositoryImpl

import scala.concurrent.Future

class AccountingPeriodDetailsControllerSpec extends SpecBase with MockitoSugar with AccountingPeriodDetailsStubData {

  private class SetUp {
    val mockAccountingPeriodDetailsRepository: AccountingPeriodDetailsRepositoryImpl = mock[AccountingPeriodDetailsRepositoryImpl]
    val controller: AccountingPeriodDetailsController =
      new AccountingPeriodDetailsController(fakeAuthAction, mockAccountingPeriodDetailsRepository, cc)
  }

  "AccountingPeriodDetailsController#getIsAPBalanced" - {
    "return 200 and a successful response when repository return default record" in new SetUp {
      when(mockAccountingPeriodDetailsRepository.getIsAPBalanced(any[Long], any[Long]))
        .thenReturn(Future.successful(aPBalancedItemDefault))

      val result: Future[Result] = controller.getIsAPBalanced(1L, 1L)(fakeRequest)

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(APBalanced(aPBalancedItemDefault))
      contentType(result)   shouldBe Some("application/json")
      verify(mockAccountingPeriodDetailsRepository).getIsAPBalanced(1L, 1L)
    }

    "return 200 and a successful response when repository return empty record" in new SetUp {
      when(mockAccountingPeriodDetailsRepository.getIsAPBalanced(any[Long], any[Long]))
        .thenReturn(Future.successful(aPBalancedItemEmpty))

      val result: Future[Result] = controller.getIsAPBalanced(1L, 2L)(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(APBalanced(aPBalancedItemEmpty))
      verify(mockAccountingPeriodDetailsRepository).getIsAPBalanced(1L, 2L)
    }

    "return 500 and when repository call fails" in new SetUp {
      when(mockAccountingPeriodDetailsRepository.getIsAPBalanced(any[Long], any[Long]))
        .thenReturn(Future.failed(new RuntimeException("Database error")))

      val result: Future[Result] = controller.getIsAPBalanced(3L, 7L)(fakeRequest)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      verify(mockAccountingPeriodDetailsRepository).getIsAPBalanced(3L, 7L)
    }

  }

}
