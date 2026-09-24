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

package uk.gov.hmrc.rdsdatacacheproxy.ct.services.gpa

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.gpa.PaymentAllocationDetailsRepository
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.gpa.PaymentAllocationDetailsHelper

import scala.concurrent.Future

class PaymentAllocationDetailsServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar with PaymentAllocationDetailsHelper {

  private trait Setup {

    val mockRepo: PaymentAllocationDetailsRepository = mock[PaymentAllocationDetailsRepository]

    val service = new PaymentAllocationDetailsService(mockRepo)

  }

  "PaymentAllocationDetailsServiceSpec" - {
    "must return payment allocation details" in new Setup {
      when(mockRepo.getGPAPaymentAllocationDetail(any[Long], any[Long], any[Long], any[Long], any[Long], any[Long]))
        .thenReturn(Future.successful(fullPaymentAllocationDetails))

      val result = service.getGPAPaymentAllocationDetail(6212811176L, 2L, 3L, 4L, 5L, 6L).futureValue

      result mustBe fullPaymentAllocationDetails

      verify(mockRepo, times(1)).getGPAPaymentAllocationDetail(6212811176L, 2L, 3L, 4L, 5L, 6L)
    }

    "must return payment allocation details with multiple allocation details" in new Setup {
      when(mockRepo.getGPAPaymentAllocationDetail(any[Long], any[Long], any[Long], any[Long], any[Long], any[Long]))
        .thenReturn(Future.successful(paymentAllocationDetailsWithMultipleAllocationDetails))

      val result = service.getGPAPaymentAllocationDetail(6212811176L, 2L, 3L, 4L, 5L, 6L).futureValue

      result mustBe paymentAllocationDetailsWithMultipleAllocationDetails

      verify(mockRepo, times(1)).getGPAPaymentAllocationDetail(6212811176L, 2L, 3L, 4L, 5L, 6L)

    }

    "must return payment allocation details with minimal details" in new Setup {
      when(mockRepo.getGPAPaymentAllocationDetail(any[Long], any[Long], any[Long], any[Long], any[Long], any[Long]))
        .thenReturn(Future.successful(minimalPaymentAllocationDetails))

      val result = service.getGPAPaymentAllocationDetail(1L, 2L, 3L, 4L, 5L, 6L).futureValue

      result mustBe minimalPaymentAllocationDetails

      verify(mockRepo, times(1)).getGPAPaymentAllocationDetail(1L, 2L, 3L, 4L, 5L, 6L)
    }

    "must propagate failure from repository" in new Setup {
      val exception = new RuntimeException("Error")

      when(mockRepo.getGPAPaymentAllocationDetail(any[Long], any[Long], any[Long], any[Long], any[Long], any[Long]))
        .thenReturn(Future.failed(exception))

      val result = service.getGPAPaymentAllocationDetail(1L, 2L, 3L, 4L, 5L, 6L).failed.futureValue

      result mustBe exception

      verify(mockRepo, times(1)).getGPAPaymentAllocationDetail(1L, 2L, 3L, 4L, 5L, 6L)
    }
  }

}
