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

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers.gpa

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.gpa.PaymentAllocationDetailsService
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.gpa.PaymentAllocationDetailsHelper

import scala.concurrent.Future

class PaymentAllocationDetailsControllerSpec extends SpecBase with MockitoSugar with PaymentAllocationDetailsHelper {

  private class Setup {
    val mockService: PaymentAllocationDetailsService = mock[PaymentAllocationDetailsService]
    val controller: PaymentAllocationDetailsController = new PaymentAllocationDetailsController(fakeAuthAction, mockService, cc)

  }

  "PaymentAllocationDetailsControllerSpec" - {
    "return a 200 and a successful response for payment allocation details" in new Setup {
      when(mockService.getGPAPaymentAllocationDetail(any[Long], any[Long], any[Long], any[Long], any[Long], any[Long]))
        .thenReturn(Future.successful(fullPaymentAllocationDetails))

      val result: Future[Result] = controller.getGPAPaymentAllocationDetail(6212811176L, 2L, 3L, 4L, 5L, 6L)(fakeRequest)

      status(result)      shouldBe OK
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getGPAPaymentAllocationDetail(6212811176L, 2L, 3L, 4L, 5L, 6L)
    }

    "return a 200 and a successful response for payment allocation details with multiple allocation details" in new Setup {
      when(mockService.getGPAPaymentAllocationDetail(any[Long], any[Long], any[Long], any[Long], any[Long], any[Long]))
        .thenReturn(Future.successful(paymentAllocationDetailsWithMultipleAllocationDetails))

      val result: Future[Result] = controller.getGPAPaymentAllocationDetail(6212811176L, 2L, 3L, 4L, 5L, 6L)(fakeRequest)

      status(result)      shouldBe OK
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getGPAPaymentAllocationDetail(6212811176L, 2L, 3L, 4L, 5L, 6L)
    }

    "return a 200 and a successful response for payment allocation details with minimal details" in new Setup {
      when(mockService.getGPAPaymentAllocationDetail(any[Long], any[Long], any[Long], any[Long], any[Long], any[Long]))
        .thenReturn(Future.successful(minimalPaymentAllocationDetails))

      val result: Future[Result] = controller.getGPAPaymentAllocationDetail(1L, 2L, 3L, 4L, 5L, 6L)(fakeRequest)

      status(result)      shouldBe OK
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getGPAPaymentAllocationDetail(1L, 2L, 3L, 4L, 5L, 6L)
    }

    "return 500 and when repository call fails" in new Setup {
      when(mockService.getGPAPaymentAllocationDetail(any[Long], any[Long], any[Long], any[Long], any[Long], any[Long]))
        .thenReturn(Future.failed(new RuntimeException("Error")))

      val result: Future[Result] = controller.getGPAPaymentAllocationDetail(1L, 2L, 3L, 4L, 5L, 6L)(fakeRequest)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getGPAPaymentAllocationDetail(1L, 2L, 3L, 4L, 5L, 6L)
    }

  }
}
