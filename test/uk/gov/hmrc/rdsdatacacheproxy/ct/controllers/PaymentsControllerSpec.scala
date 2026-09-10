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
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{PaymentTransactions, Payments}
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.PaymentsService

import java.time.LocalDate
import scala.concurrent.Future

class PaymentsControllerSpec extends SpecBase with MockitoSugar {

  private trait Setup {
    val mockService: PaymentsService = mock[PaymentsService]
    val controller = new PaymentsController(fakeAuthAction, mockService, cc)
  }

  val payments: List[PaymentTransactions] = List(
    PaymentTransactions(amount = 123.44, paymentType   = "CP", effectiveDateOfPayment = LocalDate.of(2026, 1, 1)),
    PaymentTransactions(amount = 3213.44, paymentType  = "CP", effectiveDateOfPayment = LocalDate.of(2026, 2, 1)),
    PaymentTransactions(amount = 56785.45, paymentType = "CP", effectiveDateOfPayment = LocalDate.of(2026, 1, 23))
  )

  val emptyPayments: List[PaymentTransactions] = List.empty

  "PaymentsController getPayments" - {

    "returns 200 when Payments retrieved from repository" in new Setup {

      when(mockService.getPayments(any[Long], any[Long]))
        .thenReturn(Future.successful(payments))

      val result: Future[Result] = controller.getPayments(1, 1)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some(JSON)
      contentAsJson(result) mustBe Json.toJson(Payments(payments))

      verify(mockService).getPayments(1, 1)
    }

    "returns 200 when empty Payments retrieved from repository" in new Setup {

      when(mockService.getPayments(any[Long], any[Long]))
        .thenReturn(Future.successful(emptyPayments))

      val result: Future[Result] = controller.getPayments(1, 1)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some(JSON)
      contentAsJson(result) mustBe Json.toJson(Payments(emptyPayments))

      verify(mockService).getPayments(1, 1)
    }

    "returns 500 when unexpected error" in new Setup {

      when(mockService.getPayments(any[Long], any[Long]))
        .thenReturn(Future.failed(new RuntimeException("Error")))

      val result: Future[Result] = controller.getPayments(1, 1)(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR

      verify(mockService).getPayments(1, 1)
    }
  }
}
