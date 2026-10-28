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
import org.mockito.Mockito.{times, verify, when}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsJson, contentType, status}
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.{PaymentPostingResponse, PaymentPostingRow}
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.payments.PaymentPostingsService

import java.time.LocalDate
import scala.concurrent.Future

class PaymentPostingsControllerSpec extends SpecBase {

  private trait Setup {
    val mockService: PaymentPostingsService = mock[PaymentPostingsService]

    val controller = new PaymentPostingsController(cc, fakeAuthAction, mockService)

    val taxRef: Long = 98765232L

    val emptyList: PaymentPostingResponse = PaymentPostingResponse(paymentPostings = List.empty)

    val paymentPostingResponse: PaymentPostingResponse = PaymentPostingResponse(
      paymentPostings = List(
        PaymentPostingRow(
          accountingPeriod       = 12820832002L,
          settlementSequenceNo   = 5678L,
          payslipId              = 999123L,
          postingAmount          = 123.45,
          effectiveDateOfPayment = LocalDate.of(2016, 11, 9),
          paymentType            = "BAC"
        ),
        PaymentPostingRow(
          accountingPeriod       = 12820832002L,
          settlementSequenceNo   = 5678L,
          payslipId              = 999123L,
          postingAmount          = 123.45,
          effectiveDateOfPayment = LocalDate.of(2017, 11, 9),
          paymentType            = "BAC"
        )
      )
    )
  }

  "getPaymentPostings" - {

    "should return 200 OK with empty list of Payment Postings " in new Setup {

      when(mockService.getPaymentPostings(any())).thenReturn(Future.successful(emptyList))

      val result: Future[Result] = controller.getPaymentPostings(taxRef)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(emptyList)

      verify(mockService).getPaymentPostings(taxRef)
      verify(mockService, times(1)).getPaymentPostings(taxRef)

    }
    "should return 200 OK with list Payment Postings containing multiple elements " in new Setup {

      when(mockService.getPaymentPostings(any())).thenReturn(Future.successful(paymentPostingResponse))

      val result: Future[Result] = controller.getPaymentPostings(taxRef)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(paymentPostingResponse)

      verify(mockService).getPaymentPostings(taxRef)
      verify(mockService, times(1)).getPaymentPostings(taxRef)

    }
    "should return 500 when there is an exception returned from the service " in new Setup {

      when(mockService.getPaymentPostings(any())).thenReturn(Future.failed(new RuntimeException("Error")))

      val result: Future[Result] = controller.getPaymentPostings(taxRef)(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some("application/json")
      (contentAsJson(result) \ "message").as[String] mustBe "Unexpected error"

      verify(mockService).getPaymentPostings(taxRef)
      verify(mockService, times(1)).getPaymentPostings(taxRef)
    }
  }

}
