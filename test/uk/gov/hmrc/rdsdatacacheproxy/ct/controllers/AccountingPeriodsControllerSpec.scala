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
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.AccountingPeriodsHelper
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.RdsAccountingPeriod
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.AccountingPeriodsService

import scala.concurrent.Future

class AccountingPeriodsControllerSpec extends SpecBase with AccountingPeriodsHelper {

  private trait Setup {
    val mockService: AccountingPeriodsService = mock[AccountingPeriodsService]

    val controller = new AccountingPeriodsController(fakeAuthAction, mockService, cc)

    val taxPayerReference: Long = 98765L

  }

  "getAccountingPeriods" - {

    "should return 200 OK with empty list of RdsAccountingPeriod " in new Setup {

      when(mockService.getAccountingPeriods(any())).thenReturn(Future.successful(emptyAccountingPeriods))

      val result: Future[Result] = controller.getAccountingPeriods(taxPayerReference)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(emptyAccountingPeriods)

      verify(mockService).getAccountingPeriods(taxPayerReference)
      verify(mockService, times(1)).getAccountingPeriods(taxPayerReference)

    }
    "should return 200 OK with list RdsAccountingPeriod containing single elements " in new Setup {

      when(mockService.getAccountingPeriods(any())).thenReturn(Future.successful(accountingPeriodsWithSingleItem))

      val result: Future[Result] = controller.getAccountingPeriods(taxPayerReference)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(accountingPeriodsWithSingleItem)

      verify(mockService).getAccountingPeriods(taxPayerReference)
      verify(mockService, times(1)).getAccountingPeriods(taxPayerReference)

    }
    "should return 200 OK with list RdsAccountingPeriod containing multiple elements " in new Setup {

      when(mockService.getAccountingPeriods(any())).thenReturn(Future.successful(accountingPeriodsWithMultipleItems))

      val result: Future[Result] = controller.getAccountingPeriods(taxPayerReference)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(accountingPeriodsWithMultipleItems)

      verify(mockService).getAccountingPeriods(taxPayerReference)
      verify(mockService, times(1)).getAccountingPeriods(taxPayerReference)

    }
    "should return 500 when there is  an exception returned from the service " in new Setup {

      when(mockService.getAccountingPeriods(any())).thenReturn(Future.failed(new RuntimeException("Boom")))

      val result: Future[Result] = controller.getAccountingPeriods(taxPayerReference)(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some("application/json")
      (contentAsJson(result) \ "error").as[String] mustBe "Failed to retrieve accounting periods"

      verify(mockService).getAccountingPeriods(taxPayerReference)
      verify(mockService, times(1)).getAccountingPeriods(taxPayerReference)
    }
  }

}
