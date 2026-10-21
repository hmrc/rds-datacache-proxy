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
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.payments.ElectronicPaymentAllocationDetailsHelper
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.payments.ElectronicPaymentAllocationDetailsRepository

import scala.concurrent.Future

class ElectronicPaymentAllocationDetailsServiceSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with MockitoSugar
    with ElectronicPaymentAllocationDetailsHelper {

  private trait Setup {

    val mockRepo: ElectronicPaymentAllocationDetailsRepository = mock[ElectronicPaymentAllocationDetailsRepository]

    val service = new ElectronicPaymentAllocationDetailsService(mockRepo)

  }

  "ElectronicPaymentAllocationDetailsServiceSpec" - {
    "must return electronic payment allocation details" in new Setup {
      when(mockRepo.getPayAllocationElectronicDtls(any[Long], any[Long], any[Long]))
        .thenReturn(Future.successful(ePaymentAllocationDetails))

      val result = service.getPayAllocationElectronicDtls(6212811176L, 2L, 3L).futureValue

      result mustBe ePaymentAllocationDetails

      verify(mockRepo, times(1)).getPayAllocationElectronicDtls(6212811176L, 2L, 3L)
    }

    "must return payment allocation details with multiple allocation details" in new Setup {
      when(mockRepo.getPayAllocationElectronicDtls(any[Long], any[Long], any[Long]))
        .thenReturn(Future.successful(multipleEPaymentAllocationDetails))

      val result = service.getPayAllocationElectronicDtls(6212811176L, 2L, 3L).futureValue

      result mustBe multipleEPaymentAllocationDetails

      verify(mockRepo, times(1)).getPayAllocationElectronicDtls(6212811176L, 2L, 3L)

    }

    "must return payment allocation details with minimal details" in new Setup {
      when(mockRepo.getPayAllocationElectronicDtls(any[Long], any[Long], any[Long]))
        .thenReturn(Future.successful(emptyEPaymentAllocationDetails))

      val result = service.getPayAllocationElectronicDtls(1L, 2L, 3L).futureValue

      result mustBe emptyEPaymentAllocationDetails

      verify(mockRepo, times(1)).getPayAllocationElectronicDtls(1L, 2L, 3L)
    }

    "must propagate failure from repository" in new Setup {
      val exception = new RuntimeException("Error")

      when(mockRepo.getPayAllocationElectronicDtls(any[Long], any[Long], any[Long]))
        .thenReturn(Future.failed(exception))

      val result = service.getPayAllocationElectronicDtls(1L, 2L, 3L).failed.futureValue

      result mustBe exception

      verify(mockRepo, times(1)).getPayAllocationElectronicDtls(1L, 2L, 3L)
    }
  }

}
