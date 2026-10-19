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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.{PaymentAllocationChequeDetails, PaymentAllocationChequeRow}
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.payments.PaymentAllocationChequeDetailsService

import java.time.LocalDate
import scala.concurrent.Future

class PaymentAllocationChequeDetailsControllerSpec extends SpecBase {

  private trait Setup {
    val mockService: PaymentAllocationChequeDetailsService = mock[PaymentAllocationChequeDetailsService]

    val controller = new PaymentAllocationChequeDetailsController(cc, fakeAuthAction, mockService)

    val taxRef: Long = 98765232L
    val chequeId: Long = 56L

    val emptyList: PaymentAllocationChequeDetails = PaymentAllocationChequeDetails(paymentAllocationDetails = List.empty)

    val paymentAllocationChequeDetails: PaymentAllocationChequeDetails = PaymentAllocationChequeDetails(
      paymentAllocationDetails = List(
        PaymentAllocationChequeRow(
          accountingPeriod       = 729372L,
          apEndDate              = LocalDate.of(2017, 4, 21),
          amount                 = Some(BigDecimal(2342.922)),
          effectiveDateOfPayment = LocalDate.of(2021, 9, 1)
        ),
        PaymentAllocationChequeRow(
          accountingPeriod       = 7292L,
          apEndDate              = LocalDate.of(2025, 4, 21),
          amount                 = Some(BigDecimal(23426225.922)),
          effectiveDateOfPayment = LocalDate.of(2011, 9, 1)
        )
      )
    )
  }

  "getPaymentAllocationChequeDetails" - {

    "should return 200 OK with empty list of PaymentAllocationChequeDetails " in new Setup {

      when(mockService.getPaymentAllocationChequeDetails(any(), any())).thenReturn(Future.successful(emptyList))

      val result: Future[Result] = controller.getPaymentAllocationChequeDetails(taxRef, chequeId)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(emptyList)

      verify(mockService).getPaymentAllocationChequeDetails(taxRef, chequeId)
      verify(mockService, times(1)).getPaymentAllocationChequeDetails(taxRef, chequeId)

    }
    "should return 200 OK with list PaymentAllocationChequeDetails containing multiple elements " in new Setup {

      when(mockService.getPaymentAllocationChequeDetails(any(), any())).thenReturn(Future.successful(paymentAllocationChequeDetails))

      val result: Future[Result] = controller.getPaymentAllocationChequeDetails(taxRef, chequeId)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(paymentAllocationChequeDetails)

      verify(mockService).getPaymentAllocationChequeDetails(taxRef, chequeId)
      verify(mockService, times(1)).getPaymentAllocationChequeDetails(taxRef, chequeId)

    }
    "should return 500 when there is  an exception returned from the service " in new Setup {

      when(mockService.getPaymentAllocationChequeDetails(any(), any())).thenReturn(Future.failed(new RuntimeException("Boom")))

      val result: Future[Result] = controller.getPaymentAllocationChequeDetails(taxRef, chequeId)(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some("application/json")
      (contentAsJson(result) \ "message").as[String] mustBe "Unexpected error"

      verify(mockService).getPaymentAllocationChequeDetails(taxRef, chequeId)
      verify(mockService, times(1)).getPaymentAllocationChequeDetails(taxRef, chequeId)
    }
  }

}
