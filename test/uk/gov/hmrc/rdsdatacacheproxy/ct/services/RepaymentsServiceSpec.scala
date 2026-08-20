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
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.RepaymentsHelper
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.RepaymentsRepository

import scala.concurrent.Future

class RepaymentsServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar with RepaymentsHelper {

  private trait Setup {
    val mockRepo: RepaymentsRepository = mock[RepaymentsRepository]
    val service = new RepaymentsService(mockRepo)
  }

  "RepaymentsServiceSpec" - {
    "must return repayments with one item" in new Setup {
      when(mockRepo.getRepayments(any[Long], any[Long]))
        .thenReturn(Future.successful(repaymentsWithOneItem))

      val result = service.getRepayments(6212811176L, 2L).futureValue

      result mustBe repaymentsWithOneItem

      verify(mockRepo, times(1)).getRepayments(6212811176L, 2L)
    }

    "must return repayments with multiple items" in new Setup {
      when(mockRepo.getRepayments(any[Long], any[Long]))
        .thenReturn(Future.successful(repaymentsWithMultipleItems))

      val result = service.getRepayments(6212811176L, 2L).futureValue

      result mustBe repaymentsWithMultipleItems

      verify(mockRepo, times(1)).getRepayments(6212811176L, 2L)

    }

    "must return empty repayments" in new Setup {
      when(mockRepo.getRepayments(any[Long], any[Long]))
        .thenReturn(Future.successful(emptyRepayments))

      val result = service.getRepayments(1L, 2L).futureValue

      result mustBe emptyRepayments

      verify(mockRepo, times(1)).getRepayments(1L, 2L)
    }

    "must propagate failure from repository" in new Setup {
      val exception = new RuntimeException("Error")

      when(mockRepo.getRepayments(any[Long], any[Long]))
        .thenReturn(Future.failed(exception))

      val result = service.getRepayments(1L, 2L).failed.futureValue

      result mustBe exception

      verify(mockRepo, times(1)).getRepayments(1L, 2L)
    }
  }

}
