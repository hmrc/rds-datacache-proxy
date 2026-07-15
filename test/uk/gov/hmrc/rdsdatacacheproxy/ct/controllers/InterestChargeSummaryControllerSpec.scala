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

import org.mockito.Mockito.{times, verify, when}
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsJson, contentType, status}
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{InterestCharges, InterestChargesResponse}
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.InterestChargeService

import scala.concurrent.Future

class InterestChargeSummaryControllerSpec extends SpecBase with MockitoSugar {

  private trait Setup {
    val mockService: InterestChargeService = mock[InterestChargeService]
    val mockController: InterestChargeSummaryController = new InterestChargeSummaryController(cc, fakeAuthAction, mockService)
    val emptyInterestCharges: InterestCharges = InterestCharges(List.empty)
    val interestCharges: InterestCharges =
      InterestCharges(
        List(
          InterestChargesResponse(
            accountingPeriod      = BigDecimal(12),
            interestChargeSummary = BigDecimal(123.45)
          ),
          InterestChargesResponse(
            accountingPeriod      = BigDecimal(145),
            interestChargeSummary = BigDecimal(987.45)
          )
        )
      )
  }

  "getInterestController" - {

    "returns 200 with List of InterestCharges when the services succeeds" in new Setup {
      val taxPayerReference: String = "11237658"

      when(mockService.getInterestSummaryList(taxPayerReference)).thenReturn(Future.successful(interestCharges))

      val result: Future[Result] = mockController.getInterestController(taxPayerReference)(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(interestCharges)

      verify(mockService).getInterestSummaryList(taxPayerReference)
      verify(mockService, times(1)).getInterestSummaryList(taxPayerReference)

    }

    "returns 200 with empty response when service returns no items " in new Setup {
      val taxPayerReference: String = "11237658"

      when(mockService.getInterestSummaryList(taxPayerReference)).thenReturn(Future.successful(emptyInterestCharges))

      val result: Future[Result] = mockController.getInterestController(taxPayerReference)(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(emptyInterestCharges)

      verify(mockService).getInterestSummaryList(taxPayerReference)
      verify(mockService, times(1)).getInterestSummaryList(taxPayerReference)
    }

    "returns 400 when TaxPayerReference is missing" in new Setup {

      val result: Future[Result] = mockController.getInterestController("")(fakeRequest)

      status(result) shouldBe BAD_REQUEST

      verify(mockService, times(0)).getInterestSummaryList("")
    }

    "returns 500 with generic message on unexpected exception" in new Setup {
      val taxPayerReference: String = "11237658"
      when(mockService.getInterestSummaryList(taxPayerReference)).thenReturn(Future.failed(new RuntimeException("Database error")))

      val result: Future[Result] = mockController.getInterestController(taxPayerReference)(fakeRequest)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      (contentAsJson(result) \ "message").as[String] mustBe "Unexpected error"

      verify(mockService).getInterestSummaryList(taxPayerReference)
      verify(mockService, times(1)).getInterestSummaryList(taxPayerReference)
    }

  }

}
