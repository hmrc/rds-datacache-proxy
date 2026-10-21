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

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers.payments

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.payments.ElectronicPaymentAllocationDetailsHelper
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.payments.ElectronicPaymentAllocationDetailsService

import scala.concurrent.Future

class ElectronicPaymentAllocationDetailsControllerSpec extends SpecBase with MockitoSugar with ElectronicPaymentAllocationDetailsHelper {

  private class Setup {
    val mockService: ElectronicPaymentAllocationDetailsService = mock[ElectronicPaymentAllocationDetailsService]
    val controller: ElectronicPaymentAllocationDetailsController = new ElectronicPaymentAllocationDetailsController(fakeAuthAction, mockService, cc)
  }

  "ElectronicPaymentAllocationDetailsControllerSpec" - {
    "return a 200 and a successful response for electronic payment allocation details" in new Setup {
      when(mockService.getPayAllocationElectronicDtls(any[Long], any[Long], any[Long]))
        .thenReturn(Future.successful(ePaymentAllocationDetails))

      val result: Future[Result] =
        controller.getPayAllocationElectronicDtls(6212811176L, 2L, 3L)(fakeRequest)

      status(result)      shouldBe OK
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getPayAllocationElectronicDtls(6212811176L, 2L, 3L)
    }

    "return a 200 and a successful response for multiple electronic payment allocation details" in new Setup {
      when(mockService.getPayAllocationElectronicDtls(any[Long], any[Long], any[Long]))
        .thenReturn(Future.successful(multipleEPaymentAllocationDetails))

      val result: Future[Result] =
        controller.getPayAllocationElectronicDtls(6212811176L, 2L, 3L)(fakeRequest)

      status(result)      shouldBe OK
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getPayAllocationElectronicDtls(6212811176L, 2L, 3L)
    }

    "return a 200 and a successful response for empty electronic payment allocation details" in new Setup {
      when(mockService.getPayAllocationElectronicDtls(any[Long], any[Long], any[Long]))
        .thenReturn(Future.successful(emptyEPaymentAllocationDetails))

      val result: Future[Result] =
        controller.getPayAllocationElectronicDtls(1L, 2L, 3L)(fakeRequest)

      status(result)      shouldBe OK
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getPayAllocationElectronicDtls(1L, 2L, 3L)
    }

    "return 500 and when repository call fails" in new Setup {
      when(mockService.getPayAllocationElectronicDtls(any[Long], any[Long], any[Long]))
        .thenReturn(Future.failed(new RuntimeException("Error")))

      val result: Future[Result] =
        controller.getPayAllocationElectronicDtls(1L, 2L, 3L)(fakeRequest)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getPayAllocationElectronicDtls(1L, 2L, 3L)
    }

  }
}
