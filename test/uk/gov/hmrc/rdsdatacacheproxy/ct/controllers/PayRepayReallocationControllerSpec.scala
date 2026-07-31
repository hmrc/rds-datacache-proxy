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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{PayRepayReallocations, PayRepayReallocationsList}
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.PayRepayReallocationService

import scala.concurrent.Future

class PayRepayReallocationControllerSpec extends SpecBase with MockitoSugar {

  private class Setup {
    val mockService: PayRepayReallocationService = mock[PayRepayReallocationService]
    val controller: PayRepayReallocationController = new PayRepayReallocationController(fakeAuthAction, mockService, cc)

    val emptyPayRepayReallocationsList: PayRepayReallocationsList = PayRepayReallocationsList(List.empty)
    val payRepayReallocationsList: PayRepayReallocationsList = PayRepayReallocationsList(
      List(
        PayRepayReallocations(totalAmountReoRfrRto = Some(BigDecimal(50.00)), totalAmountPayments  = Some(BigDecimal(60.00))),
        PayRepayReallocations(totalAmountReoRfrRto = Some(BigDecimal(100.00)), totalAmountPayments = Some(BigDecimal(110.00)))
      )
    )
  }

  "PayRepayReallocationControllerSpec" - {
    "return a 200 and a successful response when retrieving payment repayment reallocation list" in new Setup {
      when(mockService.getTotalAmounts(any[Long], any[Long]))
        .thenReturn(Future.successful(emptyPayRepayReallocationsList))

      val result: Future[Result] = controller.getTotalAmounts(1L, 2L)(fakeRequest)

      status(result)      shouldBe OK
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getTotalAmounts(1L, 2L)
    }

    "return 200 and a successful response when repository return payment repayment reallocation list with two items " in new Setup {
      when(mockService.getTotalAmounts(any[Long], any[Long]))
        .thenReturn(Future.successful(payRepayReallocationsList))

      val result: Future[Result] = controller.getTotalAmounts(1L, 2L)(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(payRepayReallocationsList)
      verify(mockService).getTotalAmounts(1L, 2L)
    }

    "return 500 and when repository call fails" in new Setup {
      when(mockService.getTotalAmounts(any[Long], any[Long]))
        .thenReturn(Future.failed(new RuntimeException("Error")))

      val result: Future[Result] = controller.getTotalAmounts(1L, 10L)(fakeRequest)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getTotalAmounts(1L, 10L)
    }

  }
}
