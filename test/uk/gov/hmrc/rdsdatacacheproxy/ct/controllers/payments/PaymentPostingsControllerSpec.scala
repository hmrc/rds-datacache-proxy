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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.PaymentPostingsResponse
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.payments.PaymentPostingsService
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.payments.PaymentPostingsStubData

import scala.concurrent.Future

class PaymentPostingsControllerSpec extends SpecBase with PaymentPostingsStubData {

  private trait Setup {
    val mockService: PaymentPostingsService = mock[PaymentPostingsService]

    val controller = new PaymentPostingsController(cc, fakeAuthAction, mockService)

    val emptyPaymentPostingsTaxRef: Long = 124252L
    val singlePaymentPostingsTaxRef: Long = 7293742L
    val multiplePaymentPostingsTaxRef: Long = 93837L
    val errorTaxRef: Long = 99999L

  }

  "getPaymentPostings" - {

    "should return 200 OK with empty list of Payment Postings " in new Setup {

      when(mockService.getPaymentPostings(any())).thenReturn(Future.successful(emptyPaymentPostings))

      val result: Future[Result] = controller.getPaymentPostings(emptyPaymentPostingsTaxRef)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(emptyPaymentPostings)

      verify(mockService).getPaymentPostings(emptyPaymentPostingsTaxRef)
      verify(mockService, times(1)).getPaymentPostings(emptyPaymentPostingsTaxRef)
    }

    "should return 200 OK with a single Payment Posting " in new Setup {
      when(mockService.getPaymentPostings(any())).thenReturn(Future.successful(singlePaymentPosting))

      val result: Future[Result] = controller.getPaymentPostings(singlePaymentPostingsTaxRef)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(singlePaymentPosting)

      verify(mockService).getPaymentPostings(singlePaymentPostingsTaxRef)
      verify(mockService, times(1)).getPaymentPostings(singlePaymentPostingsTaxRef)
    }

    "should return 200 OK with Payment Postings containing multiple elements " in new Setup {

      when(mockService.getPaymentPostings(any())).thenReturn(Future.successful(multiplePaymentPostings))

      val result: Future[Result] = controller.getPaymentPostings(multiplePaymentPostingsTaxRef)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(multiplePaymentPostings)

      verify(mockService).getPaymentPostings(multiplePaymentPostingsTaxRef)
      verify(mockService, times(1)).getPaymentPostings(multiplePaymentPostingsTaxRef)

    }

    "should return 500 when there is an exception returned from the service " in new Setup {

      when(mockService.getPaymentPostings(any())).thenReturn(Future.failed(new RuntimeException("Unexpected error")))

      val result: Future[Result] = controller.getPaymentPostings(errorTaxRef)(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some("application/json")
      (contentAsJson(result) \ "message").as[String] mustBe "Unexpected error"

      verify(mockService).getPaymentPostings(errorTaxRef)
      verify(mockService, times(1)).getPaymentPostings(errorTaxRef)
    }
  }

}
