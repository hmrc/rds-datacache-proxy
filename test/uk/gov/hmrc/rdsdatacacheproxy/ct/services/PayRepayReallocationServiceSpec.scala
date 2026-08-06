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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.PayRepayReallocations
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.PayRepayReallocationRepository

import scala.concurrent.Future

class PayRepayReallocationServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar {

  private trait Setup {

    val mockRepo: PayRepayReallocationRepository = mock[PayRepayReallocationRepository]

    val service = new PayRepayReallocationService(mockRepo)

    val emptyPayRepayReallocations: PayRepayReallocations = PayRepayReallocations(Some(0), Some(0))
    val payRepayReallocations: PayRepayReallocations =
      PayRepayReallocations(
        totalAmountReoRfrRto = Some(BigDecimal(50.00)),
        totalAmountPayments  = Some(BigDecimal(60.00))
      )

  }

  "PayRepayReallocationServiceServiceSpec" - {
    "must return payment repayment allocation total amounts" in new Setup {
      when(mockRepo.getTotalAmounts(any[Long], any[Long]))
        .thenReturn(Future.successful(payRepayReallocations))

      val result = service.getTotalAmounts(6212811176L, 2L).futureValue

      result mustBe payRepayReallocations

      verify(mockRepo, times(1)).getTotalAmounts(6212811176L, 2L)

    }

    "must return empty payment repayment allocation total amounts" in new Setup {
      when(mockRepo.getTotalAmounts(any[Long], any[Long]))
        .thenReturn(Future.successful(emptyPayRepayReallocations))

      val result = service.getTotalAmounts(1L, 2L).futureValue

      result mustBe emptyPayRepayReallocations

      verify(mockRepo, times(1)).getTotalAmounts(1L, 2L)

    }

    "must propagate failure from repository" in new Setup {

      val exception = new RuntimeException("Error")

      when(mockRepo.getTotalAmounts(any[Long], any[Long]))
        .thenReturn(Future.failed(exception))

      val result = service.getTotalAmounts(1L, 2L).failed.futureValue

      result mustBe exception

      verify(mockRepo, times(1)).getTotalAmounts(1L, 2L)

    }
  }

}
