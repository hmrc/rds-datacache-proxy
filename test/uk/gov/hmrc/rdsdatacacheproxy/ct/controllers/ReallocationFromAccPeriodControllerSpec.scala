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
import org.mockito.Mockito.{times, verify, when}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsJson, contentType, status}
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{ReallocationFromAccDetails, ReallocationFromAccPeriod}
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.ReallocationFromAccPeriodService

import java.time.LocalDate
import scala.concurrent.Future

class ReallocationFromAccPeriodControllerSpec extends SpecBase {

  private trait Setup {
    val mockService: ReallocationFromAccPeriodService = mock[ReallocationFromAccPeriodService]

    val controller = new ReallocationFromAccPeriodController(cc, fakeAuthAction, mockService)

    val taxPayerReference: Long = 98765L
    val accountingPeriod: Long = 562763L

    val emptyList: ReallocationFromAccPeriod = ReallocationFromAccPeriod(List.empty)

    val reallocationFromAccPeriod: ReallocationFromAccPeriod = ReallocationFromAccPeriod(
      List(
        ReallocationFromAccDetails(
          Some(BigDecimal(12390)),
          Some(LocalDate.of(2026, 12, 27)),
          Some(LocalDate.of(204, 2, 2)),
          Some("18969779586")
        ),
        ReallocationFromAccDetails(
          Some(BigDecimal(12345)),
          Some(LocalDate.of(2026, 12, 27)),
          Some(LocalDate.of(204, 2, 2)),
          Some("18969779586")
        )
      )
    )
  }

  "getReallocationFromAccPeriod" - {

    "should return 200 OK with empty list of ReallocationFromAccPeriod " in new Setup {

      when(mockService.getReallocationFromAccPeriod(any(), any())).thenReturn(Future.successful(emptyList))

      val result: Future[Result] = controller.getReallocationFromAccPeriod(taxPayerReference, accountingPeriod)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(emptyList)

      verify(mockService).getReallocationFromAccPeriod(taxPayerReference, accountingPeriod)
      verify(mockService, times(1)).getReallocationFromAccPeriod(taxPayerReference, accountingPeriod)

    }
    "should return 200 OK with list ReallocationFromAccPeriod containing two elements " in new Setup {

      when(mockService.getReallocationFromAccPeriod(any(), any())).thenReturn(Future.successful(reallocationFromAccPeriod))

      val result: Future[Result] = controller.getReallocationFromAccPeriod(taxPayerReference, accountingPeriod)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(reallocationFromAccPeriod)

      verify(mockService).getReallocationFromAccPeriod(taxPayerReference, accountingPeriod)
      verify(mockService, times(1)).getReallocationFromAccPeriod(taxPayerReference, accountingPeriod)

    }
    "should return 500 when there is  an exception returned from the service " in new Setup {

      when(mockService.getReallocationFromAccPeriod(any(), any())).thenReturn(Future.failed(new RuntimeException("Boom")))

      val result: Future[Result] = controller.getReallocationFromAccPeriod(taxPayerReference, accountingPeriod)(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some("application/json")
      (contentAsJson(result) \ "message").as[String] mustBe "Unexpected error"

      verify(mockService).getReallocationFromAccPeriod(taxPayerReference, accountingPeriod)
      verify(mockService, times(1)).getReallocationFromAccPeriod(taxPayerReference, accountingPeriod)
    }
  }

}
