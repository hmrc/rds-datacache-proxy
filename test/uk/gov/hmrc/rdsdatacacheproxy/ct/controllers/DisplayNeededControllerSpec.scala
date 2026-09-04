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
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsJson, contentType, status}
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.DisplayNeeded
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.DisplayNeededService
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.DisplayNeededHelper.{displayNeededAllFalse, displayNeededAllTrue, displayNeededMixed}

import scala.concurrent.Future

class DisplayNeededControllerSpec extends SpecBase with MockitoSugar {

  private trait Setup {
    val mockService: DisplayNeededService = mock[DisplayNeededService]
    val mockController: DisplayNeededController = new DisplayNeededController(fakeAuthAction, mockService, cc)
  }

  "getDisplayNeeded" - {

    "returns 200 with Display Needed with all details set to false" in new Setup {
      val taxPayerReference: Long = 10L
      val accountingPeriod: Long = 1L

      when(mockService.getDisplayNeeded(taxPayerReference, accountingPeriod)).thenReturn(Future.successful(displayNeededAllFalse))

      val result: Future[Result] = mockController.getDisplayNeeded(taxPayerReference, accountingPeriod)(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(displayNeededAllFalse)

      verify(mockService).getDisplayNeeded(taxPayerReference, accountingPeriod)
      verify(mockService, times(1)).getDisplayNeeded(taxPayerReference, accountingPeriod)

    }

    "returns 200 with Display Needed with all details set to true" in new Setup {
      val taxPayerReference: Long = 20L
      val accountingPeriod: Long = 1L

      when(mockService.getDisplayNeeded(taxPayerReference, accountingPeriod)).thenReturn(Future.successful(displayNeededAllTrue))

      val result: Future[Result] = mockController.getDisplayNeeded(taxPayerReference, accountingPeriod)(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(displayNeededAllTrue)

      verify(mockService).getDisplayNeeded(taxPayerReference, accountingPeriod)
      verify(mockService, times(1)).getDisplayNeeded(taxPayerReference, accountingPeriod)
    }

    "returns 200 with Display Needed with some details set to true or false" in new Setup {
      val taxPayerReference: Long = 30L
      val accountingPeriod: Long = 1L

      when(mockService.getDisplayNeeded(taxPayerReference, accountingPeriod)).thenReturn(Future.successful(displayNeededMixed))

      val result: Future[Result] = mockController.getDisplayNeeded(taxPayerReference, accountingPeriod)(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(displayNeededMixed)

      verify(mockService).getDisplayNeeded(taxPayerReference, accountingPeriod)
      verify(mockService, times(1)).getDisplayNeeded(taxPayerReference, accountingPeriod)
    }

    "returns 500 with generic error message on runtime exception" in new Setup {
      val taxPayerReference: Long = 999L
      val accountingPeriod: Long = 1L
      when(mockService.getDisplayNeeded(taxPayerReference, accountingPeriod)).thenReturn(Future.failed(new RuntimeException("Error from downstream")))

      val result: Future[Result] = mockController.getDisplayNeeded(taxPayerReference, accountingPeriod)(fakeRequest)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      (contentAsJson(result) \ "error").as[String] mustBe "Failed to retrieve display needed"

      verify(mockService).getDisplayNeeded(taxPayerReference, accountingPeriod)
      verify(mockService, times(1)).getDisplayNeeded(taxPayerReference, accountingPeriod)
    }

  }

}
